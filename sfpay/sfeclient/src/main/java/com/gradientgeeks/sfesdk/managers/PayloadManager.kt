package com.gradientgeeks.sfesdk.managers

import android.content.Context

/**
 * Manager for secure data payload construction
 * 
 * Constructs JSON payloads containing security and device information
 */
interface PayloadManager {
    
    /**
     * Construct secure payload containing all security information
     * 
     * @param context Application context
     * @param attestationToken Optional Play Integrity attestation token
     * @return Result containing JSON payload string or error
     */
    suspend fun constructSecurePayload(context: Context, attestationToken: String?): Result<String>
} 