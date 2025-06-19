package com.sfe.sdk.backend

// Assuming this is an internal event or a parameter for some methods
data class TransactionInitiatedEvent(
    val transactionId: String,
    val userId: String,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val type: String // e.g., "PAYMENT", "TRANSFER"
)
