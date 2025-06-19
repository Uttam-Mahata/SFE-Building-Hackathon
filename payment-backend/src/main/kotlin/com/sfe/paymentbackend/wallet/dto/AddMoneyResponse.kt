package com.sfe.paymentbackend.wallet.dto

import com.sfe.sdk.backend.PaymentResponse as SDKPaymentResponse
import com.sfe.sdk.backend.PaymentStatus

// Maps from the SDK's PaymentResponse after an add money operation.
data class AddMoneyResponse(
    val transactionId: String?,
    val status: String, // e.g., "SUCCESS", "PENDING", "FAILED"
    val message: String?,
    val isBlocked: Boolean,
    val errorCode: String?
) {
    companion object {
        fun fromSDKPaymentResponse(sdkResponse: SDKPaymentResponse): AddMoneyResponse {
            return AddMoneyResponse(
                transactionId = sdkResponse.transactionId,
                status = sdkResponse.status.name, // Convert enum to string
                message = sdkResponse.message,
                isBlocked = sdkResponse.isBlocked,
                errorCode = sdkResponse.errorCode
            )
        }
    }
}
