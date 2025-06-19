package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.WebhookPaymentStatusUpdate
import com.sfe.sdk.backend.FraudAlert
// Assuming SFEWebhookValidationException is in com.sfe.sdk.backend.exceptions
import com.sfe.sdk.backend.exceptions.SFEWebhookValidationException

interface WebhookOperations {
    fun validateSignature(payload: String, signature: String, secret: String): Boolean // Basic signature validation
    fun parsePaymentStatusUpdate(payload: String): WebhookPaymentStatusUpdate // Throws SFEWebhookValidationException
    fun parseFraudAlert(payload: String): FraudAlert // Throws SFEWebhookValidationException
    fun validateNPCISignature(payload: String, signatureHeader: String): Boolean // Specific to NPCI
}
