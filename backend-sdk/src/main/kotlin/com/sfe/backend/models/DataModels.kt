package com.sfe.backend.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Payment request model
 */
data class PaymentRequest(
    @JsonProperty("transaction_id")
    val transactionId: String = UUID.randomUUID().toString(),
    
    @JsonProperty("user_id")
    val userId: String,
    
    @JsonProperty("amount")
    val amount: BigDecimal,
    
    @JsonProperty("currency")
    val currency: String = "INR",
    
    @JsonProperty("transaction_type")
    val transactionType: TransactionType,
    
    @JsonProperty("recipient_details")
    val recipientDetails: RecipientDetails,
    
    @JsonProperty("description")
    val description: String? = null,
    
    @JsonProperty("device_info")
    val deviceInfo: DeviceInfo? = null,
    
    @JsonProperty("metadata")
    val metadata: Map<String, Any> = emptyMap(),
    
    @JsonProperty("created_at")
    val createdAt: Instant = Instant.now()
)

/**
 * Recipient details for payment
 */
data class RecipientDetails(
    @JsonProperty("account_number")
    val accountNumber: String? = null,
    
    @JsonProperty("ifsc_code")
    val ifscCode: String? = null,
    
    @JsonProperty("upi_id")
    val upiId: String? = null,
    
    @JsonProperty("phone_number")
    val phoneNumber: String? = null,
    
    @JsonProperty("name")
    val name: String,
    
    @JsonProperty("bank_name")
    val bankName: String? = null
)

/**
 * Device information for security tracking
 */
data class DeviceInfo(
    @JsonProperty("device_id")
    val deviceId: String,
    
    @JsonProperty("device_type")
    val deviceType: String,
    
    @JsonProperty("ip_address")
    val ipAddress: String,
    
    @JsonProperty("user_agent")
    val userAgent: String? = null,
    
    @JsonProperty("location")
    val location: Location? = null,
    
    @JsonProperty("app_version")
    val appVersion: String? = null
)

/**
 * Location information
 */
data class Location(
    @JsonProperty("latitude")
    val latitude: Double,
    
    @JsonProperty("longitude")
    val longitude: Double,
    
    @JsonProperty("address")
    val address: String? = null
)

/**
 * Payment response model
 */
data class PaymentResponse(
    @JsonProperty("transaction_id")
    val transactionId: String,
    
    @JsonProperty("status")
    val status: PaymentStatus,
    
    @JsonProperty("message")
    val message: String,
    
    @JsonProperty("reference_id")
    val referenceId: String? = null,
    
    @JsonProperty("fee")
    val fee: BigDecimal? = null,
    
    @JsonProperty("processed_at")
    val processedAt: Instant? = null,
    
    @JsonProperty("failure_reason")
    val failureReason: String? = null
) {
    companion object {
        fun success(transactionId: String, referenceId: String? = null) = PaymentResponse(
            transactionId = transactionId,
            status = PaymentStatus.COMPLETED,
            message = "Payment processed successfully",
            referenceId = referenceId,
            processedAt = Instant.now()
        )
        
        fun error(message: String) = PaymentResponse(
            transactionId = "",
            status = PaymentStatus.FAILED,
            message = message,
            failureReason = message
        )
        
        fun blocked(reason: String) = PaymentResponse(
            transactionId = "",
            status = PaymentStatus.BLOCKED,
            message = "Payment blocked due to security concerns",
            failureReason = reason
        )
    }
}

/**
 * Transaction status model
 */
data class TransactionStatus(
    @JsonProperty("transaction_id")
    val transactionId: String,
    
    @JsonProperty("status")
    val status: PaymentStatus,
    
    @JsonProperty("amount")
    val amount: BigDecimal,
    
    @JsonProperty("currency")
    val currency: String,
    
    @JsonProperty("created_at")
    val createdAt: Instant,
    
    @JsonProperty("updated_at")
    val updatedAt: Instant,
    
    @JsonProperty("reference_id")
    val referenceId: String? = null,
    
    @JsonProperty("failure_reason")
    val failureReason: String? = null
)

/**
 * User registration request
 */
data class UserRegistrationRequest(
    @JsonProperty("email")
    val email: String,
    
    @JsonProperty("phone")
    val phone: String,
    
    @JsonProperty("full_name")
    val fullName: String,
    
    @JsonProperty("aadhaar_number")
    val aadhaarNumber: String,
    
    @JsonProperty("pan_number")
    val panNumber: String,
    
    @JsonProperty("bank_account")
    val bankAccount: BankAccountDetails,
    
    @JsonProperty("video_kyc_data")
    val videoKYCData: VideoKYCData? = null
)

/**
 * Bank account details
 */
data class BankAccountDetails(
    @JsonProperty("account_number")
    val accountNumber: String,
    
    @JsonProperty("ifsc_code")
    val ifscCode: String,
    
    @JsonProperty("bank_name")
    val bankName: String,
    
    @JsonProperty("account_type")
    val accountType: String = "SAVINGS"
)

/**
 * Video KYC data
 */
data class VideoKYCData(
    @JsonProperty("video_url")
    val videoUrl: String,
    
    @JsonProperty("face_match_score")
    val faceMatchScore: Double,
    
    @JsonProperty("document_verification")
    val documentVerification: Boolean,
    
    @JsonProperty("liveness_check")
    val livenessCheck: Boolean
)

/**
 * KYC verification result
 */
data class KYCResult(
    @JsonProperty("is_verified")
    val isVerified: Boolean,
    
    @JsonProperty("verification_score")
    val verificationScore: Double,
    
    @JsonProperty("risk_profile")
    val riskProfile: RiskProfile,
    
    @JsonProperty("failure_reason")
    val failureReason: String? = null,
    
    @JsonProperty("verified_at")
    val verifiedAt: Instant? = null
)

/**
 * Risk profile for users
 */
data class RiskProfile(
    @JsonProperty("risk_level")
    val riskLevel: RiskLevel,
    
    @JsonProperty("risk_score")
    val riskScore: Double,
    
    @JsonProperty("risk_factors")
    val riskFactors: List<String> = emptyList()
)

/**
 * Fraud assessment result
 */
data class RiskAssessment(
    @JsonProperty("risk_level")
    val riskLevel: RiskLevel,
    
    @JsonProperty("risk_score")
    val riskScore: Double,
    
    @JsonProperty("reason")
    val reason: String,
    
    @JsonProperty("recommended_action")
    val recommendedAction: String,
    
    @JsonProperty("fraud_indicators")
    val fraudIndicators: List<String> = emptyList()
)

/**
 * User model
 */
data class User(
    @JsonProperty("id")
    val id: String,
    
    @JsonProperty("email")
    val email: String,
    
    @JsonProperty("phone")
    val phone: String,
    
    @JsonProperty("full_name")
    val fullName: String,
    
    @JsonProperty("kyc_status")
    val kycStatus: KYCStatus,
    
    @JsonProperty("risk_profile")
    val riskProfile: RiskProfile,
    
    @JsonProperty("status")
    val status: UserStatus = UserStatus.ACTIVE,
    
    @JsonProperty("created_at")
    val createdAt: Instant,
    
    @JsonProperty("updated_at")
    val updatedAt: Instant = Instant.now()
)

/**
 * User response model
 */
data class UserResponse(
    @JsonProperty("user_id")
    val userId: String,
    
    @JsonProperty("email")
    val email: String,
    
    @JsonProperty("phone")
    val phone: String,
    
    @JsonProperty("full_name")
    val fullName: String,
    
    @JsonProperty("kyc_status")
    val kycStatus: KYCStatus,
    
    @JsonProperty("status")
    val status: UserStatus,
    
    @JsonProperty("created_at")
    val createdAt: Instant
) {
    companion object {
        fun from(user: User) = UserResponse(
            userId = user.id,
            email = user.email,
            phone = user.phone,
            fullName = user.fullName,
            kycStatus = user.kycStatus,
            status = user.status,
            createdAt = user.createdAt
        )
    }
}

/**
 * Transaction limits
 */
data class TransactionLimits(
    @JsonProperty("daily_limit")
    val dailyLimit: BigDecimal,
    
    @JsonProperty("monthly_limit")
    val monthlyLimit: BigDecimal,
    
    @JsonProperty("per_transaction_limit")
    val perTransactionLimit: BigDecimal,
    
    @JsonProperty("yearly_limit")
    val yearlyLimit: BigDecimal
)

/**
 * Webhook payload
 */
data class WebhookPayload(
    @JsonProperty("event_type")
    val eventType: WebhookEventType,
    
    @JsonProperty("event_id")
    val eventId: String = UUID.randomUUID().toString(),
    
    @JsonProperty("timestamp")
    val timestamp: Instant = Instant.now(),
    
    @JsonProperty("data")
    val data: Map<String, Any>
)