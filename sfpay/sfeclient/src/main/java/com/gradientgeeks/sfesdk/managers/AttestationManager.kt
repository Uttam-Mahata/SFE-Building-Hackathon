package com.gradientgeeks.sfesdk.managers

import android.content.Context

/**
 * Manager for Google Play Integrity API integration
 * 
 * Handles device attestation through Google Play services
 */
interface AttestationManager {
    
    /**
     * Request Play Integrity attestation token
     * 
     * @param context Application context
     * @param nonce Cryptographic nonce for validation
     * @return Result containing attestation token or error
     */
    suspend fun requestPlayIntegrityToken(context: Context, nonce: String): Result<String>
} 