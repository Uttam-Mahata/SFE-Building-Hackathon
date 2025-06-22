package com.gradientgeeks.csfe.security

/**
 * Status of device binding.
 */
data class DeviceBindingStatus(
    val isBound: Boolean,
    val deviceId: String?
)

/**
 * Results of device binding operation.
 */
sealed class DeviceBindingResult {
    /**
     * Binding succeeded.
     */
    data class Success(
        val deviceId: String
    ) : DeviceBindingResult()
    
    /**
     * Binding failed with an error.
     */
    data class Error(
        val message: String,
        val code: String
    ) : DeviceBindingResult()
}

/**
 * Types of security threats that can be detected
 */
enum class SecurityThreat {
    ROOT_DETECTED,
    EMULATOR_DETECTED,
    DEBUGGER_DETECTED,
    HOOKS_DETECTED,
    SCREEN_RECORDING_DETECTED,
    INSECURE_KEYBOARD
}

/**
 * Result of comprehensive security checks
 */
data class SecurityCheckResult(
    val isSafe: Boolean,
    val threats: List<SecurityThreat>,
    val detectionResults: Map<String, Boolean>
)
