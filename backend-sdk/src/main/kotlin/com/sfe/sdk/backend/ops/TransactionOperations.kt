package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.TransactionStatus
import com.sfe.sdk.backend.PaymentStatus
import com.sfe.sdk.backend.TransactionFilter
import com.sfe.sdk.backend.TransactionHistoryResponse

interface TransactionOperations {
    fun getStatus(transactionId: String): TransactionStatus
    fun blockTransaction(transactionId: String, reason: String): Boolean
    fun getTransactionStatus(transactionId: String): PaymentStatus // This seems redundant with getStatus, but listed in README
    fun listTransactions(filter: TransactionFilter): TransactionHistoryResponse // Added based on TransactionFilter
}
