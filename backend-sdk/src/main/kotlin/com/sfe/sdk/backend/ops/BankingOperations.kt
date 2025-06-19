package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.BankTransferResult // Assuming these classes exist
import com.sfe.sdk.backend.PaymentRequest

interface BankingOperations {
    /**
     * Initiates a bank transfer.
     * @param request The payment request details, potentially extended for bank transfers.
     * @return A result object indicating the status of the bank transfer.
     */
    fun initiateBankTransfer(request: PaymentRequest): BankTransferResult {
        throw NotImplementedError("initiateBankTransfer is not yet implemented.")
    }

    /**
     * Validates an IFSC code.
     * @param ifscCode The IFSC code to validate.
     * @return True if the IFSC code is valid, false otherwise.
     */
    fun validateIFSC(ifscCode: String): Boolean {
        throw NotImplementedError("validateIFSC is not yet implemented.")
    }

    /**
     * Performs account verification (e.g., penny drop).
     * @param accountNumber The account number to verify.
     * @param ifscCode The IFSC code of the bank.
     * @return True if verification was successful (or initiated successfully), false otherwise.
     */
    fun performAccountVerification(accountNumber: String, ifscCode: String): Boolean {
        throw NotImplementedError("performAccountVerification is not yet implemented.")
    }
}
