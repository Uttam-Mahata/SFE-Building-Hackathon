package com.gradientgeeks.sfesdk.managers.impl

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.gradientgeeks.sfesdk.exceptions.SfeAttestationException
import com.gradientgeeks.sfesdk.managers.AttestationManager
import kotlinx.coroutines.tasks.await

class AttestationManagerImpl : AttestationManager {
    
    override suspend fun requestPlayIntegrityToken(context: Context, nonce: String): Result<String> {
        return try {
            val integrityManager = IntegrityManagerFactory.create(context)
            
            val integrityTokenRequest = IntegrityTokenRequest.builder()
                .setNonce(nonce)
                .build()
            
            val integrityTokenResponse = integrityManager
                .requestIntegrityToken(integrityTokenRequest)
                .await()
            
            val token = integrityTokenResponse.token()
            if (token.isNotEmpty()) {
                Result.success(token)
            } else {
                Result.failure(SfeAttestationException("Empty attestation token received"))
            }
        } catch (e: Exception) {
            Result.failure(SfeAttestationException("Failed to request Play Integrity token", e))
        }
    }
} 