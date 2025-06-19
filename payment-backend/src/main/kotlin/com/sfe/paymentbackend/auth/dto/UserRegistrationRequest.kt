package com.sfe.paymentbackend.auth.dto

// Based on SAMPLE-BACKEND-README.md and common registration fields
data class UserRegistrationRequest(
    val email: String,
    val password: String, // Plain text password, will be hashed by UserService
    val firstName: String,
    val lastName: String,
    val aadhaarNumber: String,
    val panNumber: String,
    val videoKYCData: String, // Could be a base64 encoded string or a URL
    val consentToTerms: Boolean
)
