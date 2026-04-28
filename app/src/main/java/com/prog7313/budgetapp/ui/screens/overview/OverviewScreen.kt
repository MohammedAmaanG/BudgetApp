package com.prog7313.budgetapp.ui.screens.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prog7313.budgetapp.R
import com.prog7313.budgetapp.data.model.Expense
import com.prog7313.budgetapp.ui.components.BadgeCard
import com.prog7313.budgetapp.ui.components.BudgetProgressBar
import com.prog7313.budgetapp.ui.components.SectionHeader
import com.prog7313.budgetapp.ui.theme.*
import com.prog7313.budgetapp.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    onNavigateToSubscriptions: () -> Unit
) {
    val state   by viewModel.state.collectAsStateWithLifecycle()
    val badges   = viewModel.earnedBadges()
    val spent    = viewModel.totalSpent()
    val goal     = state.budgetGoal
    val now      = LocalDate.now()
    val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter            = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "FinWise",
                            modifier           = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Budget Tracker",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                now.format(monthFmt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSubscriptions) {
                        Icon(Icons.Default.Repeat, "Subscriptions")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(DarkNavy, AccentBlue)))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Small logo inside the hero card
                            Image(
                                painter            = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = null,
                                modifier           = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )

                            /*
                            Title: Compose — Image with clip for rounded corners
                            Author(s): Android Developers
                            Date: 2024
                            Version: Compose BOM 2024.06.00
                            Type: Documentation
                            Availability: https://developer.android.com/develop/ui/compose/graphics/images/customize
                            */

                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Total Spent This Month",
                                color = White.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "R ${"%,.2f".format(spent)}",
                            color      = White,
                            style      = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Spacer(Modifier.height(8.dp))
                        if (goal != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                BudgetStat("Budget",     "R ${"%,.0f".format(goal.totalBudget)}", White)
                                BudgetStat("Remaining",  "R ${"%,.0f".format(goal.totalBudget - spent)}",
                                    if (goal.totalBudget - spent >= 0) AccentGreen else AccentRed)
                                BudgetStat("Recurring",  "R ${"%,.0f".format(state.monthlyRecurringTotal)}", AccentOrange)
                            }
                        } else {
                            Text(
                                "No budget set – go to Budget tab",
                                color = White.copy(0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (goal != null) {
                item {
                    Card(
                        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape     = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Monthly Budget Progress", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))
                            val progress = if (goal.totalBudget > 0) (spent / goal.totalBudget).toFloat() else 0f
                            BudgetProgressBar(
                                progress       = progress,
                                budgetLabel    = "Budget: R ${"%,.0f".format(goal.totalBudget)}",
                                spentLabel     = "Spent: R ${"%,.0f".format(spent)}",
                                availableLabel = "Left: R ${"%,.0f".format(goal.totalBudget - spent)}",
                                isOverBudget   = goal.maxAmount > 0 && spent > goal.maxAmount
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text("Min: R ${"%,.0f".format(goal.minAmount)}") })
                                AssistChip(onClick = {}, label = { Text("Max: R ${"%,.0f".format(goal.maxAmount)}") })
                            }
                        }
                    }
                }
            }

            item { SectionHeader("🏆 Achievements (${badges.size} earned)") }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.prog7313.budgetapp.data.model.Badges.all.forEach { badge ->
                        BadgeCard(
                            badge.icon, badge.title,
                            badges.any { it.id == badge.id }
                        )
                    }
                }
            }

            if (state.savingsGoals.isNotEmpty()) {
                item { SectionHeader("🎯 Savings Goals") }
                state.savingsGoals.take(3).forEach { g ->
                    item {
                        Card(
                            modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            shape     = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(g.icon, fontSize = 28.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(g.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "R ${"%,.0f".format(g.currentAmount)} / R ${"%,.0f".format(g.targetAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { g.progress },
                                        modifier  = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color     = AccentGreen
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${g.progressPercent}%",
                                    fontWeight = FontWeight.Bold,
                                    color      = AccentGreen
                                )
                            }
                        }
                    }
                }
            }

            item { SectionHeader("📋 Recent Transactions") }
            val recent = state.expenses.take(5)
            if (recent.isEmpty()) {
                item {
                    Text(
                        "No transactions yet. Add one!",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            } else {
                recent.forEach { expense ->
                    item {
                        RecentExpenseRow(
                            expense  = expense,
                            category = state.categories.find { it.id == expense.categoryId }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun BudgetStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(label, color = White.copy(0.7f), fontSize = 11.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecentExpenseRow(
    expense: Expense,
    category: com.prog7313.budgetapp.data.model.Category?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { Text(category?.icon ?: "💸", fontSize = 20.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                expense.description.ifBlank { "Expense" },
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                category?.name ?: "Uncategorised",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "-R ${"%,.2f".format(expense.amount)}",
                fontWeight = FontWeight.Bold,
                color      = AccentRed
            )
            Text(
                expense.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
        }
    }
}

/*
Title: Jetpack Compose — LazyColumn for scrollable lists
Author(s): Android Developers
Date: 2024
Version: Compose BOM 2024.06.00
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/lists
*/

/*
Title: Material 3 — TopAppBar composable
Author(s): Android Developers
Date: 2024
Version: Material3 1.2.1
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/components/app-bars
*/