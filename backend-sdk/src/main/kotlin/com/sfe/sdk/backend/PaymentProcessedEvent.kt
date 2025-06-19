package com.sfe.sdk.backend

// Assuming this is an internal event or a parameter
data class PaymentProcessedEvent(
    val transactionId: String,
    val status: PaymentStatus,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val processorReference: String? = null
)
