package com.sfe.sdk.backend

data class QRGenerationRequest(
    val amount: Double,
    val currency: String,
    val merchantId: String?,
    val transactionReference: String?,
    val purpose: String?
) {
    class Builder {
        private var amount: Double = 0.0
        private var currency: String = "INR" // Default currency
        private var merchantId: String? = null
        private var transactionReference: String? = null
        private var purpose: String? = null

        fun amount(amount: Double) = apply { this.amount = amount }
        fun currency(currency: String) = apply { this.currency = currency }
        fun merchantId(merchantId: String?) = apply { this.merchantId = merchantId }
        fun transactionReference(transactionReference: String?) = apply { this.transactionReference = transactionReference }
        fun purpose(purpose: String?) = apply { this.purpose = purpose }

        fun build() = QRGenerationRequest(amount, currency, merchantId, transactionReference, purpose)
    }
}
