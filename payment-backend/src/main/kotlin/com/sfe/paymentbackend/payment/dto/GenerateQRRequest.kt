package com.sfe.paymentbackend.payment.dto

// This DTO is used by the Spring Boot backend's API.
// It will be mapped to com.sfe.sdk.backend.QRGenerationRequest for SDK interaction.
data class GenerateQRRequest(
    val amount: Double,
    val currency: String = "INR",
    val description: String?,
    val merchantId: String?, // Optional: if QR is for a specific merchant
    val transactionReference: String?, // Optional: a reference for this QR transaction
    val purpose: String?, // Optional: purpose of the QR code
    val expiryMinutes: Long? = null, // Optional: QR expiry time in minutes
    val isDynamic: Boolean = false // Optional: if the QR is dynamic or static
)
