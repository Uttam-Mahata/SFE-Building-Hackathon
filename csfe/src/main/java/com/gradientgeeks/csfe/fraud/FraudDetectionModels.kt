package com.gradientgeeks.csfe.fraud

import java.util.Date

/**
 * Represents the result of a fraud detection analysis.
 */
sealed class FraudDetectionResult {
    /**
     * Transaction is allowed to proceed.
     */
    data class Allowed(
        val riskScore: Double,
        val riskLevel: RiskLevel,
        val analysisDetails: String
    ) : FraudDetectionResult()
    
    /**
     * Transaction is blocked due to fraud risk.
     */
    data class Blocked(
        val riskScore: Double,
        val riskLevel: RiskLevel,
        val reason: String,
        val blockReason: BlockReason
    ) : FraudDetectionResult()
    
    /**
     * Transaction requires additional verification.
     */
    data class RequiresVerification(
        val riskScore: Double,
        val riskLevel: RiskLevel,
        val verificationType: VerificationType
    ) : FraudDetectionResult()
    
    /**
     * Error occurred during fraud analysis.
     */
    data class Error(
        val errorMessage: String,
        val errorCode: String
    ) : FraudDetectionResult()
}

/**
 * Fraud analysis request data.
 */
data class FraudAnalysisRequest(
    val transactionId: String,
    val amount: Double,
    val recipientId: String,
    val userId: String,
    val deviceId: String,
    val timestamp: Date,
    val transactionType: TransactionType,
    val location: Location? = null,
    val deviceInfo: DeviceInfo? = null,
    val behaviorMetrics: BehaviorMetrics? = null
)

/**
 * Device information for fraud analysis.
 */
data class DeviceInfo(
    val deviceId: String,
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String,
    val isRooted: Boolean,
    val isEmulator: Boolean,
    val screenResolution: String,
    val simCountry: String?
)

/**
 * Location information for fraud analysis.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Date,
    val country: String?,
    val city: String?
)

/**
 * User behavior metrics for fraud analysis.
 */
data class BehaviorMetrics(
    val typingSpeed: Double,
    val touchPressure: Float,
    val touchArea: Float,
    val deviceOrientation: String,
    val timeSpentOnScreen: Long,
    val navigationPattern: String?
)

/**
 * Risk levels for fraud detection.
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Reasons for blocking a transaction.
 */
enum class BlockReason {
    HIGH_RISK_SCORE,
    VELOCITY_LIMIT_EXCEEDED,
    SUSPICIOUS_DEVICE,
    BLACKLISTED_USER,
    GEOGRAPHICAL_MISMATCH,
    UNUSUAL_BEHAVIOR,
    COMPROMISED_DEVICE,
    REGULATORY_COMPLIANCE
}

/**
 * Types of additional verification required.
 */
enum class VerificationType {
    BIOMETRIC,
    OTP,
    STEP_UP_AUTHENTICATION,
    MANUAL_REVIEW
}

/**
 * Transaction types for fraud analysis.
 */
enum class TransactionType {
    UPI,
    WALLET,
    BANK_TRANSFER,
    CARD_PAYMENT,
    QR_CODE,
    NEFT,
    RTGS,
    IMPS
}

/**
 * Fraud detection configuration.
 */
data class FraudDetectionConfig(
    val enableVelocityCheck: Boolean = true,
    val enableDeviceCheck: Boolean = true,
    val enableLocationCheck: Boolean = true,
    val enableBehaviorAnalysis: Boolean = true,
    val maxTransactionAmount: Double = 100000.0,
    val maxDailyTransactionAmount: Double = 500000.0,
    val maxTransactionCount: Int = 10,
    val riskScoreThreshold: Double = 0.7,
    val criticalRiskThreshold: Double = 0.9
)

/**
 * Velocity check result.
 */
data class VelocityCheckResult(
    val transactionCount: Int,
    val totalAmount: Double,
    val timeWindow: Long,
    val isViolated: Boolean,
    val limitType: VelocityLimitType
)

/**
 * Types of velocity limits.
 */
enum class VelocityLimitType {
    TRANSACTION_COUNT,
    TRANSACTION_AMOUNT,
    DAILY_LIMIT,
    HOURLY_LIMIT
}