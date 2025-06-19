package com.sfe.sdk.backend

data class PaymentResponse(
    val transactionId: String?,
    val status: PaymentStatus,
    val message: String?,
    val isBlocked: Boolean = false,
    val errorCode: String? = null
) {
    companion object {
        fun error(message: String, errorCode: String? = null): PaymentResponse {
            return PaymentResponse(
                transactionId = null,
                status = PaymentStatus.FAILED,
                message = message,
                errorCode = errorCode
            )
        }

        fun blocked(transactionId: String?, message: String, errorCode: String? = null): PaymentResponse {
            return PaymentResponse(
                transactionId = transactionId,
                status = PaymentStatus.FAILED, // Or a specific BLOCKED status if added to PaymentStatus enum
                message = message,
                isBlocked = true,
                errorCode = errorCode
            )
        }
    }
}
