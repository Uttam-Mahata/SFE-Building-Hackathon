package com.gradientgeeks.csfe.auth

/**
 * Models for authentication data and results.
 */

/**
 * Data for user registration.
 */
data class RegistrationData(
    val phoneNumber: String,
    val email: String? = null,
    val fullName: String? = null
)

/**
 * Data for user login.
 */
data class LoginData(
    val phoneNumber: String,
    val otp: String? = null
)

/**
 * Results from a biometric authentication.
 */
sealed class BiometricResult {
    /**
     * Biometric authentication succeeded.
     */
    data class Success(
        val token: String
    ) : BiometricResult()
    
    /**
     * Biometric authentication failed with an error.
     */
    data class Error(
        val errorMessage: String,
        val errorCode: String
    ) : BiometricResult()
    
    /**
     * User cancelled the biometric authentication.
     */
    object Cancelled : BiometricResult()
}

/**
 * Results from a user registration attempt.
 */
sealed class RegistrationResult {
    /**
     * Registration succeeded.
     */
    data class Success(
        val userId: String
    ) : RegistrationResult()
    
    /**
     * Registration failed with an error.
     */
    data class Error(
        val message: String,
        val code: String
    ) : RegistrationResult()
}

/**
 * Results from a user login attempt.
 */
sealed class LoginResult {
    /**
     * Login succeeded.
     */
    data class Success(
        val token: String
    ) : LoginResult()
    
    /**
     * Login failed with an error.
     */
    data class Error(
        val message: String,
        val code: String
    ) : LoginResult()
}
