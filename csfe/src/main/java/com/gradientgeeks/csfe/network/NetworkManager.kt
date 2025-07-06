package com.gradientgeeks.csfe.network

import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.utils.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Manages network communications for the SFE Client SDK.
 */
class NetworkManager(private val config: SFEConfig) {
    
    private val TAG = "NetworkManager"
    
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectionTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(config.readTimeout.toLong(), TimeUnit.SECONDS)
            .writeTimeout(config.readTimeout.toLong(), TimeUnit.SECONDS)
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(createEncryptionInterceptor())
        
        // Add logging interceptor for debug builds
        if (config.debugMode) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Logger.d(TAG, message)
            }
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }
        
        builder.build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(config.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    /**
     * Create authentication interceptor to add API key to requests.
     */
    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("X-API-Key", config.apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "SFE-Client-SDK/1.0.0")
            
            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }
    
    /**
     * Create encryption interceptor for request/response encryption.
     */
    private fun createEncryptionInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            
            // TODO: Implement request encryption based on config.encryptionLevel
            // For now, just pass through the request
            val response = chain.proceed(original)
            
            // TODO: Implement response decryption
            response
        }
    }
    
    /**
     * Execute a network request safely with error handling.
     */
    inline fun <T> executeRequest(
        crossinline apiCall: () -> retrofit2.Call<T>,
        crossinline onSuccess: (T) -> Unit,
        crossinline onError: (String, String) -> Unit
    ) {
        try {
            val call = apiCall()
            call.enqueue(object : retrofit2.Callback<T> {
                override fun onResponse(call: retrofit2.Call<T>, response: retrofit2.Response<T>) {
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            onSuccess(body)
                        } ?: run {
                            onError("Empty response body", "EMPTY_RESPONSE")
                        }
                    } else {
                        val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                        Logger.e(TAG, errorMessage)
                        onError(errorMessage, "HTTP_ERROR_${response.code()}")
                    }
                }
                
                override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                    val errorMessage = "Network request failed: ${t.message}"
                    Logger.e(TAG, errorMessage)
                    onError(errorMessage, "NETWORK_ERROR")
                }
            })
        } catch (e: Exception) {
            val errorMessage = "Request execution failed: ${e.message}"
            Logger.e(TAG, errorMessage)
            onError(errorMessage, "EXECUTION_ERROR")
        }
    }
    
    /**
     * Check if network is available.
     */
    fun isNetworkAvailable(): Boolean {
        // TODO: Implement network availability check
        return true
    }
    
    /**
     * Get network type (WiFi, Mobile, etc.).
     */
    fun getNetworkType(): String {
        // TODO: Implement network type detection
        return "UNKNOWN"
    }
}

/**
 * Extension function to add Bearer token to authorization header.
 */
fun Request.Builder.addBearerToken(token: String): Request.Builder {
    return this.header("Authorization", "Bearer $token")
}