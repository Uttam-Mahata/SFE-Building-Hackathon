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
