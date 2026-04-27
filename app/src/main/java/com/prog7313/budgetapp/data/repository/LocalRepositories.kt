package com.prog7313.application001.data.repository

import android.content.Context
import android.util.Log
import com.prog7313.application001.data.local.AppDatabase
import com.prog7313.application001.data.local.toDomain
import com.prog7313.application001.data.local.toEntity
import com.prog7313.application001.data.model.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "LocalRepositories"

class LocalExpenseRepository(context: Context) {

    private val db          = AppDatabase.getInstance(context)
    private val categoryDao = db.categoryDao()
    private val expenseDao  = db.expenseDao()

    // The local user ID is set by LocalAuthRepository on login.
    // We read it from LocalUserSession rather than Supabase.
    private val userId get() = LocalUserSession.userId


    suspend fun getCategories(): Result<List<Category>> = try {
        Result.success(categoryDao.getCategories(userId).map { it.toDomain() })
    } catch (e: Exception) {
        Log.e(TAG, "getCategories failed", e); Result.failure(e)
    }

    suspend fun createCategory(category: Category): Result<Category> = try {
        val entity = category.copy(userId = userId).toEntity()
        categoryDao.insertCategory(entity)
        Result.success(entity.toDomain())
    } catch (e: Exception) {
        Log.e(TAG, "createCategory failed", e); Result.failure(e)
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = try {
        categoryDao.deleteCategoryById(categoryId, userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "deleteCategory failed", e); Result.failure(e)
    }

    /*
    Title: Room — Insert, update and delete data
    Author(s): Android Developers
    Date: 2024
    Version: Room 2.6.1
    Type: Documentation
    Availability: https://developer.android.com/training/data-storage/room/inserting-data
    */


    suspend fun getExpenses(
        fromDate: String? = null,
        toDate: String? = null
    ): Result<List<Expense>> = try {
        val list = expenseDao.getExpenses(userId, fromDate, toDate).map { it.toDomain() }
        Result.success(list)
    } catch (e: Exception) {
        Log.e(TAG, "getExpenses failed", e); Result.failure(e)
    }

    suspend fun createExpense(expense: Expense): Result<Expense> = try {
        val entity = expense.copy(userId = userId).toEntity()
        expenseDao.insertExpense(entity)
        Log.i(TAG, "Expense saved locally: ${entity.id}")
        Result.success(entity.toDomain())
    } catch (e: Exception) {
        Log.e(TAG, "createExpense failed", e); Result.failure(e)
    }

    suspend fun updateExpense(expense: Expense): Result<Expense> = try {
        expenseDao.insertExpense(expense.toEntity())  // REPLACE handles update
        Result.success(expense)
    } catch (e: Exception) {
        Log.e(TAG, "updateExpense failed", e); Result.failure(e)
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = try {
        expenseDao.deleteExpense(expenseId, userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "deleteExpense failed", e); Result.failure(e)
    }


    suspend fun getDailySpending(from: String, to: String): Result<List<DailySpending>> =
        getExpenses(from, to).map { list ->
            list.groupBy { it.date }
                .map { (d, items) -> DailySpending(d, items.sumOf { it.amount }) }
                .sortedBy { it.date }
        }

    suspend fun getSpendingByCategory(from: String, to: String): Result<Map<String, Double>> =
        getExpenses(from, to).map { list ->
            list.groupBy { it.categoryId }
                .mapValues { (_, v) -> v.sumOf { it.amount } }
        }

    /*
    Title: Room — Query with optional parameters for date range filtering
    Author(s): Android Developers
    Date: 2024
    Version: Room 2.6.1
    Type: Documentation
    Availability: https://developer.android.com/training/data-storage/room/accessing-data#query
    */


    /**
     * Copies the receipt image to the app's internal files directory and stores
     * the local file path in the database as the receipt URL.
     * In Part 3 (Supabase), this is replaced with a proper cloud upload.
     */
    suspend fun saveReceiptLocally(expenseId: String, imageFile: File): Result<String> = try {
        val dir  = File(imageFile.parentFile, "receipts").also { it.mkdirs() }
        val dest = File(dir, "$expenseId.jpg")
        imageFile.copyTo(dest, overwrite = true)
        expenseDao.updateReceiptUrl(expenseId, dest.absolutePath)
        Log.i(TAG, "Receipt saved locally: ${dest.absolutePath}")
        Result.success(dest.absolutePath)
    } catch (e: Exception) {
        Log.e(TAG, "saveReceiptLocally failed", e); Result.failure(e)
    }

    /*
    Title: Android — Reading and writing files to internal storage
    Author(s): Android Developers
    Date: 2024
    Version: N/A
    Type: Documentation
    Availability: https://developer.android.com/training/data-storage/app-specific#kotlin
    */
}


class LocalBudgetRepository(context: Context) {

    private val budgetGoalDao = AppDatabase.getInstance(context).budgetGoalDao()
    private val userId get()  = LocalUserSession.userId

    suspend fun getBudgetGoal(month: Int, year: Int): Result<BudgetGoal?> = try {
        Result.success(budgetGoalDao.getBudgetGoal(userId, month, year)?.toDomain())
    } catch (e: Exception) {
        Log.e(TAG, "getBudgetGoal failed", e); Result.failure(e)
    }

    suspend fun upsertBudgetGoal(goal: BudgetGoal): Result<BudgetGoal> = try {
        val entity = goal.copy(userId = userId).toEntity()
        budgetGoalDao.upsertBudgetGoal(entity)
        Result.success(entity.toDomain())
    } catch (e: Exception) {
        Log.e(TAG, "upsertBudgetGoal failed", e); Result.failure(e)
    }
}


class LocalSavingsRepository(context: Context) {

    private val savingsDao  = AppDatabase.getInstance(context).savingsGoalDao()
    private val userId get() = LocalUserSession.userId

    suspend fun getSavingsGoals(): Result<List<SavingsGoal>> = try {
        Result.success(savingsDao.getSavingsGoals(userId).map { it.toDomain() })
    } catch (e: Exception) {
        Log.e(TAG, "getSavingsGoals failed", e); Result.failure(e)
    }

    suspend fun createSavingsGoal(goal: SavingsGoal): Result<SavingsGoal> = try {
        val entity = goal.copy(userId = userId).toEntity()
        savingsDao.insertSavingsGoal(entity)
        Result.success(entity.toDomain())
    } catch (e: Exception) {
        Log.e(TAG, "createSavingsGoal failed", e); Result.failure(e)
    }

    suspend fun addContribution(goalId: String, amount: Double): Result<SavingsGoal> = try {
        val goals   = savingsDao.getSavingsGoals(userId)
        val current = goals.find { it.id == goalId }
            ?: return Result.failure(Exception("Savings goal not found: $goalId"))
        val newAmount = (current.currentAmount + amount).coerceAtLeast(0.0)
        savingsDao.updateCurrentAmount(goalId, userId, newAmount)
        Result.success(current.copy(currentAmount = newAmount).toDomain())
    } catch (e: Exception) {
        Log.e(TAG, "addContribution failed", e); Result.failure(e)
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> = try {
        savingsDao.deleteSavingsGoal(goalId, userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "deleteSavingsGoal failed", e); Result.failure(e)
    }
}


class LocalRecurringRepository(context: Context) {

    private val recurringDao = AppDatabase.getInstance(context).recurringDao()
    private val userId get() = LocalUserSession.userId

    suspend fun getRecurringTransactions(): Result<List<RecurringTransaction>> = try {
        Result.success(recurringDao.getRecurringTransactions(userId).map { it.toDomain() })
    } catch (e: Exception) {
        Log.e(TAG, "getRecurring failed", e); Result.failure(e)
    }

    suspend fun createRecurring(rt: RecurringTransaction): Result<RecurringTransaction> = try {
        val entity = rt.copy(userId = userId).toEntity()
        recurringDao.insertRecurring(entity)
        Result.success(entity.toDomain())
    } catch (e: Exception) {
        Log.e(TAG, "createRecurring failed", e); Result.failure(e)
    }

    suspend fun toggleActive(id: String, isActive: Boolean): Result<Unit> = try {
        recurringDao.setActive(id, userId, isActive)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "toggleActive failed", e); Result.failure(e)
    }

    suspend fun deleteRecurring(id: String): Result<Unit> = try {
        recurringDao.deleteRecurring(id, userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "deleteRecurring failed", e); Result.failure(e)
    }

    suspend fun getMonthlyRecurringTotal(): Result<Double> =
        getRecurringTransactions().map { list ->
            list.filter { it.isActive }.sumOf { t ->
                when (t.frequency) {
                    "weekly" -> t.amount * 4.33
                    "yearly" -> t.amount / 12.0
                    else     -> t.amount
                }
            }
        }
}


object LocalUserSession {
    var userId: String    = ""
    var userEmail: String = ""
    val isLoggedIn: Boolean get() = userId.isNotEmpty()
    fun clear() { userId = ""; userEmail = "" }
}

/*
Title: Android architecture — Repository pattern for data access abstraction
Author(s): Android Developers
Date: 2024
Version: N/A
Type: Documentation
Availability: https://developer.android.com/topic/architecture/data-layer
*/