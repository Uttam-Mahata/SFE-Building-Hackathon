package com.gradientgeeks.csfe.fraud

/**
 * Risk levels for transactions.
 */
enum class RiskLevel {
    /**
     * Low risk transaction.
     */
    LOW,
    
    /**
     * Medium risk transaction.
     */
    MEDIUM,
    
    /**
     * High risk transaction.
     */
    HIGH
}

/**
 * Result of risk analysis for a transaction.
 * @deprecated Use FraudDetectionResult instead
 */
@Deprecated("Use FraudDetectionResult instead")
data class RiskAnalysisResult(
    val riskLevel: RiskLevel,
    val reason: String,
    val suggestedAction: String
)
