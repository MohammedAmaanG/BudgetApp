package com.prog7313.budgetapp.data.repository

import android.util.Log
import com.prog7313.budgetapp.data.remote.AuthRequest
import com.prog7313.budgetapp.data.remote.SupabaseSession
import com.prog7313.budgetapp.data.remote.supabaseAuthApi

private const val TAG = "AuthRepository"

class AuthRepository {


    val currentUserId: String get() = SupabaseSession.userId
    val isLoggedIn: Boolean   get() = SupabaseSession.isLoggedIn


    suspend fun register(userEmail: String, userPassword: String): Result<String> {
        return try {
            val response = supabaseAuthApi.signUp(AuthRequest(userEmail, userPassword))
            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.accessToken
                val userId = body?.user?.id
                if (token != null && userId != null) {
                    SupabaseSession.accessToken = token
                    SupabaseSession.userId      = userId
                    SupabaseSession.userEmail   = userEmail
                    Log.i(TAG, "Registered: $userId")
                    Result.success(userId)
                } else {
                   Log.i(TAG, "Registration submitted (email confirmation may be required)")
                    Result.success("pending_confirmation")
                }
            } else {
                val errBody = response.errorBody()?.string()
                Log.e(TAG, "register HTTP ${response.code()}: $errBody")
                Result.failure(Exception(parseSupabaseError(errBody, response.code())))
            }
        } catch (e: Exception) {
            Log.e(TAG, "register failed", e)
            Result.failure(e)
        }
    }

    suspend fun login(userEmail: String, userPassword: String): Result<String> {
        return try {
            val response = supabaseAuthApi.signIn(body = AuthRequest(userEmail, userPassword))
            if (response.isSuccessful) {
                val body  = response.body()
                val token = body?.accessToken
                val userId = body?.user?.id
                if (token != null && userId != null) {
                    SupabaseSession.accessToken = token
                    SupabaseSession.userId      = userId
                    SupabaseSession.userEmail   = userEmail
                    Log.i(TAG, "Logged in: $userId")
                    Result.success(userId)
                } else {
                    Result.failure(Exception("Login response missing token"))
                }
            } else {
                val errBody = response.errorBody()?.string()
                Log.e(TAG, "login HTTP ${response.code()}: $errBody")
                Result.failure(Exception(parseSupabaseError(errBody, response.code())))
            }
        } catch (e: Exception) {
            Log.e(TAG, "login failed", e)
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            supabaseAuthApi.signOut()
            SupabaseSession.clear()
            Log.i(TAG, "Signed out")
            Result.success(Unit)
        } catch (e: Exception) {
            SupabaseSession.clear()     // always clear locally
            Log.e(TAG, "logout failed (session cleared anyway)", e)
            Result.success(Unit)
        }
    }



    fun restoreSession(): Boolean = SupabaseSession.isLoggedIn

    private fun parseSupabaseError(body: String?, code: Int): String {
        if (body == null) return "Request failed (HTTP $code)"
        return try {
            // Supabase error bodies look like: {"error":"invalid_grant","error_description":"..."}
            val desc = body.substringAfter("\"error_description\":\"", "")
                .substringBefore("\"", "")
            val msg  = body.substringAfter("\"message\":\"", "")
                .substringBefore("\"", "")
            when {
                desc.isNotBlank() -> desc
                msg.isNotBlank()  -> msg
                else              -> "Request failed (HTTP $code)"
            }
        } catch (e: Exception) {
            "Request failed (HTTP $code)"
        }
    }
}