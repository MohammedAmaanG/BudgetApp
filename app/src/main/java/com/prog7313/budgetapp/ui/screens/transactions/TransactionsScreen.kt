package com.prog7313.budgetapp.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.prog7313.budgetapp.data.model.Category
import com.prog7313.budgetapp.data.model.Expense
import com.prog7313.budgetapp.ui.components.SectionHeader
import com.prog7313.budgetapp.ui.theme.AccentRed
import com.prog7313.budgetapp.viewmodel.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: AppViewModel, onAddExpense: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var fromDate by remember { mutableStateOf(state.filterFrom) }
    var toDate   by remember { mutableStateOf(state.filterTo) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var receiptUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Transactions", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    IconButton(onClick = onAddExpense) {
                        Icon(Icons.Default.Add, "Add")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Period chip
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text("$fromDate → $toDate", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text(
                    "${state.expenses.size} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }

            // Total row
            Card(
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total spent", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "R ${"%,.2f".format(state.expenses.sumOf { it.amount })}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.expenses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No expenses in this period", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onAddExpense) { Text("Add Expense") }
                    }
                }
            } else {
                // Group by date
                val grouped = state.expenses.groupBy { it.date }.toSortedMap(reverseOrder())
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    grouped.forEach { (date, expenses) ->
                        item {
                            Text(
                                text = formatDateHeader(date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        items(expenses, key = { it.id }) { expense ->
                            val category = state.categories.find { it.id == expense.categoryId }
                            ExpenseRow(
                                expense  = expense,
                                category = category,
                                onDelete = { viewModel.deleteExpense(expense.id) },
                                onViewReceipt = { receiptUrl = expense.receiptUrl }
                            )
                        }
                    }
                }
            }
        }
    }

    // Date filter dialog
    if (showFilterDialog) {
        DateFilterDialog(
            from = fromDate, to = toDate,
            onDismiss = { showFilterDialog = false },
            onApply = { f, t ->
                fromDate = f; toDate = t
                viewModel.setFilter(f, t)
                showFilterDialog = false
            }
        )
    }

    // Receipt viewer dialog
    receiptUrl?.let { url ->
        Dialog(onDismissRequest = { receiptUrl = null }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Receipt", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { receiptUrl = null }) { Icon(Icons.Default.Close, null) }
                    }
                    AsyncImage(
                        model             = url,
                        contentDescription = "Receipt",
                        modifier          = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

// ── Expense row ───────────────────────────────────────────────────────────────

@Composable
private fun ExpenseRow(
    expense: Expense,
    category: Category?,
    onDelete: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val catColor = try { Color(category?.color?.toColorInt() ?: 0xFF4A90D9.toInt()) }
    catch (e: Exception) { Color(0xFF4A90D9.toInt()) }
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable { showDelete = !showDelete },
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(catColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(category?.icon ?: "💸", fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(expense.description.ifBlank { "Expense" }, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category?.name ?: "Uncategorised", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    if (expense.receiptUrl != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Receipt, null, Modifier.size(12.dp).clickable { onViewReceipt() },
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("-R ${"%,.2f".format(expense.amount)}", fontWeight = FontWeight.Bold, color = AccentRed)
                Text(expense.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
            if (showDelete) {
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Date filter dialog ────────────────────────────────────────────────────────

@Composable
private fun DateFilterDialog(
    from: String, to: String,
    onDismiss: () -> Unit, onApply: (String, String) -> Unit
) {
    var f by remember { mutableStateOf(from) }
    var t by remember { mutableStateOf(to) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Filter Period") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(f, { f = it }, label = { Text("From (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(t, { t = it }, label = { Text("To (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                // Quick presets
                Text("Quick presets:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val today = LocalDate.now()
                    FilterChip(selected = false, onClick = {
                        f = today.withDayOfMonth(1).toString(); t = today.toString()
                    }, label = { Text("This month") })
                    FilterChip(selected = false, onClick = {
                        val lm = today.minusMonths(1)
                        f = lm.withDayOfMonth(1).toString(); t = lm.withDayOfMonth(lm.lengthOfMonth()).toString()
                    }, label = { Text("Last month") })
                    FilterChip(selected = false, onClick = {
                        f = today.minusDays(30).toString(); t = today.toString()
                    }, label = { Text("30 days") })
                }
            }
        },
        confirmButton = { TextButton(onClick = { onApply(f, t) }) { Text("Apply") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatDateHeader(dateStr: String): String = try {
    val d = LocalDate.parse(dateStr)
    d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } + ", " +
            d.dayOfMonth + " " + d.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + d.year
} catch (e: Exception) { dateStr }