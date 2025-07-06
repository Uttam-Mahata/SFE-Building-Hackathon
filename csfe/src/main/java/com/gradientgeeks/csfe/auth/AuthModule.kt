package com.gradientgeeks.csfe.auth

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.models.BiometricResult
import com.gradientgeeks.csfe.utils.Logger
import java.util.concurrent.Executor

/**
 * Handles authentication operations including biometric authentication,
 * user registration, and login.
 */
class AuthModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "AuthModule"
    private val executor: Executor = ContextCompat.getMainExecutor(context)
    
    /**
     * Check if biometric authentication is available on the device.
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Logger.d(TAG, "Biometric authentication is available")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Logger.d(TAG, "No biometric hardware available")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Logger.d(TAG, "Biometric hardware unavailable")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Logger.d(TAG, "No biometric credentials enrolled")
                false
            }
            else -> false
        }
    }
    
    /**
     * Authenticate user with biometrics.
     */
    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "Authenticate",
        subtitle: String = "Use your biometric to authenticate",
        description: String = "Place your finger on the sensor or look at the camera",
        callback: (BiometricResult) -> Unit
    ) {
        if (!config.enableBiometrics) {
            Logger.w(TAG, "Biometric authentication is disabled in config")
            callback(BiometricResult.NotAvailable)
            return
        }
        
        if (!isBiometricAvailable()) {
            Logger.w(TAG, "Biometric authentication not available")
            callback(BiometricResult.NotAvailable)
            return
        }
        
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Logger.e(TAG, "Biometric authentication error: $errString")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> {
                            callback(BiometricResult.Cancelled)
                        }
                        else -> {
                            callback(BiometricResult.Error(errString.toString()))
                        }
                    }
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Logger.d(TAG, "Biometric authentication succeeded")
                    callback(BiometricResult.Success)
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Logger.w(TAG, "Biometric authentication failed")
                    callback(BiometricResult.Error("Authentication failed. Please try again."))
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancel")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Generate an authentication token for API calls.
     */
    fun generateAuthToken(userId: String): String {
        // In a real implementation, this would:
        // 1. Create a signed JWT token
        // 2. Include device information
        // 3. Set appropriate expiration time
        // 4. Sign with private key
        
        val timestamp = System.currentTimeMillis()
        val deviceId = getDeviceId()
        
        // For demo purposes, create a simple token
        return "AUTH_${userId}_${deviceId}_${timestamp}"
    }
    
    /**
     * Validate an authentication token.
     */
    fun validateAuthToken(token: String): Boolean {
        // In a real implementation, this would:
        // 1. Verify JWT signature
        // 2. Check expiration time
        // 3. Validate device information
        // 4. Check against revoked tokens list
        
        return token.startsWith("AUTH_") && token.split("_").size >= 4
    }
    
    /**
     * Login user with credentials.
     */
    fun login(
        username: String,
        password: String,
        callback: (LoginResult) -> Unit
    ) {
        Logger.d(TAG, "Login attempt for user: $username")
        
        if (username.isBlank() || password.isBlank()) {
            callback(LoginResult.Error("Username and password are required"))
            return
        }
        
        // TODO: Implement actual login with backend
        // For demo, simulate network call
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (config.enableMockPayments) {
                // Mock successful login
                val authToken = generateAuthToken(username)
                callback(LoginResult.Success(
                    userId = username,
                    authToken = authToken,
                    displayName = "Demo User"
                ))
            } else {
                // TODO: Make actual API call to backend
                callback(LoginResult.Error("Login not implemented yet"))
            }
        }, 1000)
    }
    
    /**
     * Register new user.
     */
    fun register(
        username: String,
        password: String,
        email: String,
        phoneNumber: String,
        callback: (RegistrationResult) -> Unit
    ) {
        Logger.d(TAG, "Registration attempt for user: $username")
        
        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            callback(RegistrationResult.Error("All fields are required"))
            return
        }
        
        // TODO: Implement actual registration with backend
        // For demo, simulate network call
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (config.enableMockPayments) {
                // Mock successful registration
                callback(RegistrationResult.Success(
                    userId = username,
                    message = "Registration successful"
                ))
            } else {
                // TODO: Make actual API call to backend
                callback(RegistrationResult.Error("Registration not implemented yet"))
            }
        }, 1500)
    }
    
    /**
     * Logout current user.
     */
    fun logout(callback: (Boolean) -> Unit) {
        Logger.d(TAG, "User logout")
        
        // TODO: Implement logout logic:
        // 1. Revoke authentication tokens
        // 2. Clear local storage
        // 3. Notify backend
        
        // For demo, just return success
        callback(true)
    }
    
    private fun getDeviceId(): String {
        // In a real implementation, this would generate a unique device ID
        // using Android ID, MAC address, or other device-specific identifiers
        return "DEVICE_${android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )}"
    }
}

/**
 * Login result sealed class
 */
sealed class LoginResult {
    data class Success(
        val userId: String,
        val authToken: String,
        val displayName: String
    ) : LoginResult()
    
    data class Error(val message: String) : LoginResult()
}

/**
 * Registration result sealed class
 */
sealed class RegistrationResult {
    data class Success(
        val userId: String,
        val message: String
    ) : RegistrationResult()
    
    data class Error(val message: String) : RegistrationResult()
}
