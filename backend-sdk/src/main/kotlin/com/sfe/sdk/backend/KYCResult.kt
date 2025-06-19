package com.sfe.sdk.backend

data class KYCResult(
    val kycId: String,
    val status: KYCStatus, // Enum to be created
    val details: Map<String, Any>? = null
)
