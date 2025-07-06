package com.sfe.backend.services

import com.sfe.backend.models.*
import com.sfe.backend.sdk.SFEConfiguration
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

@Service
class WebhookService(private val config: SFEConfiguration) {
    
    private val objectMapper = ObjectMapper()
    
    fun validateSignature(payload: String, signature: String): WebhookService {
        val expectedSignature = generateSignature(payload)
        if (!signature.equals(expectedSignature, ignoreCase = true)) {
            throw SFEWebhookValidationException("Invalid webhook signature")
        }
        return this
    }
    
    fun parsePaymentStatusUpdate(payload: String): WebhookPayload {
        return try {
            objectMapper.readValue(payload, WebhookPayload::class.java)
        } catch (e: Exception) {
            throw SFEWebhookValidationException("Invalid webhook payload format", e)
        }
    }
    
    fun parseFraudAlert(payload: String): WebhookPayload {
        return try {
            objectMapper.readValue(payload, WebhookPayload::class.java)
        } catch (e: Exception) {
            throw SFEWebhookValidationException("Invalid fraud alert payload format", e)
        }
    }
    
    private fun generateSignature(payload: String): String {
        val secret = config.apiKey
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        val hash = mac.doFinal(payload.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }
}