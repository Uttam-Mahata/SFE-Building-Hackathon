package com.sfe.sdk.backend

data class PaymentProcessingResult(
    val transactionId: String,
    val status: PaymentStatus,
    val message: String? = null
) {
    companion object {
        fun success(transactionId: String, message: String? = "Payment processed successfully"): PaymentProcessingResult {
            return PaymentProcessingResult(
                transactionId = transactionId,
                status = PaymentStatus.SUCCESS, // Or COMPLETED
                message = message
            )
        }
         fun failure(transactionId: String, message: String? = "Payment failed"): PaymentProcessingResult {
            return PaymentProcessingResult(
                transactionId = transactionId,
                status = PaymentStatus.FAILED,
                message = message
            )
        }
    }
}
