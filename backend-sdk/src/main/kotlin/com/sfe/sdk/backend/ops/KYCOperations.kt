package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.KYCResult
import com.sfe.sdk.backend.KYCStatus

interface KYCOperations {
    fun verifyAadhaar(userId: String, aadhaarNumber: String): KYCResult
    fun verifyPAN(userId: String, panNumber: String): KYCResult
    fun verifyBankAccount(userId: String, accountNumber: String, ifscCode: String): KYCResult
    fun performVideoKYC(userId: String): KYCResult // This would likely initiate a process
    fun getKYCStatus(userId: String): KYCStatus // Added for completeness
}
