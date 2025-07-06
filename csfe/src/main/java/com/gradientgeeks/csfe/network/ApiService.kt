package com.gradientgeeks.csfe.network

import com.gradientgeeks.csfe.auth.LoginData
import com.gradientgeeks.csfe.auth.LoginResult
import com.gradientgeeks.csfe.auth.RegistrationData
import com.gradientgeeks.csfe.auth.RegistrationResult
import com.gradientgeeks.csfe.payment.PaymentRequest
import com.gradientgeeks.csfe.payment.PaymentResult
import com.gradientgeeks.csfe.qr.QRGenerationRequest
import com.gradientgeeks.csfe.qr.QRGenerationResult
import com.gradientgeeks.csfe.transaction.TransactionFilter
import com.gradientgeeks.csfe.transaction.TransactionHistoryResult
import com.gradientgeeks.csfe.wallet.WalletBalanceResult
import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit API service interface for SFE Backend communication.
 */
interface ApiService {
    
    /**
     * User registration endpoint.
     */
    @POST("api/users/register")
    fun registerUser(@Body registrationData: RegistrationData): Call<RegistrationResult>
    
    /**
     * User login endpoint.
     */
    @POST("api/auth/login")
    fun loginUser(@Body loginData: LoginData): Call<LoginResult>
    
    /**
     * Initiate payment endpoint.
     */
    @POST("api/payments/initiate")
    fun initiatePayment(
        @Header("Authorization") token: String,
        @Body paymentRequest: PaymentRequest
    ): Call<PaymentResult>
    
    /**
     * Check payment status endpoint.
     */
    @GET("api/payments/{transactionId}/status")
    fun checkPaymentStatus(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: String
    ): Call<PaymentResult>
    
    /**
     * Get transaction history endpoint.
     */
    @POST("api/transactions/history")
    fun getTransactionHistory(
        @Header("Authorization") token: String,
        @Body filter: TransactionFilter
    ): Call<TransactionHistoryResult>
    
    /**
     * Get wallet balance endpoint.
     */
    @GET("api/wallet/balance")
    fun getWalletBalance(
        @Header("Authorization") token: String
    ): Call<WalletBalanceResult>
    
    /**
     * Generate QR code endpoint.
     */
    @POST("api/qr/generate")
    fun generateQRCode(
        @Header("Authorization") token: String,
        @Body request: QRGenerationRequest
    ): Call<QRGenerationResult>
}