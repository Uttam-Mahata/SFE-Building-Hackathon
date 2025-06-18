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
import com.gradientgeeks.csfe.utils.Logger

/**
 * Handles authentication operations including biometric authentication,
 * user registration, and login.
 */
class AuthModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "AuthModule"
    
    /**
     * Authenticates the user using biometric authentication.
     * 
     * @param activity The activity to show the biometric prompt on
     * @param title The title of the biometric prompt
     * @param subtitle The subtitle of the biometric prompt
     * @param description The description of the biometric prompt
     * @param callback Callback with the biometric result
     */
    fun authenticateWithBiometrics(
        activity: Activity,
        title: String,
        subtitle: String,
        description: String,
        callback: (BiometricResult) -> Unit
    ) {
        if (!config.enableBiometrics) {
            Logger.w(TAG, "Biometric authentication is disabled in config")
            callback(BiometricResult.Error("Biometric authentication is disabled", "CONFIG_DISABLED"))
            return
        }
        
        // Check if biometric authentication is available
        if (activity !is FragmentActivity) {
            Logger.e(TAG, "Activity is not FragmentActivity")
            callback(BiometricResult.Error("Invalid activity type", "INVALID_ACTIVITY"))
            return
        }
        
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            Logger.e(TAG, "Biometric authentication not available: $canAuthenticate")
            callback(BiometricResult.Error("Biometric authentication not available", "NOT_AVAILABLE"))
            return
        }
        
        try {
            val executor = ContextCompat.getMainExecutor(context)
            
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Logger.d(TAG, "Biometric authentication succeeded")
                        
                        // Generate an auth token
                        val authToken = "bio_auth_${System.currentTimeMillis()}"
                        
                        callback(BiometricResult.Success(authToken))
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Logger.e(TAG, "Biometric error: $errString ($errorCode)")
                        
                        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            callback(BiometricResult.Cancelled)
                        } else {
                            callback(BiometricResult.Error(errString.toString(), "AUTH_ERROR_$errorCode"))
                        }
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Logger.e(TAG, "Biometric authentication failed")
                    }
                })
            
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
            
            biometricPrompt.authenticate(promptInfo)
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error during biometric authentication: ${e.message}")
            callback(BiometricResult.Error("Biometric authentication error", "SYSTEM_ERROR"))
        }
    }
    
    /**
     * Registers a new user.
     * 
     * @param registrationData User registration data
     * @param callback Callback with the registration result
     */
    fun register(registrationData: RegistrationData, callback: (RegistrationResult) -> Unit) {
        Logger.d(TAG, "Registering user: ${registrationData.phoneNumber}")
        
        // Simulate network delay for demo
        Handler(Looper.getMainLooper()).postDelayed({
            // For demo, always succeed unless the phone number is "1234567890"
            if (registrationData.phoneNumber == "1234567890") {
                callback(RegistrationResult.Error("User already registered", "USER_EXISTS"))
            } else {
                val userId = "user_${System.currentTimeMillis()}"
                callback(RegistrationResult.Success(userId))
            }
        }, 1500)
    }
    
    /**
     * Logs in an existing user.
     * 
     * @param loginData User login data
     * @param callback Callback with the login result
     */
    fun login(loginData: LoginData, callback: (LoginResult) -> Unit) {
        Logger.d(TAG, "Logging in user: ${loginData.phoneNumber}")
        
        // Simulate network delay for demo
        Handler(Looper.getMainLooper()).postDelayed({
            // For demo, succeed if a password is provided
            if (loginData.otp.isNullOrEmpty()) {
                callback(LoginResult.Error("OTP is required", "MISSING_OTP"))
            } else {
                val token = "token_${System.currentTimeMillis()}"
                callback(LoginResult.Success(token))
            }
        }, 1000)
    }
}
