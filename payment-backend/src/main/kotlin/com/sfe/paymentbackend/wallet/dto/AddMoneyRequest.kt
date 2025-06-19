package com.sfe.paymentbackend.wallet.dto

// This DTO is used by the Spring Boot backend's API for adding money to a wallet.
// It will be mapped to com.sfe.sdk.backend.AddMoneyRequest for SDK interaction.
data class AddMoneyRequest(
    val amount: Double,
    val currency: String = "INR",
    // paymentMethodId could be a token representing a card, a linked bank account ID, etc.
    // This aligns with the SDK's AddMoneyRequest.
    val paymentMethodId: String
)
