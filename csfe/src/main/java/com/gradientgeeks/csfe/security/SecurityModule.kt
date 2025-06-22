package com.gradientgeeks.csfe.security

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.EditText
import com.gradientgeeks.csfe.config.EncryptionLevel
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.utils.Logger
import java.io.File

/**
 * Handles security-related functionality including device binding,
 * encryption, and security checks.
 */
class SecurityModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "SecurityModule"
    
    // Advanced security detectors
    private val rootDetector = RootDetector(context)
    private val emulatorDetector = EmulatorDetector(context)
    private val antiDebugger = AntiDebugger(context)
    private val hookDetector = HookDetector(context)
    private val screenProtector = ScreenProtector(context)
    private val secureKeyboard = SecureKeyboard(context)
    
    // Security monitoring state
    private var isMonitoringActive = false
    private var securityThreatCallback: ((SecurityThreat) -> Unit)? = null
    
    /**
     * Perform comprehensive security checks using all available detectors
     */
    fun performSecurityChecks(): SecurityCheckResult {
        Logger.d(TAG, "Performing comprehensive security checks...")
        
        val results = mutableMapOf<String, Boolean>()
        val threats = mutableListOf<SecurityThreat>()
        
        // Check for root
        val isRooted = rootDetector.isRooted()
        results["root_detection"] = isRooted
        if (isRooted) {
            threats.add(SecurityThreat.ROOT_DETECTED)
        }
        
        // Check for emulator
        val isEmulator = emulatorDetector.isEmulator()
        results["emulator_detection"] = isEmulator
        if (isEmulator) {
            threats.add(SecurityThreat.EMULATOR_DETECTED)
        }
        
        // Check for debugger
        val isDebuggerDetected = antiDebugger.isDebuggerDetected()
        results["debugger_detection"] = isDebuggerDetected
        if (isDebuggerDetected) {
            threats.add(SecurityThreat.DEBUGGER_DETECTED)
        }
        
        // Check for hooks
        val isHooked = hookDetector.isHooked()
        results["hook_detection"] = isHooked
        if (isHooked) {
            threats.add(SecurityThreat.HOOKS_DETECTED)
        }
        
        // Check for screen recording
        val isScreenRecording = screenProtector.isScreenRecordingActive()
        results["screen_recording_detection"] = isScreenRecording
        if (isScreenRecording) {
            threats.add(SecurityThreat.SCREEN_RECORDING_DETECTED)
        }
        
        // Check keyboard security
        val isKeyboardSecure = secureKeyboard.isSecureKeyboard()
        results["keyboard_security"] = !isKeyboardSecure
        if (!isKeyboardSecure) {
            threats.add(SecurityThreat.INSECURE_KEYBOARD)
        }
        
        val overallSafe = threats.isEmpty()
        
        Logger.d(TAG, "Security check completed. Safe: $overallSafe, Threats: ${threats.size}")
        
        if (!overallSafe) {
            Logger.w(TAG, "Security threats detected: ${threats.joinToString { it.name }}")
            securityThreatCallback?.invoke(threats.first()) // Report first threat
        }
        
        return SecurityCheckResult(
            isSafe = overallSafe,
            threats = threats,
            detectionResults = results
        )
    }
    
    /**
     * Check if the device is bound to the user's account.
     * 
     * @param callback Callback with the binding status
     */
    fun getDeviceBindingStatus(callback: (DeviceBindingStatus) -> Unit) {
        if (!config.enableDeviceBinding) {
            Logger.i(TAG, "Device binding is disabled in config")
            callback(DeviceBindingStatus(isBound = false, deviceId = null))
            return
        }
        
        // In a real implementation, this would check secure storage and/or server
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        // Simulated binding status - in reality would check with backend
        val isBound = deviceId != null
        
        callback(DeviceBindingStatus(isBound = isBound, deviceId = deviceId))
    }
    
    /**
     * Bind this device to the user's account for enhanced security.
     * 
     * @param userId ID of the user to bind the device to
     * @param callback Callback with the binding result
     */
    fun bindDevice(userId: String, callback: (DeviceBindingResult) -> Unit) {
        if (!config.enableDeviceBinding) {
            Logger.i(TAG, "Device binding is disabled in config")
            callback(DeviceBindingResult.Error("Device binding is disabled", "BINDING_DISABLED"))
            return
        }
        
        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        
        if (deviceId == null) {
            callback(DeviceBindingResult.Error("Could not determine device ID", "NO_DEVICE_ID"))
            return
        }
        
        // In a real implementation, this would make a network call to associate
        // the device ID with the user account on the backend
        
        // For the hackathon, simulate success
        callback(DeviceBindingResult.Success(deviceId))
    }
    
    /**
     * Encrypt sensitive data using the configured encryption level.
     * 
     * @param data Data to encrypt
     * @return Encrypted data
     */
    fun encryptData(data: String): String {
        Logger.d(TAG, "Encrypting data using ${config.encryptionLevel}")
        
        // In a real implementation, this would use proper encryption
        // based on the configured encryption level
        return when (config.encryptionLevel) {
            EncryptionLevel.AES_256 -> "AES256_${data.reversed()}"
            EncryptionLevel.AES_128 -> "AES128_${data.reversed()}"
        }
    }
    
    /**
     * Decrypt data that was encrypted with encryptData.
     * 
     * @param encryptedData Data to decrypt
     * @return Decrypted data or null if decryption failed
     */
    fun decryptData(encryptedData: String): String? {
        Logger.d(TAG, "Decrypting data")
        
        // In a real implementation, this would use proper decryption
        try {
            // For demo purposes, check if it matches our fake encryption pattern
            if (encryptedData.startsWith("AES256_") || encryptedData.startsWith("AES128_")) {
                val encrypted = encryptedData.substring(encryptedData.indexOf('_') + 1)
                return encrypted.reversed()
            }
            return null
        } catch (e: Exception) {
            Logger.e(TAG, "Error decrypting data: ${e.message}")
            return null
        }
    }
    
    /**
     * Start continuous security monitoring
     */
    fun startSecurityMonitoring(onThreatDetected: (SecurityThreat) -> Unit) {
        if (isMonitoringActive) {
            Logger.w(TAG, "Security monitoring already active")
            return
        }
        
        securityThreatCallback = onThreatDetected
        isMonitoringActive = true
        
        Logger.d(TAG, "Starting continuous security monitoring")
        
        // Start anti-debugging monitoring
        antiDebugger.startMonitoring {
            onThreatDetected(SecurityThreat.DEBUGGER_DETECTED)
        }
        
        // Enable ptrace protection
        antiDebugger.enablePtraceProtection()
        
        Logger.d(TAG, "Security monitoring started")
    }
    
    /**
     * Stop security monitoring
     */
    fun stopSecurityMonitoring() {
        isMonitoringActive = false
        antiDebugger.stopMonitoring()
        Logger.d(TAG, "Security monitoring stopped")
    }
    
    /**
     * Enable screen protection for an activity
     */
    fun enableScreenProtection(activity: Activity) {
        screenProtector.enableScreenProtection(activity) {
            securityThreatCallback?.invoke(SecurityThreat.SCREEN_RECORDING_DETECTED)
        }
    }
    
    /**
     * Disable screen protection for an activity
     */
    fun disableScreenProtection(activity: Activity) {
        screenProtector.disableScreenProtection(activity)
    }
    
    /**
     * Secure an EditText field for sensitive input
     */
    fun secureEditText(editText: EditText, isPasswordField: Boolean = false) {
        secureKeyboard.secureEditText(editText, isPasswordField)
    }
    
    /**
     * Get keyboard security recommendations
     */
    fun getKeyboardSecurityRecommendations(): List<String> {
        return secureKeyboard.getSecureKeyboardRecommendations()
    }
    
    /**
     * Check if the device is an emulator (legacy method for backward compatibility)
     */
    fun isEmulator(): Boolean {
        return emulatorDetector.isEmulator()
    }
    
    /**
     * Check if the device has been rooted (legacy method for backward compatibility)
     */
    fun isRooted(): Boolean {
        return rootDetector.isRooted()
    }
    
    /**
     * Check if a debugger is attached (legacy method for backward compatibility)
     */
    fun isDebuggerAttached(): Boolean {
        return antiDebugger.isDebuggerDetected()
    }
}
