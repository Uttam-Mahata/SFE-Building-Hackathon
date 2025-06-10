package com.app.sfpay.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.app.sfpay.api.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Service for communicating with SFE Payment Backend
 * Implements the complete end-to-end flow described in the README
 */
class SfeBackendService(private val context: Context) {
    
    companion object {
        private const val TAG = "SfeBackendService"
        private const val BASE_URL = "http://10.0.2.2:8080/" // Android emulator localhost
        private const val PREFS_NAME = "sfe_backend_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_INFO = "user_info"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().setLenient().create()
    
    private val api: SfeBackendApi by lazy {
        createRetrofit().create(SfeBackendApi::class.java)
    }
    
    private fun createRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Authenticate with SFE backend using security payload
     */
    suspend fun authenticateWithSfePayload(
        username: String, 
        password: String, 
        sfePayload: String?
    ): Result<LoginResponse> {
        return try {
            Log.d(TAG, "Authenticating user: $username with SFE payload")
            
            val request = LoginRequest(
                username = username,
                password = password,
                sfePayload = sfePayload
            )
            
            val response = api.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                if (loginResponse.success && loginResponse.token != null) {
                    // Store authentication token and user info
                    saveAuthToken(loginResponse.token)
                    loginResponse.userInfo?.let { saveUserInfo(it) }
                    
                    Log.d(TAG, "Authentication successful. Risk level: ${loginResponse.securityAssessment?.riskLevel}")
                    Result.success(loginResponse)
                } else {
                    Log.w(TAG, "Authentication failed: ${loginResponse.message}")
                    Result.failure(Exception(loginResponse.message))
                }
            } else {
                val errorMsg = "Authentication failed: HTTP ${response.code()}"
                Log.w(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Initiate payment transaction with SFE security verification
     */
    suspend fun initiatePaymentTransaction(
        amount: Double,
        currency: String,
        recipient: String,
        description: String?,
        sfePayload: String
    ): Result<TransactionResponse> {
        return try {
            val authToken = getAuthToken()
            if (authToken.isNullOrEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val transactionId = generateTransactionId()
            Log.d(TAG, "Initiating payment transaction: $transactionId for $$amount $currency to $recipient")
            
            val request = TransactionRequest(
                transactionId = transactionId,
                amount = amount,
                currency = currency,
                recipient = recipient,
                description = description,
                sfePayload = sfePayload,
                paymentMethod = createMockPaymentMethod()
            )
            
            val response = api.initiateTransaction("Bearer $authToken", request)
            
            if (response.isSuccessful && response.body() != null) {
                val transactionResponse = response.body()!!
                
                Log.d(TAG, "Transaction ${transactionResponse.transactionId} status: ${transactionResponse.status}")
                Log.d(TAG, "Security assessment: ${transactionResponse.securityInfo?.riskLevel}")
                
                Result.success(transactionResponse)
            } else {
                val errorMsg = "Transaction failed: HTTP ${response.code()}"
                Log.w(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Transaction error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get transaction status
     */
    suspend fun getTransactionStatus(transactionId: String): Result<TransactionStatusResponse> {
        return try {
            val authToken = getAuthToken()
            if (authToken.isNullOrEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val response = api.getTransactionStatus("Bearer $authToken", transactionId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get transaction status: HTTP ${response.code()}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get transaction history
     */
    suspend fun getTransactionHistory(limit: Int = 10, offset: Int = 0): Result<TransactionHistoryResponse> {
        return try {
            val authToken = getAuthToken()
            if (authToken.isNullOrEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val response = api.getTransactionHistory("Bearer $authToken", limit, offset)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get transaction history: HTTP ${response.code()}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction history", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check backend health
     */
    suspend fun checkBackendHealth(): Result<HealthResponse> {
        return try {
            val response = api.getHealthStatus()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Backend health check failed: HTTP ${response.code()}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Backend health check error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Demo login with default credentials
     */
    suspend fun demoLogin(sfePayload: String?): Result<LoginResponse> {
        return authenticateWithSfePayload(
            username = "demo@gradientgeeks.com",
            password = "demo123",
            sfePayload = sfePayload
        )
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }
    
    /**
     * Get stored user info
     */
    fun getUserInfo(): UserInfo? {
        val userInfoJson = prefs.getString(KEY_USER_INFO, null)
        return if (userInfoJson != null) {
            try {
                gson.fromJson(userInfoJson, UserInfo::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_INFO)
            .apply()
        Log.d(TAG, "User logged out")
    }
    
    private fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    private fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    private fun saveUserInfo(userInfo: UserInfo) {
        val userInfoJson = gson.toJson(userInfo)
        prefs.edit().putString(KEY_USER_INFO, userInfoJson).apply()
    }
    
    private fun generateTransactionId(): String {
        return "txn_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun createMockPaymentMethod(): PaymentMethod {
        return PaymentMethod(
            type = "CARD",
            lastFourDigits = "4829",
            provider = "Visa",
            token = "card_token_${System.currentTimeMillis()}"
        )
    }
} 