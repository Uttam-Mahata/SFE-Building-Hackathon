package com.sfe.backend.models

/**
 * Environment types for SFE Backend SDK
 */
enum class SFEEnvironment {
    SANDBOX,
    PRODUCTION,
    TEST
}

/**
 * Encryption levels supported by the SDK
 */
enum class EncryptionLevel {
    AES_128,
    AES_256,
    RSA_2048,
    RSA_4096
}

/**
 * Payment status enumeration
 */
enum class PaymentStatus {
    INITIATED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED,
    BLOCKED
}

/**
 * Transaction types
 */
enum class TransactionType {
    UPI,
    NEFT,
    RTGS,
    IMPS,
    CARD_PAYMENT,
    WALLET_TRANSFER,
    QR_CODE_PAYMENT,
    BANK_TRANSFER
}

/**
 * KYC verification status
 */
enum class KYCStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    EXPIRED,
    UNDER_REVIEW
}

/**
 * Risk levels for fraud detection
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Step-up authentication types
 */
enum class StepUpType {
    OTP,
    BIOMETRIC,
    PIN,
    SECURITY_QUESTION,
    DEVICE_VERIFICATION
}

/**
 * Webhook event types
 */
enum class WebhookEventType {
    PAYMENT_STATUS_UPDATE,
    FRAUD_ALERT,
    KYC_STATUS_UPDATE,
    COMPLIANCE_ALERT,
    SYSTEM_HEALTH_UPDATE
}

/**
 * Compliance report types
 */
enum class ReportType {
    DAILY_TRANSACTION_SUMMARY,
    FRAUD_DETECTION_SUMMARY,
    KYC_COMPLIANCE_REPORT,
    REGULATORY_FILING,
    AUDIT_TRAIL
}

/**
 * User account status
 */
enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    BLOCKED,
    PENDING_VERIFICATION
}