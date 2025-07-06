package com.sfe.backend.services

import com.sfe.backend.models.*
import com.sfe.backend.sdk.SFEConfiguration
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class TransactionService(private val config: SFEConfiguration) {
    
    private val transactions = ConcurrentHashMap<String, TransactionRecord>()
    
    fun getStatus(transactionId: String): TransactionStatus {
        val transaction = transactions[transactionId] 
            ?: throw SFEException("Transaction not found: $transactionId")
        
        return TransactionStatus(
            transactionId = transaction.id,
            status = transaction.status,
            amount = transaction.amount,
            currency = transaction.currency,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt,
            referenceId = transaction.referenceId,
            failureReason = transaction.failureReason
        )
    }
    
    fun blockTransaction(transactionId: String, reason: String): TransactionService {
        transactions[transactionId]?.let { transaction ->
            transactions[transactionId] = transaction.copy(
                status = PaymentStatus.BLOCKED,
                failureReason = reason,
                updatedAt = Instant.now()
            )
        }
        return this
    }
    
    fun recordTransaction(transaction: TransactionRecord) {
        transactions[transaction.id] = transaction
    }
    
    fun getTransactionHistory(userId: String): List<TransactionRecord> {
        return transactions.values.filter { it.userId == userId }
    }
}