package com.sfe.paymentbackend.payment.dto

// This DTO is used by the Spring Boot backend's API.
// It will be mapped to com.sfe.sdk.backend.PaymentRequest for SDK interaction.
data class InitiatePaymentRequest(
    val recipientId: String, // e.g., VPA, account number, phone number + MMID
    val amount: Double,
    val currency: String = "INR", // Default currency
    val description: String?,
    // Additional fields if needed by the payment backend before SDK call
    val senderId: String? // This might come from the authenticated UserPrincipal later
)
