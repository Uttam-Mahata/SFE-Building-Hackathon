package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.EncryptedBalance // Assuming these classes exist
import com.sfe.sdk.backend.AddMoneyRequest
import com.sfe.sdk.backend.PaymentResponse // For the result of adding money

interface WalletOperations {
    /**
     * Retrieves the encrypted balance for a user's wallet.
     * @param userId The ID of the user.
     * @param walletId The ID of the wallet (if a user can have multiple wallets).
     * @return An EncryptedBalance object.
     */
    fun getEncryptedBalance(userId: String, walletId: String? = null): EncryptedBalance {
        throw NotImplementedError("getEncryptedBalance is not yet implemented.")
    }

    /**
     * Adds money to a user's wallet.
     * @param request The details for the add money request.
     * @return A PaymentResponse indicating the status of the transaction.
     */
    fun addMoney(request: AddMoneyRequest): PaymentResponse {
        throw NotImplementedError("addMoney is not yet implemented.")
    }

    /**
     * Retrieves the transaction history for a specific wallet.
     * @param userId The ID of the user.
     * @param walletId The ID of the wallet.
     * @return A list of transaction statuses or a dedicated wallet transaction history object.
     */
    fun getWalletTransactionHistory(userId: String, walletId: String? = null): List<com.sfe.sdk.backend.TransactionStatus> {
        throw NotImplementedError("getWalletTransactionHistory is not yet implemented.")
    }
}
