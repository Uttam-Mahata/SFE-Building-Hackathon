package com.sfe.paymentbackend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "transactions_log")
data class TransactionLogEntity(
    @Id
    val id: String, // Internal unique ID for this log entry

    @Column(unique = true, nullable = true) // sdkTransactionId might not always be present if tx fails before SDK call
    var sdkTransactionId: String?,

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val type: String, // e.g., PAYMENT_INITIATED, ADD_MONEY, KYC_VERIFICATION

    @Column(nullable = false)
    val amount: Double,

    @Column(nullable = false)
    var status: String, // e.g., PENDING, SUCCESS, FAILED, BLOCKED

    @Column(nullable = true)
    var description: String? = null,

    @Column(nullable = false)
    val timestamp: Instant = Instant.now()
) {
    // No-arg constructor for JPA
    constructor() : this("", null, "", "", 0.0, "PENDING", null)
}
