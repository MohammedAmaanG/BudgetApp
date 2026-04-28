package com.prog7313.budgetapp.data.local.dao

import androidx.room.*
import com.prog7313.budgetapp.data.local.entity.*
import kotlinx.coroutines.flow.Flow


@Dao
interface CategoryDao {

    /** Returns a Flow so the UI automatically updates when categories change. */
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY name ASC")
    fun getCategoriesFlow(userId: String): Flow<List<CategoryEntity>>


    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY name ASC")
    suspend fun getCategories(userId: String): List<CategoryEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id AND user_id = :userId")
    suspend fun deleteCategoryById(id: String, userId: String)
}

/*
Title: Room — Data access objects (DAOs)
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/accessing-data
*/

/*
Title: Room — Write data to a database
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/inserting-data
*/

// ─────────────────────────────────────────────────────────────────────────────
//  ExpenseDao
// ─────────────────────────────────────────────────────────────────────────────

@Dao
interface ExpenseDao {

    @Query("""
        SELECT * FROM expenses
        WHERE user_id = :userId
          AND (:fromDate IS NULL OR date >= :fromDate)
          AND (:toDate   IS NULL OR date <= :toDate)
        ORDER BY date DESC
    """)
    suspend fun getExpenses(
        userId: String,
        fromDate: String? = null,
        toDate: String? = null
    ): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC")
    fun getExpensesFlow(userId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("UPDATE expenses SET receipt_url = :url WHERE id = :id")
    suspend fun updateReceiptUrl(id: String, url: String)

    @Query("DELETE FROM expenses WHERE id = :id AND user_id = :userId")
    suspend fun deleteExpense(id: String, userId: String)
}

/*
Title: Room — Complex queries with multiple optional filters using SQL
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/accessing-data#query
*/

/*
Title: Room — Observe database changes with Flow
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/async-queries#flow
*/


@Dao
interface BudgetGoalDao {

    @Query("""
        SELECT * FROM budget_goals
        WHERE user_id = :userId AND month = :month AND year = :year
        LIMIT 1
    """)
    suspend fun getBudgetGoal(userId: String, month: Int, year: Int): BudgetGoalEntity?

    /**
     * REPLACE acts as upsert: if a goal for the same user/month/year already
     * exists (same id), it is overwritten. If the id changes (new insert), it
     * is inserted fresh.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudgetGoal(goal: BudgetGoalEntity)
}

/*
Title: Room — OnConflictStrategy.REPLACE as an upsert mechanism
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/reference/androidx/room/OnConflictStrategy
*/

// ─────────────────────────────────────────────────────────────────────────────
//  SavingsGoalDao
// ─────────────────────────────────────────────────────────────────────────────

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getSavingsGoals(userId: String): List<SavingsGoalEntity>

    @Query("SELECT * FROM savings_goals WHERE user_id = :userId ORDER BY created_at DESC")
    fun getSavingsGoalsFlow(userId: String): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity)

    @Query("UPDATE savings_goals SET current_amount = :newAmount WHERE id = :id AND user_id = :userId")
    suspend fun updateCurrentAmount(id: String, userId: String, newAmount: Double)

    @Query("DELETE FROM savings_goals WHERE id = :id AND user_id = :userId")
    suspend fun deleteSavingsGoal(id: String, userId: String)
}

/*
Title: Room — @Update and @Query for partial updates
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/updating-data
*/

// ─────────────────────────────────────────────────────────────────────────────
//  RecurringTransactionDao
// ─────────────────────────────────────────────────────────────────────────────

@Dao
interface RecurringTransactionDao {

    @Query("SELECT * FROM recurring_transactions WHERE user_id = :userId ORDER BY name ASC")
    suspend fun getRecurringTransactions(userId: String): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE user_id = :userId ORDER BY name ASC")
    fun getRecurringFlow(userId: String): Flow<List<RecurringTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(rt: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET is_active = :isActive WHERE id = :id AND user_id = :userId")
    suspend fun setActive(id: String, userId: String, isActive: Boolean)

    @Query("DELETE FROM recurring_transactions WHERE id = :id AND user_id = :userId")
    suspend fun deleteRecurring(id: String, userId: String)
}

/*
Title: Room — Suspend functions and coroutines in DAOs
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/async-queries
*/