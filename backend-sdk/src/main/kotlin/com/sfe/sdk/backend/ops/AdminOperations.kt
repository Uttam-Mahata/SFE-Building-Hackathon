package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.AccountFreezeRequest // Assuming this class exists

interface AdminOperations {
    /**
     * Freezes a user's account.
     * @param request The account freeze request details.
     * @return True if the account was successfully frozen, false otherwise.
     */
    fun freezeAccount(request: AccountFreezeRequest): Boolean {
        throw NotImplementedError("freezeAccount is not yet implemented.")
    }

    /**
     * Unfreezes a user's account.
     * @param userId The ID of the user whose account to unfreeze.
     * @param reason The reason for unfreezing the account.
     * @return True if the account was successfully unfrozen, false otherwise.
     */
    fun unfreezeAccount(userId: String, reason: String): Boolean {
        throw NotImplementedError("unfreezeAccount is not yet implemented.")
    }
}
