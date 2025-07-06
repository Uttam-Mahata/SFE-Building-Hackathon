package com.gradientgeeks.csfe.transaction

import android.content.Context
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.models.*
import com.gradientgeeks.csfe.utils.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles transaction history and management operations.
 */
class TransactionModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "TransactionModule"
    
    // In-memory transaction store for demo purposes
    // In a real implementation, this would use Room database or SQLite
    private val transactionStore = ConcurrentHashMap<String, Transaction>()
    
    init {
        // Initialize with some sample transactions for demo
        if (config.enableMockPayments) {
            initializeSampleTransactions()
        }
    }
    
    /**
     * Get transaction history with optional filters.
     */
    fun getTransactionHistory(
        filter: TransactionFilter,
        callback: (TransactionHistoryResult) -> Unit
    ) {
        Logger.d(TAG, "Getting transaction history with filter")
        
        try {
            // Filter transactions based on criteria
            var filteredTransactions = transactionStore.values.toList()
            
            // Filter by date range
            filter.startDate?.let { startDate ->
                filteredTransactions = filteredTransactions.filter { 
                    it.timestamp.after(startDate) || it.timestamp == startDate
                }
            }
            
            filter.endDate?.let { endDate ->
                filteredTransactions = filteredTransactions.filter { 
                    it.timestamp.before(endDate) || it.timestamp == endDate
                }
            }
            
            // Filter by transaction types
            filter.transactionTypes?.let { types ->
                filteredTransactions = filteredTransactions.filter { 
                    it.type in types 
                }
            }
            
            // Filter by status
            filter.status?.let { statusList ->
                filteredTransactions = filteredTransactions.filter { 
                    it.status in statusList 
                }
            }
            
            // Sort by timestamp (most recent first)
            filteredTransactions = filteredTransactions.sortedByDescending { it.timestamp }
            
            // Apply pagination
            val startIndex = filter.pageNumber * filter.pageSize
            val endIndex = minOf(startIndex + filter.pageSize, filteredTransactions.size)
            
            val paginatedTransactions = if (startIndex < filteredTransactions.size) {
                filteredTransactions.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
            
            val hasMore = endIndex < filteredTransactions.size
            
            Logger.d(TAG, "Found ${paginatedTransactions.size} transactions")
            
            callback(TransactionHistoryResult.Success(
                transactions = paginatedTransactions,
                totalCount = filteredTransactions.size,
                hasMore = hasMore,
                currentPage = filter.pageNumber
            ))
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting transaction history: ${e.message}")
            callback(TransactionHistoryResult.Error("Failed to load transaction history"))
        }
    }
    
    /**
     * Get a specific transaction by ID.
     */
    fun getTransaction(
        transactionId: String,
        callback: (TransactionResult) -> Unit
    ) {
        Logger.d(TAG, "Getting transaction: $transactionId")
        
        val transaction = transactionStore[transactionId]
        
        if (transaction != null) {
            Logger.d(TAG, "Transaction found")
            callback(TransactionResult.Success(transaction))
        } else {
            Logger.w(TAG, "Transaction not found")
            callback(TransactionResult.Error("Transaction not found"))
        }
    }
    
    /**
     * Add a new transaction to the store.
     */
    fun addTransaction(transaction: Transaction) {
        Logger.d(TAG, "Adding transaction: ${transaction.id}")
        transactionStore[transaction.id] = transaction
        
        // In a real implementation, this would also:
        // 1. Store in local database
        // 2. Sync with backend if online
        // 3. Update any observers/listeners
    }
    
    /**
     * Update transaction status.
     */
    fun updateTransactionStatus(
        transactionId: String,
        status: TransactionStatus,
        callback: (Boolean) -> Unit
    ) {
        Logger.d(TAG, "Updating transaction status: $transactionId -> $status")
        
        val transaction = transactionStore[transactionId]
        
        if (transaction != null) {
            val updatedTransaction = transaction.copy(status = status)
            transactionStore[transactionId] = updatedTransaction
            
            Logger.d(TAG, "Transaction status updated successfully")
            callback(true)
        } else {
            Logger.w(TAG, "Transaction not found for status update")
            callback(false)
        }
    }
    
    /**
     * Get transaction statistics.
     */
    fun getTransactionStatistics(
        startDate: Date? = null,
        endDate: Date? = null,
        callback: (TransactionStatistics) -> Unit
    ) {
        Logger.d(TAG, "Getting transaction statistics")
        
        try {
            var transactions = transactionStore.values.toList()
            
            // Filter by date range if provided
            startDate?.let { start ->
                transactions = transactions.filter { it.timestamp.after(start) || it.timestamp == start }
            }
            
            endDate?.let { end ->
                transactions = transactions.filter { it.timestamp.before(end) || it.timestamp == end }
            }
            
            val totalCount = transactions.size
            val totalAmount = transactions.sumOf { it.amount }
            val successfulCount = transactions.count { it.status == TransactionStatus.COMPLETED }
            val failedCount = transactions.count { it.status == TransactionStatus.FAILED }
            val pendingCount = transactions.count { 
                it.status == TransactionStatus.PENDING || it.status == TransactionStatus.PROCESSING 
            }
            
            val successRate = if (totalCount > 0) {
                (successfulCount.toDouble() / totalCount.toDouble()) * 100
            } else {
                0.0
            }
            
            val averageAmount = if (totalCount > 0) {
                totalAmount / totalCount
            } else {
                0.0
            }
            
            val statistics = TransactionStatistics(
                totalTransactions = totalCount,
                totalAmount = totalAmount,
                averageAmount = averageAmount,
                successfulTransactions = successfulCount,
                failedTransactions = failedCount,
                pendingTransactions = pendingCount,
                successRate = successRate
            )
            
            Logger.d(TAG, "Statistics calculated: $totalCount transactions, $successRate% success rate")
            callback(statistics)
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error calculating statistics: ${e.message}")
            // Return empty statistics on error
            callback(TransactionStatistics(0, 0.0, 0.0, 0, 0, 0, 0.0))
        }
    }
    
    /**
     * Search transactions by description or recipient.
     */
    fun searchTransactions(
        query: String,
        callback: (TransactionHistoryResult) -> Unit
    ) {
        Logger.d(TAG, "Searching transactions with query: $query")
        
        try {
            val searchQuery = query.lowercase().trim()
            
            if (searchQuery.isBlank()) {
                callback(TransactionHistoryResult.Error("Search query cannot be empty"))
                return
            }
            
            val matchingTransactions = transactionStore.values.filter { transaction ->
                transaction.description?.lowercase()?.contains(searchQuery) == true ||
                transaction.recipientName.lowercase().contains(searchQuery) ||
                transaction.recipientId.lowercase().contains(searchQuery) ||
                transaction.id.lowercase().contains(searchQuery)
            }.sortedByDescending { it.timestamp }
            
            Logger.d(TAG, "Found ${matchingTransactions.size} matching transactions")
            
            callback(TransactionHistoryResult.Success(
                transactions = matchingTransactions,
                totalCount = matchingTransactions.size,
                hasMore = false,
                currentPage = 0
            ))
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error searching transactions: ${e.message}")
            callback(TransactionHistoryResult.Error("Search failed"))
        }
    }
    
    /**
     * Clear all transaction history (for testing/demo purposes).
     */
    fun clearAllTransactions() {
        Logger.d(TAG, "Clearing all transactions")
        transactionStore.clear()
    }
    
    private fun initializeSampleTransactions() {
        Logger.d(TAG, "Initializing sample transactions for demo")
        
        val now = Date()
        val oneDay = 24 * 60 * 60 * 1000L
        
        val sampleTransactions = listOf(
            Transaction(
                id = "txn_001",
                amount = 150.0,
                type = TransactionType.UPI,
                status = TransactionStatus.COMPLETED,
                description = "Coffee payment",
                recipientId = "coffee@upi",
                recipientName = "Cafe Mocha",
                timestamp = Date(now.time - oneDay),
                referenceId = "REF001"
            ),
            Transaction(
                id = "txn_002",
                amount = 500.0,
                type = TransactionType.WALLET,
                status = TransactionStatus.COMPLETED,
                description = "Wallet top-up",
                recipientId = "wallet_topup",
                recipientName = "SFE Wallet",
                timestamp = Date(now.time - 2 * oneDay),
                referenceId = "REF002"
            ),
            Transaction(
                id = "txn_003",
                amount = 75.50,
                type = TransactionType.QR_CODE,
                status = TransactionStatus.FAILED,
                description = "Grocery shopping",
                recipientId = "grocery@merchant",
                recipientName = "SuperMart",
                timestamp = Date(now.time - 3 * oneDay),
                failureReason = "Insufficient balance"
            ),
            Transaction(
                id = "txn_004",
                amount = 1200.0,
                type = TransactionType.BANK_TRANSFER,
                status = TransactionStatus.PENDING,
                description = "Rent payment",
                recipientId = "landlord@bank",
                recipientName = "Property Owner",
                timestamp = Date(now.time - 4 * oneDay)
            ),
            Transaction(
                id = "txn_005",
                amount = 25.0,
                type = TransactionType.UPI,
                status = TransactionStatus.COMPLETED,
                description = "Bus fare",
                recipientId = "transport@upi",
                recipientName = "City Transport",
                timestamp = Date(now.time - 5 * oneDay),
                referenceId = "REF005"
            )
        )
        
        sampleTransactions.forEach { transaction ->
            transactionStore[transaction.id] = transaction
        }
        
        Logger.d(TAG, "Initialized ${sampleTransactions.size} sample transactions")
    }
}

/**
 * Transaction history result
 */
sealed class TransactionHistoryResult {
    data class Success(
        val transactions: List<Transaction>,
        val totalCount: Int,
        val hasMore: Boolean,
        val currentPage: Int
    ) : TransactionHistoryResult()
    
    data class Error(val errorMessage: String) : TransactionHistoryResult()
}

/**
 * Single transaction result
 */
sealed class TransactionResult {
    data class Success(val transaction: Transaction) : TransactionResult()
    data class Error(val errorMessage: String) : TransactionResult()
}

/**
 * Transaction statistics
 */
data class TransactionStatistics(
    val totalTransactions: Int,
    val totalAmount: Double,
    val averageAmount: Double,
    val successfulTransactions: Int,
    val failedTransactions: Int,
    val pendingTransactions: Int,
    val successRate: Double
)
