package com.sfe.sdk.backend

data class TransactionStatus(
    val transactionId: String,
    val status: String, // Consider using an enum for status
    val message: String? = null
)
