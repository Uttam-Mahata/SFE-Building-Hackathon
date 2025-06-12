package com.app.sfpay.api

import retrofit2.Response
import retrofit2.http.*

/**
 * API interface for communicating with SFE Payment Backend
 * Implements the endpoints described in the SFE README documentation
 */
interface SfeBackendApi {
    
    /**
     * Login with SFE security assessment
     */
    @POST("payment-app/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    /**
     * Initiate payment transaction with SFE security verification
     */
    @POST("payment-app/api/v1/transactions/initiate")
    suspend fun initiateTransaction(
        @Header("Authorization") authToken: String,
        @Body request: TransactionRequest
    ): Response<TransactionResponse>
    
    /**
     * Get transaction status
     */
    @GET("payment-app/api/v1/transactions/{transactionId}/status")
    suspend fun getTransactionStatus(
        @Header("Authorization") authToken: String,
        @Path("transactionId") transactionId: String
    ): Response<TransactionStatusResponse>
    
    /**
     * Get user transaction history
     */
    @GET("payment-app/api/v1/transactions/history")
    suspend fun getTransactionHistory(
        @Header("Authorization") authToken: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<TransactionHistoryResponse>
    
    /**
     * Health check endpoint
     */
    @GET("payment-app/api/v1/transactions/health")
    suspend fun getHealthStatus(): Response<HealthResponse>
}

/**
 * Data classes for API requests and responses
 */
data class LoginRequest(
    val username: String,
    val password: String,
    val sfePayload: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val tokenType: String?,
    val expiresIn: Long?,
    val userInfo: UserInfo?,
    val securityAssessment: SecurityAssessment?
)

data class UserInfo(
    val userId: String,
    val username: String,
    val email: String,
    val role: String
)

data class SecurityAssessment(
    val riskLevel: String,
    val deviceTrusted: Boolean,
    val assessmentId: String,
    val assessmentTimestamp: Long
)

data class TransactionRequest(
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val recipient: String,
    val description: String?,
    val sfePayload: String,
    val paymentMethod: PaymentMethod?
)

data class PaymentMethod(
    val type: String,
    val lastFourDigits: String?,
    val provider: String?,
    val token: String?
)

data class TransactionResponse(
    val transactionId: String,
    val status: String,
    val message: String,
    val authorizationCode: String?,
    val amount: Double?,
    val currency: String?,
    val recipient: String?,
    val timestamp: Long,
    val securityInfo: SecurityInfo?,
    val trackingReference: String?
)

data class SecurityInfo(
    val riskLevel: String,
    val attestationStatus: String,
    val deviceTrusted: Boolean,
    val securityAssessmentId: String,
    val policyDecision: String
)

data class TransactionStatusResponse(
    val transactionId: String,
    val status: String,
    val timestamp: Long,
    val message: String
)

data class TransactionHistoryResponse(
    val transactions: List<TransactionSummary>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class TransactionSummary(
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val recipient: String,
    val status: String,
    val timestamp: Long
)

data class HealthResponse(
    val status: String,
    val service: String,
    val timestamp: Long
) 