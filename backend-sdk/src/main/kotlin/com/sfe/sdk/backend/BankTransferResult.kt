package com.sfe.sdk.backend

data class BankTransferResult(
    val transactionId: String,
    val status: PaymentStatus, // Reusing PaymentStatus enum
    val message: String? = null,
    val bankReference: String? = null
) {
    val isSuccessful: Boolean
        get() = status == PaymentStatus.SUCCESS || status == PaymentStatus.COMPLETED
}
