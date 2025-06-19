package com.sfe.paymentbackend.auth.dto

// Based on SAMPLE-BACKEND-README.md (deviceInfo for fraud)
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceInfo: DeviceInfo? // Optional device information for fraud detection
)

data class DeviceInfo(
    val deviceId: String,
    val ipAddress: String,
    val userAgent: String,
    // Add other relevant fields like OS version, app version, location (with consent)
)
