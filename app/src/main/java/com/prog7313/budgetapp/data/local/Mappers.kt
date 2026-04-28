package com.prog7313.budgetapp.data.local

import com.prog7313.budgetapp.data.local.entity.*
import com.prog7313.budgetapp.data.model.*
import java.util.UUID



fun CategoryEntity.toDomain() = Category(
    id          = id,
    userId      = userId,
    name        = name,
    icon        = icon,
    color       = color,
    budgetLimit = budgetLimit,
    createdAt   = createdAt
)

fun Category.toEntity() = CategoryEntity(
    id          = id.ifEmpty { UUID.randomUUID().toString() },
    userId      = userId,
    name        = name,
    icon        = icon,
    color       = color,
    budgetLimit = budgetLimit,
    createdAt   = createdAt
)

/*
Title: Android architecture — Mapping between data layers
Author(s): Android Developers
Date: 2024
Version: N/A
Type: Documentation
Availability: https://developer.android.com/topic/architecture/data-layer#data-layer-best-practices
*/

// ── Expense ───────────────────────────────────────────────────────────────────

fun ExpenseEntity.toDomain() = Expense(
    id          = id,
    userId      = userId,
    categoryId  = categoryId,
    amount      = amount,
    date        = date,
    description = description,
    receiptUrl  = receiptUrl,
    createdAt   = createdAt
)

fun Expense.toEntity() = ExpenseEntity(
    id          = id.ifEmpty { UUID.randomUUID().toString() },
    userId      = userId,
    categoryId  = categoryId,
    amount      = amount,
    date        = date,
    description = description,
    receiptUrl  = receiptUrl,
    createdAt   = createdAt
)

// ── BudgetGoal ────────────────────────────────────────────────────────────────

fun BudgetGoalEntity.toDomain() = BudgetGoal(
    id          = id,
    userId      = userId,
    month       = month,
    year        = year,
    minAmount   = minAmount,
    maxAmount   = maxAmount,
    totalBudget = totalBudget,
    createdAt   = createdAt
)

fun BudgetGoal.toEntity() = BudgetGoalEntity(
    id          = id.ifEmpty { UUID.randomUUID().toString() },
    userId      = userId,
    month       = month,
    year        = year,
    minAmount   = minAmount,
    maxAmount   = maxAmount,
    totalBudget = totalBudget,
    createdAt   = createdAt
)

// ── SavingsGoal ───────────────────────────────────────────────────────────────

fun SavingsGoalEntity.toDomain() = SavingsGoal(
    id            = id,
    userId        = userId,
    name          = name,
    targetAmount  = targetAmount,
    currentAmount = currentAmount,
    deadline      = deadline,
    icon          = icon,
    color         = color,
    createdAt     = createdAt
)

fun SavingsGoal.toEntity() = SavingsGoalEntity(
    id            = id.ifEmpty { UUID.randomUUID().toString() },
    userId        = userId,
    name          = name,
    targetAmount  = targetAmount,
    currentAmount = currentAmount,
    deadline      = deadline,
    icon          = icon,
    color         = color,
    createdAt     = createdAt
)

// ── RecurringTransaction ──────────────────────────────────────────────────────

fun RecurringTransactionEntity.toDomain() = RecurringTransaction(
    id         = id,
    userId     = userId,
    categoryId = categoryId,
    name       = name,
    amount     = amount,
    frequency  = frequency,
    nextDate   = nextDate,
    isActive   = isActive,
    icon       = icon,
    createdAt  = createdAt
)

fun RecurringTransaction.toEntity() = RecurringTransactionEntity(
    id         = id.ifEmpty { UUID.randomUUID().toString() },
    userId     = userId,
    categoryId = categoryId,
    name       = name,
    amount     = amount,
    frequency  = frequency,
    nextDate   = nextDate,
    isActive   = isActive,
    icon       = icon,
    createdAt  = createdAt
)

/*
Title: Kotlin extension functions as mapper pattern
Author(s): JetBrains
Date: 2024
Version: Kotlin 2.0
Type: Documentation
Availability: https://kotlinlang.org/docs/extensions.html
*/

/*
Title: UUID — Generating unique identifiers for local records
Author(s): Oracle / Java SE
Date: 2024
Version: Java 11
Type: API Documentation
Availability: https://docs.oracle.com/en/java/api/java.base/java/util/UUID.html
*/