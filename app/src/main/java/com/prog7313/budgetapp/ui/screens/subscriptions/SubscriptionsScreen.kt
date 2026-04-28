package com.prog7313.budgetapp.ui.screens.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prog7313.budgetapp.data.model.RecurringTransaction
import com.prog7313.budgetapp.ui.components.SectionHeader
import com.prog7313.budgetapp.ui.theme.*
import com.prog7313.budgetapp.viewmodel.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val active   = state.recurringTransactions.filter { it.isActive }
    val inactive = state.recurringTransactions.filter { !it.isActive }
    val monthlyTotal = state.monthlyRecurringTotal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscriptions & Recurring", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, "Add") } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Monthly cost summary ──────────────────────────────────────────
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(16.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Fixed Monthly Costs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("R ${"%,.2f".format(monthlyTotal)}", style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("${active.size} active subscriptions", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        if (monthlyTotal > 0) {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AssistChip(onClick = {}, label = { Text("Daily: R ${"%,.2f".format(monthlyTotal / 30)}") })
                                AssistChip(onClick = {}, label = { Text("Yearly: R ${"%,.0f".format(monthlyTotal * 12)}") })
                            }
                        }
                    }
                }
            }

            if (state.recurringTransactions.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔄", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No recurring transactions", style = MaterialTheme.typography.titleMedium)
                        Text("Add subscriptions like Netflix, gym, etc.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) { Text("Add Subscription") }
                    }
                }
            } else {
                // Active
                if (active.isNotEmpty()) {
                    item { SectionHeader("Active (${active.size})") }
                    items(active, key = { it.id }) { rt ->
                        RecurringRow(rt, state.categories.find { it.id == rt.categoryId }?.icon ?: "🔄",
                            onToggle = { viewModel.toggleRecurring(rt.id, false) },
                            onDelete = { viewModel.deleteRecurring(rt.id) })
                    }
                }

                // Inactive
                if (inactive.isNotEmpty()) {
                    item { SectionHeader("Paused (${inactive.size})") }
                    items(inactive, key = { it.id }) { rt ->
                        RecurringRow(rt, state.categories.find { it.id == rt.categoryId }?.icon ?: "🔄",
                            onToggle = { viewModel.toggleRecurring(rt.id, true) },
                            onDelete = { viewModel.deleteRecurring(rt.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringDialog(
            categories = state.categories,
            onDismiss  = { showAddDialog = false },
            onSave     = { name, amount, freq, catId, nextDate, icon ->
                viewModel.createRecurring(name, amount, freq, catId, nextDate, icon)
                showAddDialog = false
            }
        )
    }
}

// ── Recurring row ─────────────────────────────────────────────────────────────

@Composable
private fun RecurringRow(
    rt: RecurringTransaction,
    catIcon: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (rt.isActive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (rt.isActive) AccentBlue.copy(0.15f) else MaterialTheme.colorScheme.outline.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) { Text(rt.icon.ifBlank { catIcon }, fontSize = 22.sp) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text  = rt.name,
                    fontWeight = FontWeight.Medium,
                    color = if (rt.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label   = { Text(rt.frequencyLabel, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(22.dp)
                    )
                    Text("Next: ${rt.nextDate}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("R ${"%,.2f".format(rt.amount)}", fontWeight = FontWeight.Bold)
                Text(if (rt.isActive) "Active" else "Paused", style = MaterialTheme.typography.labelSmall,
                    color = if (rt.isActive) AccentGreen else MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }

            Spacer(Modifier.width(4.dp))

            // Toggle switch
            Switch(
                checked  = rt.isActive,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(36.dp)
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Add recurring dialog ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecurringDialog(
    categories: List<com.prog7313.budgetapp.data.model.Category>,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, freq: String, catId: String, nextDate: String, icon: String) -> Unit
) {
    var name       by remember { mutableStateOf("") }
    var amount     by remember { mutableStateOf("") }
    var frequency  by remember { mutableStateOf("monthly") }
    var categoryId by remember { mutableStateOf("") }
    var nextDate   by remember { mutableStateOf(LocalDate.now().plusMonths(1).withDayOfMonth(1).toString()) }
    var icon       by remember { mutableStateOf("🔄") }
    var catExpanded by remember { mutableStateOf(false) }
    val frequencies = listOf("weekly" to "Weekly", "monthly" to "Monthly", "yearly" to "Yearly")
    val icons = listOf("🔄","📺","🎵","🏋️","🌐","📱","🎮","💊","📰","☕","🏠","⚡")

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Add Recurring Transaction") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name (e.g. Netflix)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount (R)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

                // Frequency selector
                Text("Frequency:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    frequencies.forEach { (value, label) ->
                        FilterChip(selected = frequency == value, onClick = { frequency = value }, label = { Text(label) })
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(
                        value = categories.find { it.id == categoryId }?.let { "${it.icon} ${it.name}" } ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text("${cat.icon} ${cat.name}") }, onClick = { categoryId = cat.id; catExpanded = false })
                        }
                    }
                }

                OutlinedTextField(nextDate, { nextDate = it }, label = { Text("Next billing date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                // Icon chooser
                Text("Icon:", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    icons.take(6).forEach { i -> FilterChip(selected = icon == i, onClick = { icon = i }, label = { Text(i) }) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    icons.drop(6).forEach { i -> FilterChip(selected = icon == i, onClick = { icon = i }, label = { Text(i) }) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (name.isNotBlank() && amount.toDoubleOrNull() != null) onSave(name, amount.toDouble(), frequency, categoryId, nextDate, icon) },
                enabled  = name.isNotBlank() && amount.toDoubleOrNull() != null
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}