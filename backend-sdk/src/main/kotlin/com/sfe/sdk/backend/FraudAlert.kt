package com.sfe.sdk.backend

data class FraudAlert(
    val alertId: String,
    val transactionId: String?,
    val userId: String?,
    val reason: String,
    val severity: FraudSeverity, // Enum to be created
    val timestamp: Long
)
