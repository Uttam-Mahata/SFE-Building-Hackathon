package com.sfe.sdk.backend.ops

// It's good practice to add imports for all types from other packages
import com.sfe.sdk.backend.PaymentRequest
import com.sfe.sdk.backend.PaymentResponse
import com.sfe.sdk.backend.RiskLevel
// Assuming SFEValidationException is in com.sfe.sdk.backend.exceptions
import com.sfe.sdk.backend.exceptions.SFEValidationException

interface PaymentOperations {
    fun validateRequest(request: PaymentRequest): Boolean // Simplified, might throw or return a validation result object
    fun performFraudAnalysis(request: PaymentRequest, userId: String): RiskLevel
    fun processTransaction(request: PaymentRequest): PaymentResponse
    fun generateAuditLog(transactionId: String, event: String) // event could be an enum or structured object
    fun validatePaymentRequest(request: PaymentRequest) // Could throw SFEValidationException
    fun checkUserLimits(userId: String, amount: Double): Boolean
    fun verifyRecipient(recipientId: String): Boolean
    fun processUPITransaction(request: PaymentRequest): PaymentResponse // Assuming PaymentRequest can model a UPI tx
}
