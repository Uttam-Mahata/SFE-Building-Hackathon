package com.gradientgeeks.csfe.models

import java.util.Date

/**
 * Common transaction types supported by the SDK
 */
enum class TransactionType {
    UPI,
    WALLET,
    BANK_TRANSFER,
    QR_CODE,
    CARD_PAYMENT
}

/**
 * Transaction status enumeration
 */
enum class TransactionStatus {
    INITIATED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED
}

/**
 * Represents a transaction in the system
 */
data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val description: String?,
    val recipientId: String,
    val recipientName: String,
    val timestamp: Date,
    val referenceId: String? = null,
    val failureReason: String? = null
)

/**
 * Filter for querying transaction history
 */
data class TransactionFilter(
    val startDate: Date? = null,
    val endDate: Date? = null,
    val transactionTypes: List<TransactionType>? = null,
    val status: List<TransactionStatus>? = null,
    val pageSize: Int = 20,
    val pageNumber: Int = 0
) {
    class Builder {
        private var startDate: Date? = null
        private var endDate: Date? = null
        private var transactionTypes: List<TransactionType>? = null
        private var status: List<TransactionStatus>? = null
        private var pageSize: Int = 20
        private var pageNumber: Int = 0
        
        fun setStartDate(date: Date): Builder {
            this.startDate = date
            return this
        }
        
        fun setEndDate(date: Date): Builder {
            this.endDate = date
            return this
        }
        
        fun setTransactionTypes(types: List<TransactionType>): Builder {
            this.transactionTypes = types
            return this
        }
        
        fun setStatus(statusList: List<TransactionStatus>): Builder {
            this.status = statusList
            return this
        }
        
        fun setPageSize(size: Int): Builder {
            this.pageSize = size
            return this
        }
        
        fun setPageNumber(page: Int): Builder {
            this.pageNumber = page
            return this
        }
        
        fun build(): TransactionFilter {
            return TransactionFilter(
                startDate = startDate,
                endDate = endDate,
                transactionTypes = transactionTypes,
                status = status,
                pageSize = pageSize,
                pageNumber = pageNumber
            )
        }
    }
}

/**
 * Device information for security purposes
 */
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val osVersion: String,
    val appVersion: String,
    val isRooted: Boolean,
    val hasSecurityPatches: Boolean
)

/**
 * Result wrapper for API responses
 */
sealed class SFEResult<T> {
    data class Success<T>(val data: T) : SFEResult<T>()
    data class Error<T>(
        val errorCode: String,
        val errorMessage: String,
        val isRetryable: Boolean = false
    ) : SFEResult<T>()
    data class Loading<T>(val message: String = "Processing...") : SFEResult<T>()
}

/**
 * Biometric authentication result
 */
sealed class BiometricResult {
    object Success : BiometricResult()
    data class Error(val errorMessage: String) : BiometricResult()
    object Cancelled : BiometricResult()
    object NotAvailable : BiometricResult()
}