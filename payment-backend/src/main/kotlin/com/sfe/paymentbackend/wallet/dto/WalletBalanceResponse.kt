package com.sfe.paymentbackend.wallet.dto

// Assuming EncryptedBalance from SDK has fields like encryptedData and encryptionKeyId
// We can choose to expose these directly or map them.
// For simplicity, let's expose a structure that mirrors the SDK's EncryptedBalance.
import com.sfe.sdk.backend.EncryptedBalance as SDKEncryptedBalance

data class WalletBalanceResponse(
    val encryptedBalanceData: String,
    val keyId: String,
    val currency: String = "INR" // Assuming a default currency for the wallet display
) {
    companion object {
        fun fromSDKEncryptedBalance(sdkBalance: SDKEncryptedBalance, currencyCode: String = "INR"): WalletBalanceResponse {
            return WalletBalanceResponse(
                encryptedBalanceData = sdkBalance.encryptedData,
                keyId = sdkBalance.encryptionKeyId,
                currency = currencyCode
            )
        }
    }
}
