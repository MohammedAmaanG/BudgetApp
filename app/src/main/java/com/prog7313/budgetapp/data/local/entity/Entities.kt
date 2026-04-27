package com.prog7313.application001.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────────────────────
//  Room Entity: categories
//
//  Column names mirror the Supabase schema exactly (snake_case).
//  This makes it easy to migrate or sync data between local and remote
//  without any field name transformation.
// ─────────────────────────────────────────────────────────────────────────────

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")      val userId: String = "",
    val name: String = "",
    val icon: String = "💰",
    val color: String = "#4A90D9",
    @ColumnInfo(name = "budget_limit") val budgetLimit: Double = 0.0,
    @ColumnInfo(name = "created_at")   val createdAt: String = ""
)

/*
Title: Room — Define entities (database tables)
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/defining-data
*/

// ─────────────────────────────────────────────────────────────────────────────
//  Room Entity: expenses
// ─────────────────────────────────────────────────────────────────────────────

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")     val userId: String = "",
    @ColumnInfo(name = "category_id") val categoryId: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val description: String = "",
    @ColumnInfo(name = "receipt_url") val receiptUrl: String? = null,
    @ColumnInfo(name = "created_at")  val createdAt: String = ""
)

/*
Title: Room — @PrimaryKey and @ColumnInfo annotations
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/reference/androidx/room/PrimaryKey
*/

// ─────────────────────────────────────────────────────────────────────────────
//  Room Entity: budget_goals
// ─────────────────────────────────────────────────────────────────────────────

@Entity(tableName = "budget_goals")
data class BudgetGoalEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")      val userId: String = "",
    val month: Int = 1,
    val year: Int = 2024,
    @ColumnInfo(name = "min_amount")   val minAmount: Double = 0.0,
    @ColumnInfo(name = "max_amount")   val maxAmount: Double = 0.0,
    @ColumnInfo(name = "total_budget") val totalBudget: Double = 0.0,
    @ColumnInfo(name = "created_at")   val createdAt: String = ""
)

// ─────────────────────────────────────────────────────────────────────────────
//  Room Entity: savings_goals  (Extra Feature 1)
// ─────────────────────────────────────────────────────────────────────────────

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")        val userId: String = "",
    val name: String = "",
    @ColumnInfo(name = "target_amount")  val targetAmount: Double = 0.0,
    @ColumnInfo(name = "current_amount") val currentAmount: Double = 0.0,
    val deadline: String? = null,
    val icon: String = "🎯",
    val color: String = "#4CAF50",
    @ColumnInfo(name = "created_at")     val createdAt: String = ""
)

// ─────────────────────────────────────────────────────────────────────────────
//  Room Entity: recurring_transactions  (Extra Feature 2)
// ─────────────────────────────────────────────────────────────────────────────

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "user_id")     val userId: String = "",
    @ColumnInfo(name = "category_id") val categoryId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val frequency: String = "monthly",
    @ColumnInfo(name = "next_date")   val nextDate: String = "",
    @ColumnInfo(name = "is_active")   val isActive: Boolean = true,
    val icon: String = "🔄",
    @ColumnInfo(name = "created_at")  val createdAt: String = ""
)

/*
Title: Room — Database table naming and column naming conventions
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room
*/