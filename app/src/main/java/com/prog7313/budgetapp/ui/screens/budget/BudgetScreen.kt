package com.prog7313.budgetapp.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.graphics.toColorInt
import androidx.compose.ui.graphics.Color
import com.prog7313.budgetapp.data.model.CategoryWithSpending
import com.prog7313.budgetapp.ui.components.BudgetProgressBar
import com.prog7313.budgetapp.ui.components.SectionHeader
import com.prog7313.budgetapp.ui.theme.*
import com.prog7313.budgetapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categoriesWithSpending = viewModel.categoriesWithSpending()

    var showBudgetDialog   by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Budget", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.Add, "Add Category")
                    }
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(Icons.Default.Edit, "Set Budget")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Monthly budget summary ────────────────────────────────────────
            item {
                val goal  = state.budgetGoal
                val spent = viewModel.totalSpent()
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Budgeted: R ${"%,.2f".format(goal?.totalBudget ?: 0.0)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                Text("R ${"%,.2f".format(spent)}", fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                val avail = (goal?.totalBudget ?: 0.0) - spent
                                Text("R ${"%,.2f".format(avail)}", fontWeight = FontWeight.Bold,
                                    color = if (avail >= 0) AccentGreen else AccentRed)
                            }
                        }
                        if (goal != null && goal.totalBudget > 0) {
                            Spacer(Modifier.height(8.dp))
                            BudgetProgressBar(
                                progress       = (spent / goal.totalBudget).toFloat(),
                                budgetLabel    = "Spent last month: R ${"%,.0f".format(spent)}",
                                spentLabel     = "R ${"%,.0f".format(spent)}",
                                availableLabel = "R ${"%,.0f".format(goal.totalBudget - spent)}",
                                isOverBudget   = goal.maxAmount > 0 && spent > goal.maxAmount
                            )
                        }
                        if (goal == null) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = { showBudgetDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Set Monthly Budget")
                            }
                        }
                    }
                }
            }

            // ── Category rows ─────────────────────────────────────────────────
            item { SectionHeader("Expense Categories") }
            if (categoriesWithSpending.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📂", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No categories yet", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showCategoryDialog = true }) { Text("Add Category") }
                    }
                }
            } else {
                items(categoriesWithSpending) { item ->
                    CategoryBudgetRow(item) { viewModel.deleteCategory(item.category.id) }
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (showBudgetDialog) {
        SetBudgetDialog(
            existing = state.budgetGoal,
            onDismiss = { showBudgetDialog = false },
            onSave    = { min, max, total ->
                viewModel.saveBudgetGoal(min, max, total)
                showBudgetDialog = false
            }
        )
    }
    if (showCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onSave    = { name, icon, color, limit ->
                viewModel.createCategory(name, icon, color, limit)
                showCategoryDialog = false
            }
        )
    }
}

// ── Category row ──────────────────────────────────────────────────────────────

@Composable
private fun CategoryBudgetRow(item: CategoryWithSpending, onDelete: () -> Unit) {
    val catColor = try { Color(item.category.color.toColorInt()) } catch (e: Exception) { AccentBlue }
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(catColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(item.category.icon, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.category.name, fontWeight = FontWeight.Medium)
                    Text("R ${"%,.2f".format(item.totalSpent)}", fontWeight = FontWeight.Bold,
                        color = if (item.isOverBudget) AccentRed else MaterialTheme.colorScheme.onSurface)
                }
                if (item.category.budgetLimit > 0) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.percentOfBudget },
                        modifier  = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color     = if (item.isOverBudget) AccentRed else catColor
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Limit: R ${"%,.0f".format(item.category.budgetLimit)}", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        if (item.isOverBudget)
                            Text("OVER BUDGET", style = MaterialTheme.typography.labelSmall, color = AccentRed, fontWeight = FontWeight.Bold)
                        else
                            Text("Left: R ${"%,.0f".format(item.category.budgetLimit - item.totalSpent)}", style = MaterialTheme.typography.labelSmall,
                                color = AccentGreen)
                    }
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
private fun SetBudgetDialog(
    existing: com.prog7313.budgetapp.data.model.BudgetGoal?,
    onDismiss: () -> Unit,
    onSave: (min: Double, max: Double, total: Double) -> Unit
) {
    var totalBudget  by remember { mutableStateOf(existing?.totalBudget?.toString() ?: "") }
    var minAmount    by remember { mutableStateOf(existing?.minAmount?.toString() ?: "") }
    var maxAmount    by remember { mutableStateOf(existing?.maxAmount?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Set Monthly Budget") },
        text    = {
            Column {
                OutlinedTextField(totalBudget, { totalBudget = it }, label = { Text("Total Budget (R)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(minAmount, { minAmount = it }, label = { Text("Minimum Spending Goal (R)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(maxAmount, { maxAmount = it }, label = { Text("Maximum Spending Limit (R)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    minAmount.toDoubleOrNull() ?: 0.0,
                    maxAmount.toDoubleOrNull() ?: 0.0,
                    totalBudget.toDoubleOrNull() ?: 0.0
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String, limit: Double) -> Unit
) {
    var name    by remember { mutableStateOf("") }
    var icon    by remember { mutableStateOf("💰") }
    var color   by remember { mutableStateOf("#4A90D9") }
    var limit   by remember { mutableStateOf("") }
    val icons   = listOf("💰","🛒","🍔","🚗","🏠","🎮","✈️","💊","📚","🎵","👗","⚡")
    val colors  = com.prog7313.budgetapp.ui.theme.CategoryColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("New Category") },
        text    = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Category Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("Icon", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    icons.forEach { i ->
                        FilterChip(selected = icon == i, onClick = { icon = i }, label = { Text(i) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(limit, { limit = it }, label = { Text("Budget Limit (R, optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (name.isNotBlank()) onSave(name, icon, color, limit.toDoubleOrNull() ?: 0.0) },
                enabled  = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}