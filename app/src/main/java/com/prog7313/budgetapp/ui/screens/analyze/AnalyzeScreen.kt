package com.prog7313.budgetapp.ui.screens.analyze

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prog7313.budgetapp.ui.components.SectionHeader
import com.prog7313.budgetapp.ui.theme.*
import com.prog7313.budgetapp.viewmodel.AppViewModel
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
//  AnalyzeScreen
//  Uses a custom Compose Canvas bar chart — no third-party chart library needed.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedTab      by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily Spending", "By Category")

    val categorySpend = viewModel.categoriesWithSpending().filter { it.totalSpent > 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Analyze", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Period label
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, null, Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text("${state.filterFrom} → ${state.filterTo}",
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTab,
                    modifier = Modifier.padding(horizontal = 16.dp)) {
                    tabs.forEachIndexed { i, label ->
                        Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                            text = { Text(label) })
                    }
                }
            }

            if (selectedTab == 0) {
                // ── Daily spending bar chart ──────────────────────────────────
                item {
                    Card(
                        modifier  = Modifier.fillMaxWidth().padding(16.dp),
                        shape     = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Daily Spending",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("${state.dailySpending.size} days with activity",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            Spacer(Modifier.height(16.dp))

                            if (state.dailySpending.isNotEmpty()) {
                                val values = state.dailySpending.map { it.totalAmount.toFloat() }
                                BarChart(
                                    values   = values,
                                    barColor = AccentBlue,
                                    modifier = Modifier.fillMaxWidth().height(180.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                // Date labels (first and last)
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(state.dailySpending.first().date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                    Text(state.dailySpending.last().date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                }
                                // Budget goal annotations
                                val goal = state.budgetGoal
                                if (goal != null) {
                                    Spacer(Modifier.height(8.dp))
                                    val avg = state.dailySpending.sumOf { it.totalAmount } /
                                            state.dailySpending.size
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        LegendDot("Avg/day: R ${"%.0f".format(avg)}", AccentBlue)
                                        LegendDot("Max/day: R ${"%.0f".format(goal.maxAmount / 30)}", AccentRed)
                                    }
                                }
                            } else {
                                EmptyChartBox()
                            }
                        }
                    }
                }

            } else {
                // ── Category bar chart ────────────────────────────────────────
                item {
                    Card(
                        modifier  = Modifier.fillMaxWidth().padding(16.dp),
                        shape     = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Spending by Category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            if (categorySpend.isNotEmpty()) {
                                BarChart(
                                    values   = categorySpend.map { it.totalSpent.toFloat() },
                                    barColor = AccentOrange,
                                    modifier = Modifier.fillMaxWidth().height(180.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                // Category name labels
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    categorySpend.take(1).forEach {
                                        Text(it.category.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                    }
                                    categorySpend.lastOrNull()?.let {
                                        Text(it.category.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                    }
                                }
                            } else {
                                EmptyChartBox()
                            }
                        }
                    }
                }

                // Category breakdown list
                item { SectionHeader("Category Breakdown") }
                items(categorySpend.sortedByDescending { it.totalSpent }) { item ->
                    val catColor = try { Color(item.category.color.toColorInt()) }
                    catch (e: Exception) { AccentBlue }
                    Card(
                        modifier  = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 3.dp),
                        shape     = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                    .background(catColor.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) { Text(item.category.icon, fontSize = 20.sp) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.category.name, fontWeight = FontWeight.Medium)
                                if (item.category.budgetLimit > 0) {
                                    LinearProgressIndicator(
                                        progress = { item.percentOfBudget },
                                        modifier  = Modifier.fillMaxWidth().height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color     = if (item.isOverBudget) AccentRed else catColor
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("R ${"%.2f".format(item.totalSpent)}",
                                    fontWeight = FontWeight.Bold)
                                if (item.category.budgetLimit > 0) {
                                    Text(
                                        "${(item.percentOfBudget * 100).toInt()}% of limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item.isOverBudget) AccentRed
                                        else MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Filter dialog ─────────────────────────────────────────────────────────
    if (showFilterDialog) {
        var f by remember { mutableStateOf(state.filterFrom) }
        var t by remember { mutableStateOf(state.filterTo) }
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Period") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(f, { f = it }, label = { Text("From (YYYY-MM-DD)") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(t, { t = it }, label = { Text("To (YYYY-MM-DD)") },
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val today = LocalDate.now()
                        FilterChip(false, {
                            f = today.withDayOfMonth(1).toString()
                            t = today.toString()
                        }, { Text("This month") })
                        FilterChip(false, {
                            f = today.minusMonths(3).toString()
                            t = today.toString()
                        }, { Text("3 months") })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setFilter(f, t); showFilterDialog = false }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Custom Canvas bar chart — no third-party dependency
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BarChart(
    values: List<Float>,
    barColor: Color,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    if (values.isEmpty()) return
    val maxValue = values.max().takeIf { it > 0f } ?: 1f

    // Animate each bar individually from 0 to its target height
    val animatedValues = values.map { target ->
        val anim by animateFloatAsState(
            targetValue   = if (animate) target else target,
            animationSpec = tween(durationMillis = 600),
            label         = "bar_anim"
        )
        anim
    }

    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val totalBars  = values.size
        val spacing    = 4.dp.toPx()
        val barWidth   = ((size.width - spacing * (totalBars + 1)) / totalBars).coerceAtLeast(4f)
        val cornerR    = CornerRadius(3.dp.toPx())

        // Draw baseline
        drawLine(
            color       = surfaceVariantColor,
            start       = Offset(0f, size.height),
            end         = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )

        animatedValues.forEachIndexed { index, value ->
            val barH = (value / maxValue) * (size.height - 8.dp.toPx())
            val x    = spacing + index * (barWidth + spacing)
            val top  = size.height - barH - 2.dp.toPx()

            if (barH > 0f) {
                drawRoundRect(
                    color       = barColor,
                    topLeft     = Offset(x, top),
                    size        = Size(barWidth, barH),
                    cornerRadius = cornerR
                )
            }
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyChartBox() {
    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
        Text("No data for this period",
            color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
    }
}

/*
Title: Compose — Canvas API for custom drawing
Author(s): Android Developers
Date: 2024
Version: Compose BOM 2024.06.00
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/graphics/draw/overview
*/

/*
Title: Compose — drawRoundRect and CornerRadius
Author(s): Android Developers
Date: 2024
Version: Compose BOM 2024.06.00
Type: Documentation
Availability: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/drawscope/DrawScope
*/

/*
Title: Compose — animateFloatAsState for animated bar charts
Author(s): Android Developers
Date: 2024
Version: Compose BOM 2024.06.00
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/animation/value-based#animate-as-state
*/