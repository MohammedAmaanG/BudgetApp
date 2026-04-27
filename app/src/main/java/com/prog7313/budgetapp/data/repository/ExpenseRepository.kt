package com.prog7313.application001.data.repository

import android.util.Log
import com.prog7313.application001.data.model.Category
import com.prog7313.application001.data.model.DailySpending
import com.prog7313.application001.data.model.Expense
import com.prog7313.application001.data.remote.AirtableClient
import com.prog7313.application001.data.remote.AirtableCreateRequest
import com.prog7313.application001.data.remote.AirtableRecordRequest
import com.prog7313.application001.data.remote.SupabaseSession
import com.prog7313.application001.data.remote.supabaseDbApi
import com.prog7313.application001.data.remote.uploadToStorage
import java.io.File

private const val TAG = "ExpenseRepository"


class ExpenseRepository {

    private val userId get() = SupabaseSession.userId


    suspend fun getCategories(): Result<List<Category>> = try {
        val resp = supabaseDbApi.getCategories(userIdFilter = "eq.$userId")
        if (resp.isSuccessful) Result.success(resp.body() ?: emptyList())
        else Result.failure(Exception("getCategories HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "getCategories failed", e); Result.failure(e)
    }

    suspend fun createCategory(category: Category): Result<Category> {
        return try {
            val resp = supabaseDbApi.createCategory(category.copy(userId = userId))
            if (resp.isSuccessful) {
                Result.success(resp.body()?.firstOrNull()
                    ?: return Result.failure(Exception("No row returned")))
            } else Result.failure(Exception("createCategory HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "createCategory failed", e); Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = try {
        val resp = supabaseDbApi.deleteCategory(
            idFilter     = "eq.$categoryId",
            userIdFilter = "eq.$userId"
        )
        if (resp.isSuccessful || resp.code() == 204) Result.success(Unit)
        else Result.failure(Exception("deleteCategory HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "deleteCategory failed", e); Result.failure(e)
    }

    suspend fun getExpenses(
        fromDate: String? = null,
        toDate: String? = null
    ): Result<List<Expense>> = try {

        val dateFilters = buildList {
            if (fromDate != null) add("gte.$fromDate")
            if (toDate   != null) add("lte.$toDate")
        }.ifEmpty { null }

        val resp = supabaseDbApi.getExpenses(
            userIdFilter = "eq.$userId",
            dateFilters  = dateFilters
        )
        if (resp.isSuccessful) Result.success(resp.body() ?: emptyList())
        else Result.failure(Exception("getExpenses HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "getExpenses failed", e); Result.failure(e)
    }

    suspend fun createExpense(expense: Expense): Result<Expense> = try {
        val resp = supabaseDbApi.createExpense(expense.copy(userId = userId))
        if (resp.isSuccessful) {
            val created = resp.body()?.firstOrNull()
                ?: return Result.failure(Exception("No row returned"))
            syncToAirtable(created)
            Result.success(created)
        } else Result.failure(Exception("createExpense HTTP ${resp.code()}"))
    } catch (e: Exception) {
        Log.e(TAG, "createExpense failed", e); Result.failure(e)
    }

    suspend fun updateExpense(expense: Expense): Result<Expense> {
        return try {
            val resp = supabaseDbApi.updateExpense(
                idFilter = "eq.${expense.id}",
                body     = expense
            )
            if (resp.isSuccessful) {
                Result.success(resp.body()?.firstOrNull()
                    ?: return Result.failure(Exception("No row returned")))
            } else Result.failure(Exception("updateExpense HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Log.e(TAG, "updateExpense failed", e); Result.failure(e)
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = try {
        val resp = supabaseDbApi.deleteExpense(
            idFilter     = "eq.$expenseId",
            userIdFilter = "eq.$userId"
        )
        if (resp.isSuccessful || resp.code() == 204) Result.success(Unit)
        else Result.failure(Exception("deleteExpense HTTP ${resp.code()}"))
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


    suspend fun uploadReceipt(expenseId: String, imageFile: File): Result<String> =
        uploadToStorage("receipts", "$userId/$expenseId.jpg", imageFile.readBytes())

    private suspend fun syncToAirtable(expense: Expense) {
        try {
            AirtableClient.service.createRecords(
                baseId = AirtableClient.baseId, table = "Expenses",
                body = AirtableCreateRequest(listOf(AirtableRecordRequest(mapOf(
                    "ExpenseId"   to expense.id,
                    "UserId"      to expense.userId,
                    "Amount"      to expense.amount,
                    "Date"        to expense.date,
                    "Description" to expense.description,
                    "CategoryId"  to expense.categoryId
                ))))
            )
        } catch (e: Exception) {
            Log.w(TAG, "Airtable sync skipped: ${e.message}")
        }
    }
}