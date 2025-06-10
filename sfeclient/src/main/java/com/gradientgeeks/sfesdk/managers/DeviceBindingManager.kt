package com.gradientgeeks.sfesdk.managers

import android.content.Context
import com.gradientgeeks.sfesdk.models.DeviceBindingDetails

/**
 * Enhanced Device Binding Manager
 * 
 * Handles comprehensive device and SIM integrity verification
 * with cryptographic device binding for production financial applications
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
     * For production: Uses Android Keystore for cryptographic token
     */
    suspend fun getDeviceBindingToken(context: Context): Result<String>
    
    /**
     * Get comprehensive device binding information
     * Including binding strength, validation methods, and trust score
     */
    suspend fun getDeviceBindingInfo(context: Context): Result<DeviceBindingDetails>
    
    /**
     * Validate device binding integrity
     * Checks if device binding is still valid and hasn't been compromised
     */
    suspend fun validateDeviceBinding(context: Context, previousToken: String): Result<Boolean>
    
    /**
     * Generate cryptographically secure device fingerprint
     * Uses hardware identifiers and creates unique device signature
     */
    suspend fun generateDeviceFingerprint(context: Context): Result<String>
    
    /**
     * Check SIM card integrity and consistency
     * Validates SIM operator, country, and detects SIM swapping
     */
    suspend fun checkSimIntegrity(context: Context): Result<Map<String, Any>>
    
    /**
     * Validate device hardware attestation
     * Uses Android hardware attestation if available
     */
    suspend fun validateHardwareAttestation(context: Context): Result<String?>
    
    /**
     * Check for device binding anomalies
     * Detects unusual changes in device characteristics
     */
    suspend fun detectBindingAnomalies(context: Context): Result<List<String>>
    
    /**
     * Get telephony security information
     * Network security, encryption status, carrier validation
     */
    suspend fun getTelephonySecurityInfo(context: Context): Result<Map<String, String>>
    
    /**
     * Refresh device binding with current device state
     * Updates binding information while maintaining continuity
     */
    suspend fun refreshDeviceBinding(context: Context): Result<DeviceBindingDetails>
    
    /**
     * Check location consistency for device binding
     * Validates device usage patterns and location changes
     */
    suspend fun checkLocationConsistency(context: Context): Result<Boolean>
    
    /**
     * Validate device enrollment in enterprise management
     * Checks MDM/EMM enrollment for corporate devices
     */
    suspend fun checkEnterpriseEnrollment(context: Context): Result<Map<String, Any>>
} 