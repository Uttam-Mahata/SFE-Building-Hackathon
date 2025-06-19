package com.sfe.sdk.backend

data class AddMoneyRequest(
    val userId: String,
    val amount: Double,
    val currency: String,
    val paymentMethodId: String? = null // e.g., for saved card, bank account
)
