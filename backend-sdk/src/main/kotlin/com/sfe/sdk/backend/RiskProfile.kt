package com.sfe.sdk.backend

// Can be an enum if profiles are predefined, or a data class for more dynamic profiles.
// For now, let's assume a data class for flexibility.
data class RiskProfile(
    val userId: String,
    val riskLevel: RiskLevel, // Using the existing RiskLevel enum
    val reasons: List<String>? = null,
    val lastAssessed: Long // Timestamp
)
