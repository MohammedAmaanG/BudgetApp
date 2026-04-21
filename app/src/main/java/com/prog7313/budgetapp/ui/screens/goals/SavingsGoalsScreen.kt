package com.prog7313.budgetapp.ui.screens.goals

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prog7313.budgetapp.data.model.SavingsGoal
import com.prog7313.budgetapp.ui.components.SectionHeader
import com.prog7313.budgetapp.ui.theme.*
import com.prog7313.budgetapp.viewmodel.AppViewModel
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog   by remember { mutableStateOf(false) }
    var contributeGoal  by remember { mutableStateOf<SavingsGoal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Savings Goals", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Goal")
                    }
                }
            )
        }
    ) { padding ->
        if (state.savingsGoals.isEmpty()) {
            // Empty state
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No savings goals yet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Set a goal and track your progress!", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create First Goal")
                    }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Summary card
                item {
                    val totalTarget  = state.savingsGoals.sumOf { it.targetAmount }
                    val totalSaved   = state.savingsGoals.sumOf { it.currentAmount }
                    val completed    = state.savingsGoals.count { it.isCompleted }
                    Card(
                        modifier  = Modifier.fillMaxWidth().padding(16.dp),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryItem("Goals", "${state.savingsGoals.size}", MaterialTheme.colorScheme.primary)
                            SummaryItem("Saved", "R ${"%,.0f".format(totalSaved)}", AccentGreen)
                            SummaryItem("Target", "R ${"%,.0f".format(totalTarget)}", AccentBlue)
                            SummaryItem("Done", "$completed ✓", AccentPurple)
                        }
                    }
                }

                item { SectionHeader("Your Goals (${state.savingsGoals.size})") }

                items(state.savingsGoals, key = { it.id }) { goal ->
                    SavingsGoalCard(
                        goal      = goal,
                        onContribute = { contributeGoal = goal },
                        onDelete  = { viewModel.deleteSavingsGoal(goal.id) }
                    )
                }
            }
        }
    }

    // Add goal dialog
    if (showAddDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddDialog = false },
            onSave    = { name, target, deadline, icon, color ->
                viewModel.createSavingsGoal(name, target, deadline, icon, color)
                showAddDialog = false
            }
        )
    }

    // Contribute dialog
    contributeGoal?.let { goal ->
        ContributeDialog(
            goal      = goal,
            onDismiss = { contributeGoal = null },
            onSave    = { amount ->
                viewModel.contributeToGoal(goal.id, amount)
                contributeGoal = null
            }
        )
    }
}

// ── Savings goal card ─────────────────────────────────────────────────────────

@Composable
private fun SavingsGoalCard(goal: SavingsGoal, onContribute: () -> Unit, onDelete: () -> Unit) {
    val goalColor = try { Color(goal.color.toColorInt()) } catch (e: Exception) { AccentGreen }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(goalColor.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(goal.icon, fontSize = 24.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(goal.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    goal.deadline?.let {
                        Text("🗓 By $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }
                if (goal.isCompleted) {
                    AssistChip(onClick = {}, label = { Text("✅ Complete!") }, colors = AssistChipDefaults.assistChipColors(containerColor = AccentGreen.copy(0.15f)))
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
            }

            Spacer(Modifier.height(12.dp))

            // Amounts row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Saved", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text("R ${"%,.2f".format(goal.currentAmount)}", fontWeight = FontWeight.Bold, color = goalColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text("R ${"%,.2f".format(goal.targetAmount)}", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier  = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color     = goalColor,
                trackColor = goalColor.copy(0.15f)
            )

            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${goal.progressPercent}% complete", style = MaterialTheme.typography.bodySmall, color = goalColor, fontWeight = FontWeight.Medium)
                val remaining = goal.targetAmount - goal.currentAmount
                if (!goal.isCompleted) {
                    Text("R ${"%,.2f".format(remaining)} to go", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }

            if (!goal.isCompleted) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick  = onContribute,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Money")
                }
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}

@Composable
private fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, target: Double, deadline: String?, icon: String, color: String) -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var target   by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var icon     by remember { mutableStateOf("🎯") }
    var color    by remember { mutableStateOf("#4CAF50") }
    val icons    = listOf("🎯","💻","✈️","🏖️","🚗","🏠","📚","🎸","💍","🎓","🏋️","🎮")

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("New Savings Goal") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Goal Name (e.g. Laptop)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(target, { target = it }, label = { Text("Target Amount (R)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(deadline, { deadline = it }, label = { Text("Deadline (YYYY-MM-DD, optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Choose Icon:", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    icons.take(6).forEach { i ->
                        FilterChip(selected = icon == i, onClick = { icon = i }, label = { Text(i) })
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    icons.drop(6).forEach { i ->
                        FilterChip(selected = icon == i, onClick = { icon = i }, label = { Text(i) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (name.isNotBlank() && target.toDoubleOrNull() != null) onSave(name, target.toDouble(), deadline.ifBlank { null }, icon, color) },
                enabled  = name.isNotBlank() && target.toDoubleOrNull() != null
            ) { Text("Create Goal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ContributeDialog(goal: SavingsGoal, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Add to \"${goal.name}\"") },
        text    = {
            Column {
                Text("Current: R ${"%,.2f".format(goal.currentAmount)} / R ${"%,.2f".format(goal.targetAmount)}")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Amount to add (R)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { amount.toDoubleOrNull()?.let { onSave(it) } },
                enabled  = amount.toDoubleOrNull() != null
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}