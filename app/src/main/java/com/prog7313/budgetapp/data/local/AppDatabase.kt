package com.prog7313.budgetapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.prog7313.budgetapp.data.local.dao.*
import com.prog7313.budgetapp.data.local.entity.*
import com.prog7313.budgetapp.data.repository.UserDao
import com.prog7313.budgetapp.data.repository.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        BudgetGoalEntity::class,
        SavingsGoalEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringDao(): RecurringTransactionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finwise_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

/*
Title: Room — Create and configure the database
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/referencing-data
*/

/*
Title: Room — Migrating Room databases
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/migrating-db-versions
*/

/*
Title: Kotlin — Double-checked locking for thread-safe singleton
Author(s): JetBrains
Date: 2024
Version: Kotlin 2.0
Type: Documentation
Availability: https://kotlinlang.org/docs/object-declarations.html
*/