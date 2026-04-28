package com.prog7313.budgetapp.data.repository

import android.util.Log
import com.prog7313.budgetapp.data.model.BudgetGoal
import com.prog7313.budgetapp.data.model.RecurringTransaction
import com.prog7313.budgetapp.data.model.SavingsGoal
import com.prog7313.budgetapp.data.remote.ActiveUpdate
import com.prog7313.budgetapp.data.remote.SavingsAmountUpdate
import com.prog7313.budgetapp.data.remote.SupabaseSession
import com.prog7313.budgetapp.data.remote.supabaseDbApi

private const val TAG = "Repositories"

class BudgetRepository {

    private val userId get() = SupabaseSession.userId

    suspend fun getBudgetGoal(month: Int, year: Int): Result<BudgetGoal?> = try {
        val resp = supabaseDbApi.getBudgetGoals(
            userIdFilter = "eq.$userId",
            monthFilter  = "eq.$month",
            yearFilter   = "eq.$year"
        )
        if (resp.isSuccessful) Result.success(resp.body()?.firstOrNull())
        else Result.failure(Exception("getBudgetGoal HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "getBudgetGoal failed", e); Result.failure(e)
    }

    suspend fun upsertBudgetGoal(goal: BudgetGoal): Result<BudgetGoal> {
        return try {
            val resp = supabaseDbApi.upsertBudgetGoal(goal.copy(userId = userId))
            if (resp.isSuccessful) {
                Result.success(resp.body()?.firstOrNull()
                    ?: return Result.failure(Exception("No row returned")))
            } else Result.failure(Exception("upsertBudgetGoal HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "upsertBudgetGoal failed", e); Result.failure(e)
        }
    }
}


class SavingsRepository {

    private val userId get() = SupabaseSession.userId

    suspend fun getSavingsGoals(): Result<List<SavingsGoal>> = try {
        val resp = supabaseDbApi.getSavingsGoals(userIdFilter = "eq.$userId")
        if (resp.isSuccessful) Result.success(resp.body() ?: emptyList())
        else Result.failure(Exception("getSavingsGoals HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "getSavingsGoals failed", e); Result.failure(e)
    }

    suspend fun createSavingsGoal(goal: SavingsGoal): Result<SavingsGoal> {
        return try {
            val resp = supabaseDbApi.createSavingsGoal(goal.copy(userId = userId))
            if (resp.isSuccessful) {
                Result.success(resp.body()?.firstOrNull()
                    ?: return Result.failure(Exception("No row returned")))
            } else Result.failure(Exception("createSavingsGoal HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "createSavingsGoal failed", e); Result.failure(e)
        }
    }

    suspend fun addContribution(goalId: String, amount: Double): Result<SavingsGoal> {
        return try {
            // Fetch current first, then apply the delta
            val currentResp = supabaseDbApi.getSavingsGoals(userIdFilter = "eq.$userId")
            val current = currentResp.body()?.find { it.id == goalId }
                ?: return Result.failure(Exception("Goal not found"))
            val newAmount = (current.currentAmount + amount).coerceAtLeast(0.0)

            val resp = supabaseDbApi.updateSavingsGoal(
                idFilter     = "eq.$goalId",
                userIdFilter = "eq.$userId",
                body         = SavingsAmountUpdate(newAmount)
            )
            if (resp.isSuccessful) {
                Result.success(resp.body()?.firstOrNull()
                    ?: return Result.failure(Exception("No row returned")))
            } else Result.failure(Exception("addContribution HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "addContribution failed", e); Result.failure(e)
        }
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> = try {
        val resp = supabaseDbApi.deleteSavingsGoal(
            idFilter     = "eq.$goalId",
            userIdFilter = "eq.$userId"
        )
        if (resp.isSuccessful || resp.code() == 204) Result.success(Unit)
        else Result.failure(Exception("deleteSavingsGoal HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "deleteSavingsGoal failed", e); Result.failure(e)
    }
}

class RecurringRepository {

    private val userId get() = SupabaseSession.userId

    suspend fun getRecurringTransactions(): Result<List<RecurringTransaction>> = try {
        val resp = supabaseDbApi.getRecurringTransactions(userIdFilter = "eq.$userId")
        if (resp.isSuccessful) Result.success(resp.body() ?: emptyList())
        else Result.failure(Exception("getRecurring HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "getRecurring failed", e); Result.failure(e)
    }

    suspend fun createRecurring(rt: RecurringTransaction): Result<RecurringTransaction> {
        return try {
            val resp = supabaseDbApi.createRecurring(rt.copy(userId = userId))
            if (resp.isSuccessful) {
                Result.success(resp.body()?.firstOrNull()
                    ?: return Result.failure(Exception("No row returned")))
            } else Result.failure(Exception("createRecurring HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "createRecurring failed", e); Result.failure(e)
        }
    }

    suspend fun toggleActive(id: String, isActive: Boolean): Result<Unit> = try {
        val resp = supabaseDbApi.updateRecurring(
            idFilter     = "eq.$id",
            userIdFilter = "eq.$userId",
            body         = ActiveUpdate(isActive)
        )
        if (resp.isSuccessful || resp.code() == 204) Result.success(Unit)
        else Result.failure(Exception("toggleActive HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "toggleActive failed", e); Result.failure(e)
    }

    suspend fun deleteRecurring(id: String): Result<Unit> = try {
        val resp = supabaseDbApi.deleteRecurring(
            idFilter     = "eq.$id",
            userIdFilter = "eq.$userId"
        )
        if (resp.isSuccessful || resp.code() == 204) Result.success(Unit)
        else Result.failure(Exception("deleteRecurring HTTP ${resp.code()}"))
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