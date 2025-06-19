package com.sfe.paymentbackend.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON response
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val tokens: Tokens? = null,
    val actionRequired: String? = null, // e.g., "OTP_VERIFICATION", "DEVICE_VERIFICATION"
    val otpReferenceId: String? = null, // If OTP is required
    val userId: String? = null
) {
    companion object {
        fun success(message: String, tokens: Tokens, userId: String): AuthResponse {
            return AuthResponse(
                success = true,
                message = message,
                tokens = tokens,
                userId = userId
            )
        }

        fun error(message: String): AuthResponse {
            return AuthResponse(
                success = false,
                message = message
            )
        }

        fun requireOTP(message: String, otpReferenceId: String, userId: String? = null): AuthResponse {
            return AuthResponse(
                success = false,
                message = message,
                actionRequired = "OTP_VERIFICATION",
                otpReferenceId = otpReferenceId,
                userId = userId
            )
        }

        fun requireDeviceVerification(message: String, userId: String? = null): AuthResponse {
            return AuthResponse(
                success = false,
                message = message,
                actionRequired = "DEVICE_VERIFICATION",
                userId = userId
            )
        }
    }
}
