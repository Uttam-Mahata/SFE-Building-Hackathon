package com.sfe.sdk.backend

enum class PaymentStatus {
    COMPLETED,
    SUCCESS, // Note: COMPLETED and SUCCESS might be redundant, consider unifying
    FAILED,
    PENDING
}
