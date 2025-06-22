package com.gradientgeeks.csfe.security

import android.content.Context
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.provider.Settings
import android.text.InputType
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.gradientgeeks.csfe.utils.Logger
import java.security.SecureRandom

/**
 * Secure keyboard protection to prevent keylogging and input interception
 */
class SecureKeyboard(private val context: Context) {
    private val TAG = "SecureKeyboard"
    
    /**
     * Check if the current keyboard is secure and trusted
     */
    fun isSecureKeyboard(): Boolean {
        Logger.d(TAG, "Checking keyboard security")
        
        val detectionResults = mutableListOf<Pair<String, Boolean>>()
        
        // Method 1: Check for malicious input methods
        detectionResults.add("MALICIOUS_IME" to checkMaliciousInputMethods())
        
        // Method 2: Check current input method security
        detectionResults.add("CURRENT_IME_SECURITY" to checkCurrentInputMethodSecurity())
        
        // Method 3: Check for keylogger apps
        detectionResults.add("KEYLOGGER_APPS" to checkKeyloggerApps())
        
        // Method 4: Check input method permissions
        detectionResults.add("IME_PERMISSIONS" to checkInputMethodPermissions())
        
        // Method 5: Check for accessibility services that could intercept input
        detectionResults.add("ACCESSIBILITY_SERVICES" to checkAccessibilityServices())
        
        // Log results for debugging
        detectionResults.forEach { (method, result) ->
            Logger.d(TAG, "Keyboard security check $method: $result")
        }
        
        // Return false if any security issue is detected (secure = no issues)
        return !detectionResults.any { it.second }
    }
    
    /**
     * Apply secure input protection to EditText fields
     */
    fun secureEditText(editText: EditText, isPasswordField: Boolean = false) {
        try {
            // Disable text suggestions and auto-complete
            editText.inputType = editText.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            
            if (isPasswordField) {
                // For password fields, use password input type
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                
                // Disable clipboard operations
                editText.isLongClickable = false
                editText.setTextIsSelectable(false)
                
                // Disable copy/paste context menu
                editText.customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                    override fun onCreateActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?): Boolean = false
                    override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: android.view.Menu?): Boolean = false
                    override fun onActionItemClicked(mode: android.view.ActionMode?, item: android.view.MenuItem?): Boolean = false
                    override fun onDestroyActionMode(mode: android.view.ActionMode?) {}
                }
            }
            
            // Disable auto-fill for sensitive fields
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                editText.importantForAutofill = android.view.View.IMPORTANT_FOR_AUTOFILL_NO
            }
            
            Logger.d(TAG, "Applied secure input protection to EditText")
        } catch (e: Exception) {
            Logger.e(TAG, "Error securing EditText: ${e.message}")
        }
    }
    
    /**
     * Check for malicious input method editors (keyboards)
     */
    private fun checkMaliciousInputMethods(): Boolean {
        return try {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val enabledInputMethods = inputMethodManager.enabledInputMethodList
            
            val knownMaliciousIMEs = arrayOf(
                // Add known malicious keyboard package names
                "com.malicious.keyboard",
                "com.spy.keyboard",
                "com.keylogger.ime"
            )
            
            val suspiciousKeywords = arrayOf(
                "spy", "keylog", "capture", "monitor", "record", "hack"
            )
            
            enabledInputMethods.any { ime ->
                val packageName = ime.packageName.toLowerCase()
                val serviceName = ime.serviceName.toLowerCase()
                
                // Check against known malicious IMEs
                knownMaliciousIMEs.any { malicious -> packageName.contains(malicious) } ||
                
                // Check for suspicious keywords in package/service names
                suspiciousKeywords.any { keyword -> 
                    packageName.contains(keyword) || serviceName.contains(keyword)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking malicious input methods: ${e.message}")
            false
        }
    }
    
    /**
     * Check security of current input method
     */
    private fun checkCurrentInputMethodSecurity(): Boolean {
        return try {
            val currentInputMethod = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
            
            if (currentInputMethod.isNullOrEmpty()) {
                Logger.w(TAG, "No current input method found")
                return true // Suspicious
            }
            
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val enabledInputMethods = inputMethodManager.enabledInputMethodList
            
            val currentIME = enabledInputMethods.find { ime ->
                "${ime.packageName}/${ime.serviceName}" == currentInputMethod
            }
            
            if (currentIME == null) {
                Logger.w(TAG, "Current input method not found in enabled list")
                return true // Suspicious
            }
            
            // Check if current IME is from a trusted source
            val trustedIMEPackages = arrayOf(
                "com.google.android.inputmethod.latin", // Gboard
                "com.samsung.android.honeyboard",       // Samsung Keyboard
                "com.android.inputmethod.latin",        // AOSP Keyboard
                "com.swiftkey.swiftkeyapp",             // SwiftKey
                "com.touchtype.swiftkey"                // SwiftKey variant
            )
            
            val isUntrustedIME = !trustedIMEPackages.contains(currentIME.packageName)
            
            if (isUntrustedIME) {
                Logger.w(TAG, "Current IME is not from trusted source: ${currentIME.packageName}")
            }
            
            isUntrustedIME
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking current input method security: ${e.message}")
            true // Assume insecure on error
        }
    }
    
    /**
     * Check for known keylogger applications
     */
    private fun checkKeyloggerApps(): Boolean {
        return try {
            val packageManager = context.packageManager
            val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            
            val keyloggerPackages = arrayOf(
                "com.spyzie.android",
                "com.flexispy.android",
                "com.mobilespy.android",
                "com.spybubble.android",
                "com.mobile-spy.android"
            )
            
            val keyloggerKeywords = arrayOf(
                "keylog", "spy", "monitor", "track", "capture", "record", "surveillance"
            )
            
            installedPackages.any { packageInfo ->
                val packageName = packageInfo.packageName.toLowerCase()
                val appName = try {
                    packageInfo.applicationInfo?.let { appInfo ->
                        packageManager.getApplicationLabel(appInfo).toString().toLowerCase()
                    } ?: ""
                } catch (e: Exception) {
                    ""
                }
                
                // Check against known keylogger packages
                keyloggerPackages.any { keylogger -> packageName.contains(keylogger) } ||
                
                // Check for suspicious keywords
                keyloggerKeywords.any { keyword ->
                    packageName.contains(keyword) || appName.contains(keyword)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking keylogger apps: ${e.message}")
            false
        }
    }
    
    /**
     * Check input method permissions for potential security risks
     */
    private fun checkInputMethodPermissions(): Boolean {
        return try {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val enabledInputMethods = inputMethodManager.enabledInputMethodList
            
            enabledInputMethods.any { ime ->
                try {
                    val packageManager = context.packageManager
                    val packageInfo = packageManager.getPackageInfo(ime.packageName, PackageManager.GET_PERMISSIONS)
                    val permissions = packageInfo.requestedPermissions ?: return@any false
                    
                    // Check for suspicious permissions that shouldn't be needed for keyboards
                    val suspiciousPermissions = arrayOf(
                        "android.permission.RECORD_AUDIO",
                        "android.permission.CAMERA",
                        "android.permission.ACCESS_FINE_LOCATION",
                        "android.permission.READ_CONTACTS",
                        "android.permission.READ_SMS",
                        "android.permission.SEND_SMS",
                        "android.permission.CALL_PHONE",
                        "android.permission.INTERNET" // Can be legitimate but worth monitoring
                    )
                    
                    val hasSuspiciousPermissions = permissions.any { permission ->
                        suspiciousPermissions.contains(permission)
                    }
                    
                    if (hasSuspiciousPermissions) {
                        Logger.w(TAG, "IME ${ime.packageName} has suspicious permissions")
                    }
                    
                    hasSuspiciousPermissions
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking input method permissions: ${e.message}")
            false
        }
    }
    
    /**
     * Check for accessibility services that could intercept input
     */
    private fun checkAccessibilityServices(): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            if (enabledServices.isNullOrEmpty()) {
                return false // No accessibility services enabled
            }
            
            val serviceList = enabledServices.split(":")
            val suspiciousKeywords = arrayOf(
                "keylog", "spy", "monitor", "capture", "record", "hack"
            )
            
            serviceList.any { service ->
                val serviceName = service.toLowerCase()
                suspiciousKeywords.any { keyword -> serviceName.contains(keyword) }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking accessibility services: ${e.message}")
            false
        }
    }
    
    /**
     * Create a secure random PIN for additional protection
     */
    fun generateSecurePin(length: Int = 6): String {
        val secureRandom = SecureRandom()
        val pin = StringBuilder()
        
        repeat(length) {
            pin.append(secureRandom.nextInt(10))
        }
        
        return pin.toString()
    }
    
    /**
     * Obfuscate input display (show dots or asterisks)
     */
    fun obfuscateInput(input: String, showLastChar: Boolean = true): String {
        return if (input.isEmpty()) {
            ""
        } else if (showLastChar && input.length > 1) {
            "*".repeat(input.length - 1) + input.last()
        } else {
            "*".repeat(input.length)
        }
    }
    
    /**
     * Get list of enabled input methods for user review
     */
    fun getEnabledInputMethods(): List<InputMethodInfo> {
        return try {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.enabledInputMethodList
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting enabled input methods: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Check if device has a secure hardware keyboard
     */
    fun hasSecureHardwareKeyboard(): Boolean {
        return try {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Check if there's a physical keyboard connected
            context.resources.configuration.keyboard != android.content.res.Configuration.KEYBOARD_NOKEYS
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Recommend secure keyboard if current one is not safe
     */
    fun getSecureKeyboardRecommendations(): List<String> {
        return listOf(
            "Gboard (Google)",
            "Samsung Keyboard", 
            "SwiftKey Microsoft",
            "AOSP Keyboard (Stock Android)"
        )
    }
}