package com.sfe.sdk.backend

data class TransactionFilter(
    val userId: String? = null,
    val status: PaymentStatus? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
) {
    class Builder {
        private var userId: String? = null
        private var status: PaymentStatus? = null
        private var fromDate: Long? = null
        private var toDate: Long? = null
        private var minAmount: Double? = null
        private var maxAmount: Double? = null

        fun userId(userId: String?) = apply { this.userId = userId }
        fun status(status: PaymentStatus?) = apply { this.status = status }
        fun fromDate(fromDate: Long?) = apply { this.fromDate = fromDate }
        fun toDate(toDate: Long?) = apply { this.toDate = toDate }
        fun minAmount(minAmount: Double?) = apply { this.minAmount = minAmount }
        fun maxAmount(maxAmount: Double?) = apply { this.maxAmount = maxAmount }

        fun build() = TransactionFilter(userId, status, fromDate, toDate, minAmount, maxAmount)
    }
}
