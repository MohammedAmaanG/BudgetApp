package com.prog7313.budgetapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prog7313.budgetapp.data.model.*
import com.prog7313.budgetapp.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

private const val TAG = "AppViewModel"



const val USE_LOCAL_DB = true

data class AppUiState(
    val categories: List<Category> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val budgetGoal: BudgetGoal? = null,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val spendingByCategory: Map<String, Double> = emptyMap(),
    val monthlyRecurringTotal: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterFrom: String = LocalDate.now().withDayOfMonth(1).toString(),
    val filterTo: String = LocalDate.now().toString()
)

/*
Title: StateFlow and SharedFlow — Android Kotlin
Author(s): Android Developers
Date: 2024
Version: N/A
Type: Documentation
Availability: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
*/


class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx = application.applicationContext


    private val expenseRepo: Any   = if (USE_LOCAL_DB) LocalExpenseRepository(ctx)   else ExpenseRepository()
    private val budgetRepo: Any    = if (USE_LOCAL_DB) LocalBudgetRepository(ctx)    else BudgetRepository()
    private val savingsRepo: Any   = if (USE_LOCAL_DB) LocalSavingsRepository(ctx)   else SavingsRepository()
    private val recurringRepo: Any = if (USE_LOCAL_DB) LocalRecurringRepository(ctx) else RecurringRepository()

    /*
    Title: AndroidViewModel — Accessing application context safely in ViewModel
    Author(s): Android Developers
    Date: 2024
    Version: Lifecycle 2.8.2
    Type: Documentation
    Availability: https://developer.android.com/reference/androidx/lifecycle/AndroidViewModel
    */

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init { loadAll() }


    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val from = _state.value.filterFrom
                val to   = _state.value.filterTo
                val now  = LocalDate.now()

                val categories = getCategories()
                val expenses   = getExpenseList(from, to)
                val goal       = getBudget(now.monthValue, now.year)
                val savings    = getSavings()
                val recurring  = getRecurring()
                val daily      = computeDaily(expenses)
                val byCat      = computeByCategory(expenses)
                val recurTotal = computeRecurringTotal(recurring)

                _state.value = _state.value.copy(
                    categories            = categories,
                    expenses              = expenses,
                    budgetGoal            = goal,
                    savingsGoals          = savings,
                    recurringTransactions = recurring,
                    dailySpending         = daily,
                    spendingByCategory    = byCat,
                    monthlyRecurringTotal = recurTotal,
                    isLoading             = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadAll failed", e)
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /*
    Title: Kotlin Coroutines — viewModelScope
    Author(s): Android Developers
    Date: 2024
    Version: Lifecycle 2.8.2
    Type: Documentation
    Availability: https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope
    */


    private suspend fun getCategories(): List<Category> =
        if (USE_LOCAL_DB) (expenseRepo as LocalExpenseRepository).getCategories().getOrThrow()
        else              (expenseRepo as ExpenseRepository).getCategories().getOrThrow()

    private suspend fun getExpenseList(from: String, to: String): List<Expense> =
        if (USE_LOCAL_DB) (expenseRepo as LocalExpenseRepository).getExpenses(from, to).getOrThrow()
        else              (expenseRepo as ExpenseRepository).getExpenses(from, to).getOrThrow()

    private suspend fun getBudget(month: Int, year: Int): BudgetGoal? =
        if (USE_LOCAL_DB) (budgetRepo as LocalBudgetRepository).getBudgetGoal(month, year).getOrNull()
        else              (budgetRepo as BudgetRepository).getBudgetGoal(month, year).getOrNull()

    private suspend fun getSavings(): List<SavingsGoal> =
        if (USE_LOCAL_DB) (savingsRepo as LocalSavingsRepository).getSavingsGoals().getOrThrow()
        else              (savingsRepo as SavingsRepository).getSavingsGoals().getOrThrow()

    private suspend fun getRecurring(): List<RecurringTransaction> =
        if (USE_LOCAL_DB) (recurringRepo as LocalRecurringRepository).getRecurringTransactions().getOrThrow()
        else              (recurringRepo as RecurringRepository).getRecurringTransactions().getOrThrow()

    private fun computeDaily(expenses: List<Expense>): List<DailySpending> =
        expenses.groupBy { it.date }
            .map { (d, items) -> DailySpending(d, items.sumOf { it.amount }) }
            .sortedBy { it.date }

    private fun computeByCategory(expenses: List<Expense>): Map<String, Double> =
        expenses.groupBy { it.categoryId }
            .mapValues { (_, v) -> v.sumOf { it.amount } }

    private fun computeRecurringTotal(list: List<RecurringTransaction>): Double =
        list.filter { it.isActive }.sumOf { t ->
            when (t.frequency) {
                "weekly" -> t.amount * 4.33
                "yearly" -> t.amount / 12.0
                else     -> t.amount
            }
        }


    fun setFilter(from: String, to: String) {
        _state.value = _state.value.copy(filterFrom = from, filterTo = to)
        loadAll()
    }


    fun createCategory(name: String, icon: String, color: String, budgetLimit: Double) {
        val cat = Category(name = name, icon = icon, color = color, budgetLimit = budgetLimit)
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (expenseRepo as LocalExpenseRepository).createCategory(cat)
            else              (expenseRepo as ExpenseRepository).createCategory(cat)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (expenseRepo as LocalExpenseRepository).deleteCategory(id)
            else              (expenseRepo as ExpenseRepository).deleteCategory(id)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }


    fun createExpense(
        amount: Double, date: String, description: String,
        categoryId: String, receiptFile: File? = null
    ) {
        val expense = Expense(amount = amount, date = date,
            description = description, categoryId = categoryId)
        viewModelScope.launch {
            if (USE_LOCAL_DB) {
                val local = expenseRepo as LocalExpenseRepository
                local.createExpense(expense).onSuccess { created ->
                    if (receiptFile != null) {
                        local.saveReceiptLocally(created.id, receiptFile)
                    }
                    loadAll()
                }.onFailure { _state.value = _state.value.copy(error = it.message) }
            } else {
                val remote = expenseRepo as ExpenseRepository
                remote.createExpense(expense).onSuccess { created ->
                    if (receiptFile != null) {
                        remote.uploadReceipt(created.id, receiptFile)
                    }
                    loadAll()
                }.onFailure { _state.value = _state.value.copy(error = it.message) }
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (expenseRepo as LocalExpenseRepository).deleteExpense(id)
            else              (expenseRepo as ExpenseRepository).deleteExpense(id)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }


    fun saveBudgetGoal(minAmount: Double, maxAmount: Double, totalBudget: Double) {
        viewModelScope.launch {
            val now  = LocalDate.now()
            val base = _state.value.budgetGoal ?: BudgetGoal()
            val goal = base.copy(month = now.monthValue, year = now.year,
                minAmount = minAmount, maxAmount = maxAmount, totalBudget = totalBudget)
            val result = if (USE_LOCAL_DB) (budgetRepo as LocalBudgetRepository).upsertBudgetGoal(goal)
            else              (budgetRepo as BudgetRepository).upsertBudgetGoal(goal)
            result.onSuccess { saved -> _state.value = _state.value.copy(budgetGoal = saved) }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }


    fun createSavingsGoal(name: String, target: Double, deadline: String?, icon: String, color: String) {
        val goal = SavingsGoal(name = name, targetAmount = target,
            deadline = deadline, icon = icon, color = color)
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (savingsRepo as LocalSavingsRepository).createSavingsGoal(goal)
            else              (savingsRepo as SavingsRepository).createSavingsGoal(goal)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun contributeToGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (savingsRepo as LocalSavingsRepository).addContribution(goalId, amount)
            else              (savingsRepo as SavingsRepository).addContribution(goalId, amount)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (savingsRepo as LocalSavingsRepository).deleteSavingsGoal(id)
            else              (savingsRepo as SavingsRepository).deleteSavingsGoal(id)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }


    fun createRecurring(
        name: String, amount: Double, frequency: String,
        categoryId: String, nextDate: String, icon: String
    ) {
        val rt = RecurringTransaction(name = name, amount = amount, frequency = frequency,
            categoryId = categoryId, nextDate = nextDate, icon = icon)
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (recurringRepo as LocalRecurringRepository).createRecurring(rt)
            else              (recurringRepo as RecurringRepository).createRecurring(rt)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun toggleRecurring(id: String, isActive: Boolean) {
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (recurringRepo as LocalRecurringRepository).toggleActive(id, isActive)
            else              (recurringRepo as RecurringRepository).toggleActive(id, isActive)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteRecurring(id: String) {
        viewModelScope.launch {
            val result = if (USE_LOCAL_DB) (recurringRepo as LocalRecurringRepository).deleteRecurring(id)
            else              (recurringRepo as RecurringRepository).deleteRecurring(id)
            result.onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun categoriesWithSpending(): List<CategoryWithSpending> {
        val spending = _state.value.spendingByCategory
        return _state.value.categories.map { cat ->
            CategoryWithSpending(category = cat, totalSpent = spending[cat.id] ?: 0.0)
        }
    }

    fun totalSpent(): Double = _state.value.expenses.sumOf { it.amount }

    fun earnedBadges(): List<Badge> {
        val s = _state.value
        return Badges.all.filter { badge ->
            when (badge.id) {
                "first_expense"   -> s.expenses.isNotEmpty()
                "ten_expenses"    -> s.expenses.size >= 10
                "savings_starter" -> s.savingsGoals.isNotEmpty()
                "goal_achieved"   -> s.savingsGoals.any { it.isCompleted }
                "category_master" -> s.categories.size >= 5
                "under_budget"    -> { val g = s.budgetGoal; g != null && totalSpent() <= g.maxAmount }
                else -> false
            }
        }
    }

    /*
    Title: Gamification elements in personal finance applications
    Author(s): Deterding, S., Dixon, D., Khaled, R., Nacke, L.
    Date: 2011
    Version: N/A
    Type: Conference Paper
    Availability: https://doi.org/10.1145/2181037.2181040
    */
}