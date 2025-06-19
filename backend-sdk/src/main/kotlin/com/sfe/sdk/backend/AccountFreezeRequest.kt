package com.sfe.sdk.backend

data class AccountFreezeRequest(
    val userId: String,
    val reason: String,
    val freezeDurationSeconds: Long? = null // Null for indefinite freeze
)
