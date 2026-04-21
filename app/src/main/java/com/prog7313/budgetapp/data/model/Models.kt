package com.prog7313.budgetapp.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)

//  Supabase table: categories
@Serializable
data class Category(
    val id: String = "",
    @SerialName("user_id")       val userId: String = "",
    val name: String = "",
    val icon: String = "💰",
    val color: String = "#4A90D9",
    @SerialName("budget_limit")  val budgetLimit: Double = 0.0,
    @SerialName("created_at")    val createdAt: String = ""
)

/*
Title: Kotlinx Serialization — SerialName annotation
Author(s): JetBrains
Date: 2024
Version: 1.6.3
Type: Documentation
Availability: https://kotlinlang.org/docs/serialization.html
*/

//  Supabase table: expenses
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Expense(
    val id: String = "",
    @SerialName("user_id")       val userId: String = "",
    @SerialName("category_id")   val categoryId: String = "",
    val amount: Double = 0.0,
    val date: String = "",          // ISO-8601: "2024-04-13"
    val description: String = "",
    @SerialName("receipt_url")   val receiptUrl: String? = null,
    @SerialName("created_at")    val createdAt: String = ""
)

//  Supabase table: budget_goals
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BudgetGoal(
    val id: String = "",
    @SerialName("user_id")       val userId: String = "",
    val month: Int = 1,
    val year: Int = 2024,
    @SerialName("min_amount")    val minAmount: Double = 0.0,
    @SerialName("max_amount")    val maxAmount: Double = 0.0,
    @SerialName("total_budget")  val totalBudget: Double = 0.0,
    @SerialName("created_at")    val createdAt: String = ""
)

//  Supabase table: savings_goals  (extra feature 1)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SavingsGoal(
    val id: String = "",
    @SerialName("user_id")          val userId: String = "",
    val name: String = "",
    @SerialName("target_amount")    val targetAmount: Double = 0.0,
    @SerialName("current_amount")   val currentAmount: Double = 0.0,
    val deadline: String? = null,   // ISO-8601 date or null
    val icon: String = "🎯",
    val color: String = "#4CAF50",
    @SerialName("created_at")       val createdAt: String = ""
) {
    /** Progress as 0.0 – 1.0 */
    val progress: Float get() =
        if (targetAmount <= 0) 0f else (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
    val progressPercent: Int get() = (progress * 100).toInt()
    val isCompleted: Boolean get() = currentAmount >= targetAmount
}

//  Supabase table: recurring_transactions  (extra feature 2)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RecurringTransaction(
    val id: String = "",
    @SerialName("user_id")       val userId: String = "",
    @SerialName("category_id")   val categoryId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    /** "monthly" | "weekly" | "yearly" */
    val frequency: String = "monthly",
    @SerialName("next_date")     val nextDate: String = "",
    @SerialName("is_active")     val isActive: Boolean = true,
    val icon: String = "🔄",
    @SerialName("created_at")    val createdAt: String = ""
) {
    val frequencyLabel: String get() = when (frequency) {
        "weekly"  -> "Weekly"
        "yearly"  -> "Yearly"
        else      -> "Monthly"
    }
}

//  UI helper models (not persisted directly)

data class CategoryWithSpending(
    val category: Category,
    val totalSpent: Double,
    val percentOfBudget: Float = if (category.budgetLimit > 0)
        (totalSpent / category.budgetLimit).toFloat().coerceIn(0f, 1f) else 0f,
    val isOverBudget: Boolean = category.budgetLimit > 0 && totalSpent > category.budgetLimit
)

data class DailySpending(
    val date: String,
    val totalAmount: Double
)

// Gamification badge model
data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isEarned: Boolean = false
)

// Predefined badges
object Badges {
    val all = listOf(
        Badge("first_expense",   "First Step",       "Log your first expense",          "🌱"),
        Badge("week_streak",     "Week Warrior",     "Log expenses 7 days in a row",    "🔥"),
        Badge("under_budget",    "Budget Boss",      "Stay within budget for a month",  "🏆"),
        Badge("savings_starter", "Savings Starter",  "Create your first savings goal",  "💰"),
        Badge("goal_achieved",   "Goal Getter",      "Complete a savings goal",         "🎯"),
        Badge("ten_expenses",    "Tracking Pro",     "Log 10 expense entries",          "📊"),
        Badge("category_master", "Organiser",        "Create 5 categories",             "🗂️")
    )
}

/*
Title: Gamification in personal finance apps: a study of user engagement
Author(s): Hamari, J., Koivisto, J.
Date: 2015
Version: N/A
Type: Journal Article
Availability: https://doi.org/10.1016/j.chb.2015.04.042
*/

// Sealed class for screen navigation routes
sealed class Screen(val route: String) {
    object Login          : Screen("login")
    object Register       : Screen("register")
    object Overview       : Screen("overview")
    object Budget         : Screen("budget")
    object Transactions   : Screen("transactions")
    object Analyze        : Screen("analyze")
    object Goals          : Screen("goals")
    object AddExpense     : Screen("add_expense")
    object Subscriptions  : Screen("subscriptions")
    object AddSavingsGoal : Screen("add_savings_goal")
}

/*
Title: Navigate with Compose — Sealed class route destinations
Author(s): Android Developers
Date: 2024
Version: Navigation Compose 2.7.7
Type: Documentation
Availability: https://developer.android.com/develop/ui/compose/navigation
*/