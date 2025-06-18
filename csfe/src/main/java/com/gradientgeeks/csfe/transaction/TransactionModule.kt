package com.gradientgeeks.csfe.transaction

import android.content.Context
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.utils.Logger
import java.util.Date

/**
 * Handles transaction history and related operations.
 */
class TransactionModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "TransactionModule"
    
    /**
     * Get transaction history based on the provided filter.
     * 
     * @param filter Filter parameters for the transactions
     * @param callback Callback with the transaction history
     */
    fun getHistory(
        filter: TransactionFilter,
        callback: (TransactionHistoryResult) -> Unit
    ) {
        Logger.d(TAG, "Getting transaction history with filter: ${filter.transactionTypes}")
        
        // In a real implementation, this would fetch data from the server
        // For the hackathon, we'll generate mock transactions
        val mockTransactions = generateMockTransactions(filter)
        
        callback(TransactionHistoryResult.Success(mockTransactions))
    }
    
    /**
     * Get details for a specific transaction.
     * 
     * @param transactionId ID of the transaction to get details for
     * @param callback Callback with the transaction details
     */
    fun getTransactionDetails(
        transactionId: String,
        callback: (TransactionDetailsResult) -> Unit
    ) {
        Logger.d(TAG, "Getting details for transaction: $transactionId")
        
        // In a real implementation, this would fetch data from the server
        // For the hackathon, generate a mock transaction
        if (transactionId.startsWith("txn_") || transactionId.startsWith("TXN_")) {
            val transaction = Transaction(
                id = transactionId,
                amount = 100.0,
                type = TransactionType.UPI,
                status = TransactionStatus.COMPLETED,
                timestamp = Date(),
                recipientName = "Demo Recipient",
                recipientId = "demouser@sfe",
                description = "Test Transaction",
                category = "Shopping"
            )
            
            callback(TransactionDetailsResult.Success(transaction))
        } else {
            callback(TransactionDetailsResult.Error("Transaction not found", "NOT_FOUND"))
        }
    }
    
    /**
     * Generate mock transactions for demonstration.
     */
    private fun generateMockTransactions(filter: TransactionFilter): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        
        // Generate some mock data with varying types and statuses
        val types = filter.transactionTypes ?: listOf(
            TransactionType.UPI,
            TransactionType.WALLET,
            TransactionType.BANK_TRANSFER
        )
        
        val categories = listOf("Food", "Shopping", "Entertainment", "Bills", "Transfers")
        val recipients = listOf(
            "Grocery Store" to "groceries@sfe",
            "Movie Theater" to "movies@sfe",
            "Electric Company" to "electricity@sfe",
            "Online Shop" to "shop@sfe",
            "Friend" to "friend@sfe"
        )
        
        val pageSize = filter.pageSize ?: 10
        val currentTime = System.currentTimeMillis()
        
        for (i in 0 until pageSize) {
            val type = types.random()
            val (recipientName, recipientId) = recipients.random()
            val category = categories.random()
            val amount = (Math.random() * 1000).toInt().toDouble()
            val daysAgo = (Math.random() * 30).toInt()
            val timestamp = Date(currentTime - (daysAgo * 24 * 60 * 60 * 1000))
            val status = if (Math.random() > 0.1) {
                TransactionStatus.COMPLETED
            } else {
                TransactionStatus.FAILED
            }
            
            transactions.add(
                Transaction(
                    id = "txn_${i}_${System.currentTimeMillis()}",
                    amount = amount,
                    type = type,
                    status = status,
                    timestamp = timestamp,
                    recipientName = recipientName,
                    recipientId = recipientId,
                    description = "Payment to $recipientName",
                    category = category
                )
            )
        }
        
        // Sort by timestamp, most recent first
        return transactions.sortedByDescending { it.timestamp }
    }
}
