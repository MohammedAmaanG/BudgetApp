package com.prog7313.budgetapp.data.remote

import android.util.Log
import com.prog7313.budgetapp.BuildConfig
import com.prog7313.budgetapp.data.model.*
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

private const val TAG = "SupabaseClient"


object SupabaseSession {
    var accessToken: String = ""
    var userId: String = ""
    var userEmail: String = ""
    val isLoggedIn: Boolean get() = accessToken.isNotEmpty()
    fun clear() { accessToken = ""; userId = ""; userEmail = "" }
}

data class AuthRequest(val email: String, val password: String)

data class AuthResponse(
    @SerializedName("access_token")  val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    val user: UserDto? = null,
    val error: String? = null,
    @SerializedName("error_description") val errorDescription: String? = null
)

data class UserDto(
    val id: String = "",
    val email: String = ""
)


data class SavingsAmountUpdate(val currentAmount: Double)
data class ActiveUpdate(val isActive: Boolean)

interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    suspend fun signUp(@Body body: AuthRequest): Response<AuthResponse>

    @POST("auth/v1/token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: AuthRequest
    ): Response<AuthResponse>

    @POST("auth/v1/logout")
    suspend fun signOut(): Response<Void>
}


interface SupabaseDbApi {

    @GET("rest/v1/categories")
    suspend fun getCategories(
        @Query("user_id") userIdFilter: String,
        @Query("order")   order: String = "name.asc"
    ): Response<List<Category>>

    @POST("rest/v1/categories")
    suspend fun createCategory(
        @Body body: Category,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<Category>>

    @DELETE("rest/v1/categories")
    suspend fun deleteCategory(
        @Query("id")      idFilter: String,
        @Query("user_id") userIdFilter: String
    ): Response<Void>

    @GET("rest/v1/expenses")
    suspend fun getExpenses(
        @Query("user_id") userIdFilter: String,
        @Query("date")    dateFilters: List<String>? = null,
        @Query("order")   order: String = "date.desc"
    ): Response<List<Expense>>

    @POST("rest/v1/expenses")
    suspend fun createExpense(
        @Body body: Expense,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<Expense>>

    @PATCH("rest/v1/expenses")
    suspend fun updateExpense(
        @Query("id")      idFilter: String,
        @Body body: Expense,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<Expense>>

    @DELETE("rest/v1/expenses")
    suspend fun deleteExpense(
        @Query("id")      idFilter: String,
        @Query("user_id") userIdFilter: String
    ): Response<Void>


    @GET("rest/v1/budget_goals")
    suspend fun getBudgetGoals(
        @Query("user_id") userIdFilter: String,
        @Query("month")   monthFilter: String? = null,
        @Query("year")    yearFilter: String? = null
    ): Response<List<BudgetGoal>>

    @POST("rest/v1/budget_goals")
    suspend fun upsertBudgetGoal(
        @Body body: BudgetGoal,
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates"
    ): Response<List<BudgetGoal>>

    @GET("rest/v1/savings_goals")
    suspend fun getSavingsGoals(
        @Query("user_id") userIdFilter: String,
        @Query("order")   order: String = "created_at.desc"
    ): Response<List<SavingsGoal>>

    @POST("rest/v1/savings_goals")
    suspend fun createSavingsGoal(
        @Body body: SavingsGoal,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<SavingsGoal>>

    @PATCH("rest/v1/savings_goals")
    suspend fun updateSavingsGoal(
        @Query("id")      idFilter: String,
        @Query("user_id") userIdFilter: String,
        @Body body: SavingsAmountUpdate,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<SavingsGoal>>

    @DELETE("rest/v1/savings_goals")
    suspend fun deleteSavingsGoal(
        @Query("id")      idFilter: String,
        @Query("user_id") userIdFilter: String
    ): Response<Void>


    @GET("rest/v1/recurring_transactions")
    suspend fun getRecurringTransactions(
        @Query("user_id") userIdFilter: String,
        @Query("order")   order: String = "name.asc"
    ): Response<List<RecurringTransaction>>

    @POST("rest/v1/recurring_transactions")
    suspend fun createRecurring(
        @Body body: RecurringTransaction,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<RecurringTransaction>>

    @PATCH("rest/v1/recurring_transactions")
    suspend fun updateRecurring(
        @Query("id")      idFilter: String,
        @Query("user_id") userIdFilter: String,
        @Body body: ActiveUpdate,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<List<RecurringTransaction>>

    @DELETE("rest/v1/recurring_transactions")
    suspend fun deleteRecurring(
        @Query("id")      idFilter: String,
        @Query("user_id") userIdFilter: String
    ): Response<Void>
}


private val gson by lazy {
    GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .addSerializationExclusionStrategy(object : ExclusionStrategy {
            private val skip = setOf("id", "createdAt")
            override fun shouldSkipField(f: FieldAttributes) = f.name in skip
            override fun shouldSkipClass(clazz: Class<*>) = false
        })
        .create()
}

val sharedOkHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .addInterceptor { chain ->
            // Use the JWT if logged in; fall back to the anon key for auth calls
            val token = SupabaseSession.accessToken.ifEmpty { BuildConfig.SUPABASE_ANON_KEY }
            val req = chain.request().newBuilder()
                .addHeader("apikey",        BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type",  "application/json")
                .build()
            chain.proceed(req)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}

private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl("${BuildConfig.SUPABASE_URL}/")
        .client(sharedOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}


val supabaseAuthApi: SupabaseAuthApi by lazy { retrofit.create(SupabaseAuthApi::class.java) }
val supabaseDbApi:   SupabaseDbApi   by lazy { retrofit.create(SupabaseDbApi::class.java) }

suspend fun uploadToStorage(bucket: String, path: String, imageBytes: ByteArray): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = "${BuildConfig.SUPABASE_URL}/storage/v1/object/$bucket/$path"
            val body = imageBytes.toRequestBody("image/jpeg".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseSession.accessToken}")
                .addHeader("x-upsert", "true")
                .post(body)
                .build()
            val response = sharedOkHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/$bucket/$path"
                Log.i(TAG, "Uploaded to $publicUrl")
                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Storage upload failed: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadToStorage failed", e)
            Result.failure(e)
        }
    }
}