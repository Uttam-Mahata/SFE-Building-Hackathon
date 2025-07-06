package com.sfe.backend.models

/**
 * Base exception for all SFE Backend SDK exceptions
 */
open class SFEException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Validation exception for invalid requests
 */
class SFEValidationException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Fraud detection exception
 */
class SFEFraudException(
    message: String,
    val reason: String,
    val riskLevel: RiskLevel,
    cause: Throwable? = null
) : SFEException(message, cause)

/**
 * Authentication exception
 */
class SFEAuthenticationException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Authorization exception
 */
class SFEAuthorizationException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Payment processing exception
 */
class SFEPaymentException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * KYC verification exception
 */
class KYCVerificationException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Webhook validation exception
 */
class SFEWebhookValidationException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Configuration exception
 */
class SFEConfigurationException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Rate limiting exception
 */
class SFERateLimitException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * External service exception
 */
class SFEExternalServiceException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Database exception
 */
class SFEDatabaseException(message: String, cause: Throwable? = null) : SFEException(message, cause)

/**
 * Encryption exception
 */
class SFEEncryptionException(message: String, cause: Throwable? = null) : SFEException(message, cause)