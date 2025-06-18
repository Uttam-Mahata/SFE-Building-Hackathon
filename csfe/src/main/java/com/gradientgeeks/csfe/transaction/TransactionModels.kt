package com.gradientgeeks.csfe.transaction

import java.util.Date

/**
 * Transaction data model.
 */
data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val timestamp: Date,
    val recipientName: String,
    val recipientId: String,
    val description: String?,
    val category: String?
)

/**
 * Types of transactions.
 */
enum class TransactionType {
    /**
     * UPI payment transaction.
     */
    UPI,
    
    /**
     * Wallet transaction.
     */
    WALLET,
    
    /**
     * Bank transfer.
     */
    BANK_TRANSFER,
    
    /**
     * QR code payment.
     */
    QR_CODE
}

/**
 * Status of a transaction.
 */
enum class TransactionStatus {
    /**
     * Transaction is being processed.
     */
    PENDING,
    
    /**
     * Transaction completed successfully.
     */
    COMPLETED,
    
    /**
     * Transaction failed.
     */
    FAILED
}

/**
 * Filter for transaction history queries.
 */
data class TransactionFilter private constructor(
    val startDate: Date?,
    val endDate: Date?,
    val transactionTypes: List<TransactionType>?,
    val pageSize: Int?,
    val page: Int?
) {
    /**
     * Builder for creating TransactionFilter instances.
     */
    class Builder {
        private var startDate: Date? = null
        private var endDate: Date? = null
        private var transactionTypes: List<TransactionType>? = null
        private var pageSize: Int? = 20
        private var page: Int? = 1
        
        /**
         * Set the start date for filtering transactions.
         */
        fun setStartDate(date: Date): Builder {
            this.startDate = date
            return this
        }
        
        /**
         * Set the end date for filtering transactions.
         */
        fun setEndDate(date: Date): Builder {
            this.endDate = date
            return this
        }
        
        /**
         * Set the transaction types to include.
         */
        fun setTransactionTypes(types: List<TransactionType>): Builder {
            this.transactionTypes = types
            return this
        }
        
        /**
         * Set the page size for paginating results.
         */
        fun setPageSize(size: Int): Builder {
            this.pageSize = size
            return this
        }
        
        /**
         * Set the page number for paginating results.
         */
        fun setPage(page: Int): Builder {
            this.page = page
            return this
        }
        
        /**
         * Build the TransactionFilter instance.
         */
        fun build(): TransactionFilter {
            return TransactionFilter(
                startDate = startDate,
                endDate = endDate,
                transactionTypes = transactionTypes,
                pageSize = pageSize,
                page = page
            )
        }
    }
}

/**
 * Results of transaction history query.
 */
sealed class TransactionHistoryResult {
    /**
     * Transaction history query succeeded.
     */
    data class Success(
        val transactions: List<Transaction>
    ) : TransactionHistoryResult()
    
    /**
     * Transaction history query failed.
     */
    data class Error(
        val errorMessage: String,
        val errorCode: String
    ) : TransactionHistoryResult()
}

/**
 * Results of transaction details query.
 */
sealed class TransactionDetailsResult {
    /**
     * Transaction details query succeeded.
     */
    data class Success(
        val transaction: Transaction
    ) : TransactionDetailsResult()
    
    /**
     * Transaction details query failed.
     */
    data class Error(
        val errorMessage: String,
        val errorCode: String
    ) : TransactionDetailsResult()
}
