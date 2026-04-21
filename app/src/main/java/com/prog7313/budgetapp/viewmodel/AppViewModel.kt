package com.prog7313.budgetapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prog7313.budgetapp.data.model.*
import com.prog7313.budgetapp.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "AppViewModel"

// ─────────────────────────────────────────────────────────────────────────────
//  UI state containers
// ─────────────────────────────────────────────────────────────────────────────

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
    // Filter period
    val filterFrom: String = LocalDate.now().withDayOfMonth(1).toString(),
    val filterTo: String = LocalDate.now().toString()
)

// ─────────────────────────────────────────────────────────────────────────────
//  AppViewModel
// ─────────────────────────────────────────────────────────────────────────────

class AppViewModel : ViewModel() {

    private val expenseRepo   = ExpenseRepository()
    private val budgetRepo    = BudgetRepository()
    private val savingsRepo   = SavingsRepository()
    private val recurringRepo = RecurringRepository()

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    // ── Load all data ─────────────────────────────────────────────────────────

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val categories = expenseRepo.getCategories().getOrThrow()
                val expenses   = expenseRepo.getExpenses(
                    _state.value.filterFrom, _state.value.filterTo
                ).getOrThrow()
                val now = LocalDate.now()
                val goal = budgetRepo.getBudgetGoal(now.monthValue, now.year).getOrNull()
                val savings   = savingsRepo.getSavingsGoals().getOrThrow()
                val recurring = recurringRepo.getRecurringTransactions().getOrThrow()
                val daily     = expenseRepo.getDailySpending(
                    _state.value.filterFrom, _state.value.filterTo
                ).getOrThrow()
                val byCat  = expenseRepo.getSpendingByCategory(
                    _state.value.filterFrom, _state.value.filterTo
                ).getOrThrow()
                val recurringTotal = recurringRepo.getMonthlyRecurringTotal().getOrElse { 0.0 }

                _state.value = _state.value.copy(
                    categories = categories,
                    expenses = expenses,
                    budgetGoal = goal,
                    savingsGoals = savings,
                    recurringTransactions = recurring,
                    dailySpending = daily,
                    spendingByCategory = byCat,
                    monthlyRecurringTotal = recurringTotal,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadAll failed", e)
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // ── Period filter ─────────────────────────────────────────────────────────

    fun setFilter(from: String, to: String) {
        _state.value = _state.value.copy(filterFrom = from, filterTo = to)
        loadAll()
    }

    // ── Categories ────────────────────────────────────────────────────────────

    fun createCategory(name: String, icon: String, color: String, budgetLimit: Double) {
        viewModelScope.launch {
            val cat = Category(name = name, icon = icon, color = color, budgetLimit = budgetLimit)
            expenseRepo.createCategory(cat).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            expenseRepo.deleteCategory(id).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    // ── Expenses ──────────────────────────────────────────────────────────────

    fun createExpense(
        amount: Double,
        date: String,
        description: String,
        categoryId: String,
        receiptFile: File? = null
    ) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                date = date,
                description = description,
                categoryId = categoryId
            )
            expenseRepo.createExpense(expense).onSuccess { created ->
                // Upload receipt if provided
                if (receiptFile != null) {
                    expenseRepo.uploadReceipt(created.id, receiptFile).onSuccess { url ->
                        expenseRepo.updateExpense(created.copy(receiptUrl = url))
                    }
                }
                loadAll()
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            expenseRepo.deleteExpense(id).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    // ── Budget goal ───────────────────────────────────────────────────────────

    fun saveBudgetGoal(minAmount: Double, maxAmount: Double, totalBudget: Double) {
        viewModelScope.launch {
            val now = LocalDate.now()
            val goal = _state.value.budgetGoal ?: BudgetGoal()
            val toSave = goal.copy(
                month = now.monthValue,
                year = now.year,
                minAmount = minAmount,
                maxAmount = maxAmount,
                totalBudget = totalBudget
            )
            budgetRepo.upsertBudgetGoal(toSave).onSuccess { saved ->
                _state.value = _state.value.copy(budgetGoal = saved)
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    // ── Savings goals ─────────────────────────────────────────────────────────

    fun createSavingsGoal(name: String, target: Double, deadline: String?, icon: String, color: String) {
        viewModelScope.launch {
            val goal = SavingsGoal(name = name, targetAmount = target, deadline = deadline,
                icon = icon, color = color)
            savingsRepo.createSavingsGoal(goal).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun contributeToGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            savingsRepo.addContribution(goalId, amount).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            savingsRepo.deleteSavingsGoal(id).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    // ── Recurring transactions ────────────────────────────────────────────────

    fun createRecurring(
        name: String, amount: Double, frequency: String,
        categoryId: String, nextDate: String, icon: String
    ) {
        viewModelScope.launch {
            val rt = RecurringTransaction(
                name = name, amount = amount, frequency = frequency,
                categoryId = categoryId, nextDate = nextDate, icon = icon
            )
            recurringRepo.createRecurring(rt).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun toggleRecurring(id: String, isActive: Boolean) {
        viewModelScope.launch {
            recurringRepo.toggleActive(id, isActive).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun deleteRecurring(id: String) {
        viewModelScope.launch {
            recurringRepo.deleteRecurring(id).onSuccess { loadAll() }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /** Derive [CategoryWithSpending] list for the budget screen. */
    fun categoriesWithSpending(): List<CategoryWithSpending> {
        val spending = _state.value.spendingByCategory
        return _state.value.categories.map { cat ->
            CategoryWithSpending(
                category = cat,
                totalSpent = spending[cat.id] ?: 0.0
            )
        }
    }

    /** Total spent in the current filter period. */
    fun totalSpent(): Double = _state.value.expenses.sumOf { it.amount }

    /** Check which gamification badges the user has earned. */
    fun earnedBadges(): List<Badge> {
        val s = _state.value
        return Badges.all.filter { badge ->
            when (badge.id) {
                "first_expense"   -> s.expenses.isNotEmpty()
                "ten_expenses"    -> s.expenses.size >= 10
                "savings_starter" -> s.savingsGoals.isNotEmpty()
                "goal_achieved"   -> s.savingsGoals.any { it.isCompleted }
                "category_master" -> s.categories.size >= 5
                "under_budget"    -> {
                    val goal = s.budgetGoal
                    goal != null && totalSpent() <= goal.maxAmount
                }
                else -> false
            }
        }
    }
}