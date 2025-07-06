package com.gradientgeeks.csfe.payment

import android.content.Context
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.network.NetworkManager
import com.gradientgeeks.csfe.utils.Logger
import java.util.Date

/**
 * Handles all payment-related operations.
 */
class PaymentModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "PaymentModule"
    private val networkManager = NetworkManager(config)
    
    /**
     * Initiates a payment transaction.
     * 
     * @param request The payment request details
     * @param authToken Authentication token from biometric verification
     * @param callback Callback with the payment result
     */
    fun initiatePayment(
        request: PaymentRequest,
        authToken: String? = null,
        callback: (PaymentResult) -> Unit
    ) {
        Logger.d(TAG, "Initiating payment: ${request.amount} to ${request.recipientVPA}")
        
        // Validate payment request
        val validationResult = validatePaymentRequest(request)
        if (validationResult != null) {
            callback(validationResult)
            return
        }
        
        // For hackathon demo, return mock response based on configuration
        if (config.enableMockPayments) {
            Logger.d(TAG, "Returning mock payment success response")
            callback(createMockSuccessResponse(request))
            return
        }
        
        // Make actual network call to backend
        if (authToken == null) {
            callback(
                PaymentResult.Error(
                    errorCode = "MISSING_AUTH_TOKEN",
                    errorMessage = "Authentication token is required for payment",
                    isRetryable = false
                )
            )
            return
        }
        
        networkManager.executeRequest(
            apiCall = {
                networkManager.apiService.initiatePayment("Bearer $authToken", request)
            },
            onSuccess = { result ->
                Logger.d(TAG, "Payment initiated successfully")
                callback(result)
            },
            onError = { errorMessage, errorCode ->
                Logger.e(TAG, "Payment initiation failed: $errorMessage")
                callback(
                    PaymentResult.Error(
                        errorCode = errorCode,
                        errorMessage = errorMessage,
                        isRetryable = errorCode == "NETWORK_ERROR"
                    )
                )
            }
        )
    }
    
    /**
     * Check the status of a pending payment.
     * 
     * @param transactionId The ID of the transaction to check
     * @param authToken Authentication token
     * @param callback Callback with the payment status
     */
    fun checkPaymentStatus(
        transactionId: String,
        authToken: String? = null,
        callback: (PaymentResult) -> Unit
    ) {
        Logger.d(TAG, "Checking payment status for transaction: $transactionId")
        
        // For mock payments, simulate status check
        if (config.enableMockPayments) {
            val isComplete = Math.random() > 0.3
            
            if (isComplete) {
                callback(
                    PaymentResult.Success(
                        transactionId = transactionId,
                        amount = 100.0,
                        timestamp = Date(),
                        recipient = "demo@upi"
                    )
                )
            } else {
                callback(
                    PaymentResult.Pending(
                        transactionId = transactionId,
                        estimatedCompletionTime = Date(System.currentTimeMillis() + 300000) // +5 min
                    )
                )
            }
            return
        }
        
        // Make actual network call to backend
        if (authToken == null) {
            callback(
                PaymentResult.Error(
                    errorCode = "MISSING_AUTH_TOKEN",
                    errorMessage = "Authentication token is required to check payment status",
                    isRetryable = false
                )
            )
            return
        }
        
        networkManager.executeRequest(
            apiCall = {
                networkManager.apiService.checkPaymentStatus("Bearer $authToken", transactionId)
            },
            onSuccess = { result ->
                Logger.d(TAG, "Payment status retrieved successfully")
                callback(result)
            },
            onError = { errorMessage, errorCode ->
                Logger.e(TAG, "Payment status check failed: $errorMessage")
                callback(
                    PaymentResult.Error(
                        errorCode = errorCode,
                        errorMessage = errorMessage,
                        isRetryable = errorCode == "NETWORK_ERROR"
                    )
                )
            }
        )
    }
    
    /**
     * Validate payment request before processing.
     */
    private fun validatePaymentRequest(request: PaymentRequest): PaymentResult.Error? {
        return when {
            request.amount <= 0 -> PaymentResult.Error(
                errorCode = "INVALID_AMOUNT",
                errorMessage = "Payment amount must be greater than zero",
                isRetryable = false
            )
            request.amount > 200000 -> PaymentResult.Error(
                errorCode = "AMOUNT_LIMIT_EXCEEDED",
                errorMessage = "Payment amount exceeds maximum limit of ₹2,00,000",
                isRetryable = false
            )
            request.recipientVPA.isNullOrBlank() && request.recipientMobile.isNullOrBlank() -> PaymentResult.Error(
                errorCode = "INVALID_RECIPIENT",
                errorMessage = "Recipient VPA or mobile number is required",
                isRetryable = false
            )
            request.recipientVPA?.let { !isValidVPA(it) } == true -> PaymentResult.Error(
                errorCode = "INVALID_VPA",
                errorMessage = "Invalid VPA format",
                isRetryable = false
            )
            request.recipientMobile?.let { !isValidMobile(it) } == true -> PaymentResult.Error(
                errorCode = "INVALID_MOBILE",
                errorMessage = "Invalid mobile number format",
                isRetryable = false
            )
            else -> null
        }
    }
    
    /**
     * Create mock success response for testing.
     */
    private fun createMockSuccessResponse(request: PaymentRequest): PaymentResult.Success {
        return PaymentResult.Success(
            transactionId = "txn_${System.currentTimeMillis()}",
            amount = request.amount,
            timestamp = Date(),
            recipient = request.recipientVPA ?: request.recipientMobile ?: "unknown"
        )
    }
    
    /**
     * Validate VPA format.
     */
    private fun isValidVPA(vpa: String): Boolean {
        val vpaPattern = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$"
        return vpa.matches(vpaPattern.toRegex())
    }
    
    /**
     * Validate mobile number format.
     */
    private fun isValidMobile(mobile: String): Boolean {
        val mobilePattern = "^[6-9]\\d{9}$"
        return mobile.matches(mobilePattern.toRegex())
    }
}
