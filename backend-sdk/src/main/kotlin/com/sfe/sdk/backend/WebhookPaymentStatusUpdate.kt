package com.sfe.sdk.backend

data class WebhookPaymentStatusUpdate(
    val transactionId: String,
    val status: PaymentStatus, // Using the existing PaymentStatus enum
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val additionalInfo: Map<String, String>? = null
)
