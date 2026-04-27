package com.prog7313.application001.data.repository

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.prog7313.application001.data.local.AppDatabase
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "LocalAuthRepository"


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String
)

/*
Title: Room — Storing user credentials securely (hashed passwords)
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/defining-data
*/


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?
}

/*
Title: Room — @Dao interface and query annotations
Author(s): Android Developers
Date: 2024
Version: Room 2.6.1
Type: Documentation
Availability: https://developer.android.com/training/data-storage/room/accessing-data
*/


class LocalAuthRepository(private val context: Context) {

    private val userDao: UserDao by lazy {
        AppDatabase.getInstance(context).let {
            it.userDao()
        }
    }

    val currentUserId: String get() = LocalUserSession.userId
    val isLoggedIn: Boolean   get() = LocalUserSession.isLoggedIn


    suspend fun register(userEmail: String, userPassword: String): Result<String> {
        return try {
            val existing = userDao.findByEmail(userEmail.trim().lowercase())
            if (existing != null) {
                return Result.failure(Exception("An account with this email already exists"))
            }
            val id   = UUID.randomUUID().toString()
            val hash = sha256(userPassword)
            userDao.insertUser(UserEntity(id = id, email = userEmail.trim().lowercase(), passwordHash = hash))
            LocalUserSession.userId    = id
            LocalUserSession.userEmail = userEmail
            Log.i(TAG, "Local user registered: $id")
            Result.success(id)
        } catch (e: Exception) {
            Log.e(TAG, "register failed", e)
            Result.failure(Exception("Registration failed: ${e.message}"))
        }
    }

    suspend fun login(userEmail: String, userPassword: String): Result<String> {
        return try {
            val user = userDao.findByEmail(userEmail.trim().lowercase())
                ?: return Result.failure(Exception("No account found for this email"))
            if (user.passwordHash != sha256(userPassword)) {
                return Result.failure(Exception("Incorrect password"))
            }
            LocalUserSession.userId    = user.id
            LocalUserSession.userEmail = user.email
            Log.i(TAG, "Local user logged in: ${user.id}")
            Result.success(user.id)
        } catch (e: Exception) {
            Log.e(TAG, "login failed", e)
            Result.failure(e)
        }
    }


    fun logout(): Result<Unit> {
        LocalUserSession.clear()
        Log.i(TAG, "Local session cleared")
        return Result.success(Unit)
    }

    fun restoreSession(): Boolean = LocalUserSession.isLoggedIn


    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes  = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /*
    Title: MessageDigest — SHA-256 hashing in Java/Kotlin
    Author(s): Oracle / Java SE
    Date: 2024
    Version: Java 11
    Type: API Documentation
    Availability: https://docs.oracle.com/en/java/api/java.base/java/security/MessageDigest.html
    */

    /*
    Title: Android security best practices — Storing credentials
    Author(s): Android Developers
    Date: 2024
    Version: N/A
    Type: Documentation
    Availability: https://developer.android.com/topic/security/best-practices
    */
}