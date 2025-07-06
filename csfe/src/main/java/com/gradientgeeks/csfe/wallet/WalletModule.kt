package com.gradientgeeks.csfe.wallet

import android.content.Context
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.models.SFEResult
import com.gradientgeeks.csfe.utils.Logger
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles wallet operations including balance management and transactions.
 */
class WalletModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "WalletModule"
    
    // In-memory wallet store for demo purposes
    private val walletStore = ConcurrentHashMap<String, WalletInfo>()
    
    init {
        // Initialize demo wallet
        if (config.enableMockPayments) {
            initializeDemoWallet()
        }
    }
    
    /**
     * Get wallet balance for a user.
     */
    fun getWalletBalance(
        userId: String,
        callback: (WalletBalanceResult) -> Unit
    ) {
        Logger.d(TAG, "Getting wallet balance for user: $userId")
        
        try {
            val wallet = walletStore[userId]
            
            if (wallet != null) {
                Logger.d(TAG, "Wallet found, balance: ${wallet.balance}")
                callback(WalletBalanceResult.Success(wallet.balance, wallet.currency))
            } else {
                // Create new wallet if not exists
                Logger.d(TAG, "Creating new wallet for user")
                val newWallet = WalletInfo(
                    userId = userId,
                    balance = 0.0,
                    currency = "INR",
                    isActive = true,
                    lastUpdated = Date()
                )
                walletStore[userId] = newWallet
                callback(WalletBalanceResult.Success(0.0, "INR"))
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting wallet balance: ${e.message}")
            callback(WalletBalanceResult.Error("Failed to get wallet balance"))
        }
    }
    
    /**
     * Add money to wallet.
     */
    fun addMoney(
        userId: String,
        amount: Double,
        source: String = "BANK_TRANSFER",
        callback: (WalletTransactionResult) -> Unit
    ) {
        Logger.d(TAG, "Adding money to wallet: $amount for user: $userId")
        
        if (amount <= 0) {
            callback(WalletTransactionResult.Error("Amount must be greater than zero"))
            return
        }
        
        try {
            val wallet = walletStore[userId] ?: WalletInfo(
                userId = userId,
                balance = 0.0,
                currency = "INR",
                isActive = true,
                lastUpdated = Date()
            )
            
            val updatedWallet = wallet.copy(
                balance = wallet.balance + amount,
                lastUpdated = Date()
            )
            
            walletStore[userId] = updatedWallet
            
            val transaction = WalletTransaction(
                id = "wallet_txn_${System.currentTimeMillis()}",
                userId = userId,
                amount = amount,
                type = WalletTransactionType.CREDIT,
                source = source,
                timestamp = Date(),
                balanceAfter = updatedWallet.balance,
                description = "Money added to wallet"
            )
            
            Logger.d(TAG, "Money added successfully. New balance: ${updatedWallet.balance}")
            callback(WalletTransactionResult.Success(transaction))
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error adding money to wallet: ${e.message}")
            callback(WalletTransactionResult.Error("Failed to add money to wallet"))
        }
    }
    
    /**
     * Deduct money from wallet.
     */
    fun deductMoney(
        userId: String,
        amount: Double,
        purpose: String = "PAYMENT",
        callback: (WalletTransactionResult) -> Unit
    ) {
        Logger.d(TAG, "Deducting money from wallet: $amount for user: $userId")
        
        if (amount <= 0) {
            callback(WalletTransactionResult.Error("Amount must be greater than zero"))
            return
        }
        
        try {
            val wallet = walletStore[userId]
            
            if (wallet == null) {
                callback(WalletTransactionResult.Error("Wallet not found"))
                return
            }
            
            if (wallet.balance < amount) {
                callback(WalletTransactionResult.Error("Insufficient balance"))
                return
            }
            
            val updatedWallet = wallet.copy(
                balance = wallet.balance - amount,
                lastUpdated = Date()
            )
            
            walletStore[userId] = updatedWallet
            
            val transaction = WalletTransaction(
                id = "wallet_txn_${System.currentTimeMillis()}",
                userId = userId,
                amount = amount,
                type = WalletTransactionType.DEBIT,
                source = purpose,
                timestamp = Date(),
                balanceAfter = updatedWallet.balance,
                description = "Money deducted from wallet"
            )
            
            Logger.d(TAG, "Money deducted successfully. New balance: ${updatedWallet.balance}")
            callback(WalletTransactionResult.Success(transaction))
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error deducting money from wallet: ${e.message}")
            callback(WalletTransactionResult.Error("Failed to deduct money from wallet"))
        }
    }
    
    /**
     * Transfer money between wallets.
     */
    fun transferMoney(
        fromUserId: String,
        toUserId: String,
        amount: Double,
        description: String = "Wallet transfer",
        callback: (WalletTransferResult) -> Unit
    ) {
        Logger.d(TAG, "Transferring money: $amount from $fromUserId to $toUserId")
        
        if (amount <= 0) {
            callback(WalletTransferResult.Error("Amount must be greater than zero"))
            return
        }
        
        if (fromUserId == toUserId) {
            callback(WalletTransferResult.Error("Cannot transfer to the same wallet"))
            return
        }
        
        try {
            val fromWallet = walletStore[fromUserId]
            val toWallet = walletStore[toUserId] ?: WalletInfo(
                userId = toUserId,
                balance = 0.0,
                currency = "INR",
                isActive = true,
                lastUpdated = Date()
            )
            
            if (fromWallet == null) {
                callback(WalletTransferResult.Error("Source wallet not found"))
                return
            }
            
            if (fromWallet.balance < amount) {
                callback(WalletTransferResult.Error("Insufficient balance in source wallet"))
                return
            }
            
            // Deduct from source wallet
            val updatedFromWallet = fromWallet.copy(
                balance = fromWallet.balance - amount,
                lastUpdated = Date()
            )
            
            // Add to destination wallet
            val updatedToWallet = toWallet.copy(
                balance = toWallet.balance + amount,
                lastUpdated = Date()
            )
            
            walletStore[fromUserId] = updatedFromWallet
            walletStore[toUserId] = updatedToWallet
            
            val transferId = "transfer_${System.currentTimeMillis()}"
            val timestamp = Date()
            
            val debitTransaction = WalletTransaction(
                id = "${transferId}_debit",
                userId = fromUserId,
                amount = amount,
                type = WalletTransactionType.DEBIT,
                source = "WALLET_TRANSFER",
                timestamp = timestamp,
                balanceAfter = updatedFromWallet.balance,
                description = description
            )
            
            val creditTransaction = WalletTransaction(
                id = "${transferId}_credit",
                userId = toUserId,
                amount = amount,
                type = WalletTransactionType.CREDIT,
                source = "WALLET_TRANSFER",
                timestamp = timestamp,
                balanceAfter = updatedToWallet.balance,
                description = description
            )
            
            Logger.d(TAG, "Money transferred successfully")
            callback(WalletTransferResult.Success(
                transferId = transferId,
                debitTransaction = debitTransaction,
                creditTransaction = creditTransaction
            ))
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error transferring money: ${e.message}")
            callback(WalletTransferResult.Error("Failed to transfer money"))
        }
    }
    
    /**
     * Get wallet transaction history.
     */
    fun getWalletTransactionHistory(
        userId: String,
        limit: Int = 50,
        callback: (WalletTransactionHistoryResult) -> Unit
    ) {
        Logger.d(TAG, "Getting wallet transaction history for user: $userId")
        
        try {
            // In a real implementation, this would fetch from database
            // For demo, generate some sample transactions
            val sampleTransactions = generateSampleWalletTransactions(userId, limit)
            
            Logger.d(TAG, "Found ${sampleTransactions.size} wallet transactions")
            callback(WalletTransactionHistoryResult.Success(sampleTransactions))
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting wallet transaction history: ${e.message}")
            callback(WalletTransactionHistoryResult.Error("Failed to get transaction history"))
        }
    }
    
    /**
     * Check if wallet has sufficient balance for a transaction.
     */
    fun checkSufficientBalance(
        userId: String,
        amount: Double,
        callback: (Boolean) -> Unit
    ) {
        Logger.d(TAG, "Checking sufficient balance for user: $userId, amount: $amount")
        
        val wallet = walletStore[userId]
        val hasSufficientBalance = wallet != null && wallet.balance >= amount
        
        Logger.d(TAG, "Sufficient balance check: $hasSufficientBalance")
        callback(hasSufficientBalance)
    }
    
    /**
     * Freeze/unfreeze wallet.
     */
    fun setWalletStatus(
        userId: String,
        isActive: Boolean,
        callback: (Boolean) -> Unit
    ) {
        Logger.d(TAG, "Setting wallet status for user: $userId, active: $isActive")
        
        try {
            val wallet = walletStore[userId]
            
            if (wallet != null) {
                val updatedWallet = wallet.copy(
                    isActive = isActive,
                    lastUpdated = Date()
                )
                walletStore[userId] = updatedWallet
                
                Logger.d(TAG, "Wallet status updated successfully")
                callback(true)
            } else {
                Logger.w(TAG, "Wallet not found for status update")
                callback(false)
            }
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating wallet status: ${e.message}")
            callback(false)
        }
    }
    
    private fun initializeDemoWallet() {
        Logger.d(TAG, "Initializing demo wallet")
        
        val demoWallet = WalletInfo(
            userId = "demo_user",
            balance = 1000.0,
            currency = "INR",
            isActive = true,
            lastUpdated = Date()
        )
        
        walletStore["demo_user"] = demoWallet
        Logger.d(TAG, "Demo wallet initialized with balance: ${demoWallet.balance}")
    }
    
    private fun generateSampleWalletTransactions(userId: String, limit: Int): List<WalletTransaction> {
        val transactions = mutableListOf<WalletTransaction>()
        val now = Date()
        val oneDay = 24 * 60 * 60 * 1000L
        
        for (i in 1..limit) {
            val isCredit = i % 3 == 0 // Every 3rd transaction is credit
            val amount = (50.0 + Math.random() * 500.0)
            val timestamp = Date(now.time - (i * oneDay))
            
            transactions.add(
                WalletTransaction(
                    id = "wallet_txn_${userId}_${timestamp.time}",
                    userId = userId,
                    amount = amount,
                    type = if (isCredit) WalletTransactionType.CREDIT else WalletTransactionType.DEBIT,
                    source = if (isCredit) "BANK_TRANSFER" else "PAYMENT",
                    timestamp = timestamp,
                    balanceAfter = 1000.0 - (i * 50.0), // Sample balance
                    description = if (isCredit) "Money added to wallet" else "Payment made"
                )
            )
        }
        
        return transactions.sortedByDescending { it.timestamp }
    }
}

/**
 * Wallet information
 */
data class WalletInfo(
    val userId: String,
    val balance: Double,
    val currency: String,
    val isActive: Boolean,
    val lastUpdated: Date
)

/**
 * Wallet transaction
 */
data class WalletTransaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val type: WalletTransactionType,
    val source: String,
    val timestamp: Date,
    val balanceAfter: Double,
    val description: String
)

/**
 * Wallet transaction types
 */
enum class WalletTransactionType {
    CREDIT,
    DEBIT
}

/**
 * Wallet balance result
 */
sealed class WalletBalanceResult {
    data class Success(val balance: Double, val currency: String) : WalletBalanceResult()
    data class Error(val message: String) : WalletBalanceResult()
}

/**
 * Wallet transaction result
 */
sealed class WalletTransactionResult {
    data class Success(val transaction: WalletTransaction) : WalletTransactionResult()
    data class Error(val message: String) : WalletTransactionResult()
}

/**
 * Wallet transfer result
 */
sealed class WalletTransferResult {
    data class Success(
        val transferId: String,
        val debitTransaction: WalletTransaction,
        val creditTransaction: WalletTransaction
    ) : WalletTransferResult()
    data class Error(val message: String) : WalletTransferResult()
}

/**
 * Wallet transaction history result
 */
sealed class WalletTransactionHistoryResult {
    data class Success(val transactions: List<WalletTransaction>) : WalletTransactionHistoryResult()
    data class Error(val message: String) : WalletTransactionHistoryResult()
}
