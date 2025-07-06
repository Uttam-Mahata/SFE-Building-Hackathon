package com.gradientgeeks.csfe.security

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.EditText
import com.gradientgeeks.csfe.config.EncryptionLevel
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.models.DeviceInfo
import com.gradientgeeks.csfe.utils.Logger
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles security-related functionality including device binding,
 * encryption, and security checks.
 */
class SecurityModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "SecurityModule"
    private val deviceBindingStore = ConcurrentHashMap<String, String>()
    
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
     * Perform comprehensive security checks on the device.
     */
    fun performSecurityChecks(): SecurityCheckResult {
        Logger.d(TAG, "Performing security checks")
        
        val issues = mutableListOf<SecurityIssue>()
        
        // Check for root access
        if (isDeviceRooted()) {
            issues.add(SecurityIssue.DEVICE_ROOTED)
            Logger.w(TAG, "Device is rooted")
        }
        
        // Check for debugging
        if (isDebuggingEnabled()) {
            issues.add(SecurityIssue.DEBUGGING_ENABLED)
            Logger.w(TAG, "USB debugging is enabled")
        }
        
        // Check for emulator
        if (isEmulator()) {
            issues.add(SecurityIssue.RUNNING_ON_EMULATOR)
            Logger.w(TAG, "Running on emulator")
        }
        
        // Check for tampered app
        if (isAppTampered()) {
            issues.add(SecurityIssue.APP_TAMPERED)
            Logger.w(TAG, "App integrity check failed")
        }
        
        // Check for malicious apps
        if (hasMaliciousApps()) {
            issues.add(SecurityIssue.MALICIOUS_APPS_DETECTED)
            Logger.w(TAG, "Malicious apps detected")
        }
        
        val riskLevel = calculateRiskLevel(issues)
        
        return SecurityCheckResult(
            passed = issues.isEmpty(),
            riskLevel = riskLevel,
            issues = issues,
            deviceInfo = collectDeviceInfo()
        )
    }
    
    /**
     * Bind device to user account for enhanced security.
     */
    fun bindDevice(userId: String): DeviceBindingResult {
        if (!config.enableDeviceBinding) {
            Logger.w(TAG, "Device binding is disabled in config")
            return DeviceBindingResult.Error("Device binding disabled")
        }
        
        Logger.d(TAG, "Binding device for user: $userId")
        
        try {
            val deviceFingerprint = generateDeviceFingerprint()
            val bindingKey = generateBindingKey(userId, deviceFingerprint)
            
            // Store binding information
            deviceBindingStore[userId] = bindingKey
            
            // In a real implementation, this would be stored securely
            // using Android Keystore and synced with backend
            val prefs = context.getSharedPreferences("sfe_device_binding", Context.MODE_PRIVATE)
            prefs.edit().putString("binding_$userId", bindingKey).apply()
            
            Logger.d(TAG, "Device bound successfully")
            return DeviceBindingResult.Success(bindingKey)
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error binding device: ${e.message}")
            return DeviceBindingResult.Error("Device binding failed: ${e.message}")
        }
    }
    
    /**
     * Verify device binding for a user.
     */
    fun verifyDeviceBinding(userId: String): Boolean {
        if (!config.enableDeviceBinding) {
            return true // Skip verification if disabled
        }
        
        Logger.d(TAG, "Verifying device binding for user: $userId")
        
        try {
            val prefs = context.getSharedPreferences("sfe_device_binding", Context.MODE_PRIVATE)
            val storedBinding = prefs.getString("binding_$userId", null)
            
            if (storedBinding == null) {
                Logger.w(TAG, "No device binding found for user")
                return false
            }
            
            val currentFingerprint = generateDeviceFingerprint()
            val expectedBinding = generateBindingKey(userId, currentFingerprint)
            
            val isValid = storedBinding == expectedBinding
            Logger.d(TAG, "Device binding verification: $isValid")
            
            return isValid
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error verifying device binding: ${e.message}")
            return false
        }
    }
    
    /**
     * Generate a unique device fingerprint.
     */
    fun generateDeviceFingerprint(): String {
        val components = mutableListOf<String>()
        
        // Android ID
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            components.add("android_id:$androidId")
        }
        
        // Device model and manufacturer
        components.add("model:${Build.MODEL}")
        components.add("manufacturer:${Build.MANUFACTURER}")
        components.add("board:${Build.BOARD}")
        components.add("brand:${Build.BRAND}")
        
        // OS version
        components.add("sdk:${Build.VERSION.SDK_INT}")
        components.add("release:${Build.VERSION.RELEASE}")
        
        // Hardware info
        components.add("hardware:${Build.HARDWARE}")
        components.add("product:${Build.PRODUCT}")
        
        // App signature hash
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            val signature = packageInfo.signatures[0]
            val md = MessageDigest.getInstance("SHA-256")
            val signatureHash = md.digest(signature.toByteArray())
            components.add("signature:${signatureHash.joinToString("") { "%02x".format(it) }}")
        } catch (e: Exception) {
            Logger.w(TAG, "Could not get app signature: ${e.message}")
        }
        
        // Create fingerprint hash
        val fingerprintData = components.joinToString("|")
        val md = MessageDigest.getInstance("SHA-256")
        val fingerprintHash = md.digest(fingerprintData.toByteArray())
        
        return fingerprintHash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Encrypt sensitive data for storage.
     */
    fun encryptData(data: String, key: String): String {
        // In a real implementation, this would use Android Keystore
        // and proper encryption algorithms like AES-GCM
        
        // For demo purposes, return a simple encoded string
        return android.util.Base64.encodeToString(
            (data + "|" + key).toByteArray(),
            android.util.Base64.DEFAULT
        )
    }
    
    /**
     * Decrypt sensitive data.
     */
    fun decryptData(encryptedData: String, key: String): String? {
        return try {
            val decoded = String(android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT))
            val parts = decoded.split("|")
            if (parts.size >= 2 && parts[1] == key) {
                parts[0]
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error decrypting data: ${e.message}")
            null
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
    
    private fun isDeviceRooted(): Boolean {
        // Check for su binary
        val suPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        for (path in suPaths) {
            if (File(path).exists()) {
                return true
            }
        }
        
        // Check for root management apps
        val rootApps = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su"
        )
        
        for (packageName in rootApps) {
            try {
                context.packageManager.getPackageInfo(packageName, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, continue checking
            }
        }
        
        return false
    }
    
    private fun isDebuggingEnabled(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) == 1
    }
    
    private fun isAppTampered(): Boolean {
        // In a real implementation, this would check:
        // 1. App signature verification
        // 2. APK integrity checks
        // 3. Runtime application self-protection (RASP)
        
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            
            // For demo, just check if we have a signature
            return packageInfo.signatures.isEmpty()
            
        } catch (e: Exception) {
            return true // Consider tampered if we can't verify
        }
    }
    
    private fun hasMaliciousApps(): Boolean {
        // List of known malicious or risky app packages
        val suspiciousApps = arrayOf(
            "com.devadvance.rootcloak",
            "com.devadvance.rootcloakplus",
            "de.robv.android.xposed.installer",
            "com.saurik.substrate",
            "com.zachspong.temprootremovejb",
            "com.ramdroid.appquarantine",
            "com.topjohnwu.magisk"
        )
        
        for (packageName in suspiciousApps) {
            try {
                context.packageManager.getPackageInfo(packageName, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, continue checking
            }
        }
        
        return false
    }
    
    private fun calculateRiskLevel(issues: List<SecurityIssue>): SecurityRiskLevel {
        if (issues.isEmpty()) {
            return SecurityRiskLevel.LOW
        }
        
        val criticalIssues = issues.count { 
            it == SecurityIssue.DEVICE_ROOTED || 
            it == SecurityIssue.APP_TAMPERED || 
            it == SecurityIssue.MALICIOUS_APPS_DETECTED 
        }
        
        return when {
            criticalIssues > 0 -> SecurityRiskLevel.HIGH
            issues.size > 2 -> SecurityRiskLevel.MEDIUM
            else -> SecurityRiskLevel.LOW
        }
    }
    
    private fun collectDeviceInfo(): DeviceInfo {
        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
        
        return DeviceInfo(
            deviceId = generateDeviceFingerprint(),
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            osVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = packageInfo?.versionName ?: "Unknown",
            isRooted = isDeviceRooted(),
            hasSecurityPatches = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        )
    }
    
    private fun generateBindingKey(userId: String, deviceFingerprint: String): String {
        val data = "$userId|$deviceFingerprint|${System.currentTimeMillis()}"
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Security check result
 */
data class SecurityCheckResult(
    val passed: Boolean,
    val riskLevel: SecurityRiskLevel,
    val issues: List<SecurityIssue>,
    val deviceInfo: DeviceInfo
)

/**
 * Device binding result
 */
sealed class DeviceBindingResult {
    data class Success(val bindingKey: String) : DeviceBindingResult()
    data class Error(val message: String) : DeviceBindingResult()
}

/**
 * Security risk levels
 */
enum class SecurityRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Security issues that can be detected
 */
enum class SecurityIssue {
    DEVICE_ROOTED,
    DEBUGGING_ENABLED,
    RUNNING_ON_EMULATOR,
    APP_TAMPERED,
    MALICIOUS_APPS_DETECTED
}
