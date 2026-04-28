package com.prog7313.budgetapp.data.remote

import com.prog7313.budgetapp.BuildConfig
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


data class AirtableRecord(
    val id: String? = null,
    val fields: Map<String, Any> = emptyMap(),
    @SerializedName("createdTime") val createdTime: String? = null
)

data class AirtableListResponse(
    val records: List<AirtableRecord> = emptyList(),
    val offset: String? = null
)

data class AirtableCreateRequest(
    val records: List<AirtableRecordRequest>
)

data class AirtableRecordRequest(
    val fields: Map<String, Any>
)

data class AirtableUpdateRequest(
    val fields: Map<String, Any>
)


interface AirtableService {

    @GET("{baseId}/{table}")
    suspend fun listRecords(
        @Path("baseId")  baseId: String,
        @Path("table")   table: String,
        @Query("filterByFormula") filterByFormula: String? = null,
        @Query("maxRecords") maxRecords: Int = 100
    ): Response<AirtableListResponse>


    @POST("{baseId}/{table}")
    suspend fun createRecords(
        @Path("baseId") baseId: String,
        @Path("table")  table: String,
        @Body body: AirtableCreateRequest
    ): Response<AirtableListResponse>


    @PATCH("{baseId}/{table}/{recordId}")
    suspend fun updateRecord(
        @Path("baseId")   baseId: String,
        @Path("table")    table: String,
        @Path("recordId") recordId: String,
        @Body body: AirtableUpdateRequest
    ): Response<AirtableRecord>
}


object AirtableClient {

    private const val BASE_URL = "https://api.airtable.com/v0/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.AIRTABLE_API_KEY}")
                    .addHeader("Content-Type",  "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val service: AirtableService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AirtableService::class.java)
    }


    val baseId: String get() = BuildConfig.AIRTABLE_BASE_ID
}