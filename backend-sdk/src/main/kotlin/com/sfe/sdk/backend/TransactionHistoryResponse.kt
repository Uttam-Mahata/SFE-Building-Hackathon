package com.sfe.sdk.backend

data class TransactionHistoryResponse(
    val transactions: List<TransactionStatus> // Assuming TransactionStatus can represent a historical transaction
)
