package com.sfe.sdk.backend

data class PaymentRequest(
    val amount: Double,
    val currency: String,
    val recipientId: String,
    val senderId: String
    // Add more fields as needed
)
