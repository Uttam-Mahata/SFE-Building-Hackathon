package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.UserRegistrationRequest
import com.sfe.sdk.backend.UserResponse
import com.sfe.sdk.backend.TransactionLimits
// Assuming SFEValidationException is in com.sfe.sdk.backend.exceptions
import com.sfe.sdk.backend.exceptions.SFEValidationException

interface UserOperations {
    fun register(request: UserRegistrationRequest): UserResponse // Throws SFEValidationException for bad data
    fun validateLimitUpdate(userId: String, newLimits: TransactionLimits): Boolean
    fun updateTransactionLimits(userId: String, limits: TransactionLimits): Boolean
    fun notifyLimitChange(userId: String, message: String) // Could be email, SMS, push notification
    fun getUserDetails(userId: String): UserResponse? // Added for completeness
}
