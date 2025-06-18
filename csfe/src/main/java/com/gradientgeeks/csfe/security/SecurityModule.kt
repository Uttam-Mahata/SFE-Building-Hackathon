package com.gradientgeeks.csfe.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
    
    /**
     * Perform security checks to ensure the environment is safe.
     */
    fun performSecurityChecks() {
        Logger.d(TAG, "Performing security checks...")
        
        val isSafe = !isEmulator() && !isRooted() && !isDebuggerAttached()
        
        if (!isSafe) {
            Logger.w(TAG, "Security check failed: device may not be secure")
            
            // In a real app, depending on the security policy, you might:
            // 1. Disable sensitive features
            // 2. Alert the user
            // 3. Exit the app
            // 4. Report to back-end for fraud monitoring
        }
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
     * Check if the device is an emulator.
     */
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk")
    }
    
    /**
     * Check if the device has been rooted.
     */
    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su"
        )
        
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Check if a debugger is attached to the app.
     */
    private fun isDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }
}
