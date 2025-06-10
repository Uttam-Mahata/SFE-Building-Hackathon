package com.gradientgeeks.sfesdk.managers

import android.content.Context

/**
 * Manager for device binding functionality
 * 
 * Handles device and SIM integrity verification
 */
interface DeviceBindingManager {
    
    /**
     * Check if SIM card is present and ready
     */
    fun isSimPresent(context: Context): Boolean
    
    /**
     * Get network operator information
     */
    fun getNetworkOperator(context: Context): String?
    
    /**
     * Generate or retrieve device binding token
     * For prototype: Uses UUID stored in SharedPreferences
     */
    suspend fun getDeviceBindingToken(context: Context): Result<String>
} 