package com.sfe.sdk.backend

data class TransactionLimits(
    val dailyLimit: Double,
    val weeklyLimit: Double,
    val monthlyLimit: Double
)
