package com.gradientgeeks.csfe.wallet

/**
 * Data model for wallet information.
 */
data class WalletData(
    val balance: Double,
    val currency: String,
    val walletId: String,
    val lastUpdated: Long
)

/**
 * Sources for adding funds to a wallet.
 */
enum class WalletFundingSource {
    /**
     * UPI payment.
     */
    UPI,
    
    /**
     * Credit or debit card.
     */
    CARD,
    
    /**
     * Net banking.
     */
    NET_BANKING,
    
    /**
     * Bank transfer.
     */
    BANK_TRANSFER
}

/**
 * Bank account details for withdrawals.
 */
data class BankAccountDetails(
    val accountNumber: String,
    val ifscCode: String,
    val accountHolderName: String
)

/**
 * Results of wallet operations.
 */
sealed class WalletResult {
    /**
     * Wallet operation succeeded.
     */
    data class Success(
        val walletData: WalletData
    ) : WalletResult()
    
    /**
     * Wallet operation failed.
     */
    data class Error(
        val errorMessage: String,
        val errorCode: String
    ) : WalletResult()
}

/**
 * Results of add money or withdrawal operations.
 */
sealed class WalletOperationResult {
    /**
     * Operation completed successfully.
     */
    data class Success(
        val amount: Double,
        val newBalance: Double,
        val transactionId: String
    ) : WalletOperationResult()
    
    /**
     * Operation is pending completion.
     */
    data class Pending(
        val amount: Double,
        val transactionId: String,
        val estimatedCompletionTime: Long
    ) : WalletOperationResult()
    
    /**
     * Operation failed with an error.
     */
    data class Error(
        val errorMessage: String,
        val errorCode: String
    ) : WalletOperationResult()
}
