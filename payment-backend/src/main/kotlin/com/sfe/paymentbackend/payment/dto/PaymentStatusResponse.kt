package com.sfe.paymentbackend.payment.dto

import com.sfe.sdk.backend.TransactionStatus // SDK's TransactionStatus

// Wrapper for SDK's TransactionStatus to potentially add more info or structure
data class PaymentStatusResponse(
    val transactionDetails: TransactionStatus
)
