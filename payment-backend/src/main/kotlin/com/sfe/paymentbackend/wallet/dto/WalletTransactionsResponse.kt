package com.sfe.paymentbackend.wallet.dto

import com.sfe.sdk.backend.TransactionHistoryResponse as SDKTransactionHistoryResponse

// Wrapper for SDK's TransactionHistoryResponse
data class WalletTransactionsResponse(
    val history: SDKTransactionHistoryResponse
)
