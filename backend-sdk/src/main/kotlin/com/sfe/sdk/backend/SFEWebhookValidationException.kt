package com.sfe.sdk.backend

class SFEWebhookValidationException(
    message: String,
    val receivedPayload: String? = null, // Optional: include payload for debugging
    cause: Throwable? = null
) : Exception(message, cause)
