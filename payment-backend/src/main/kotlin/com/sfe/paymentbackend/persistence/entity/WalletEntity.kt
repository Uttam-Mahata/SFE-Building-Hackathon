package com.sfe.paymentbackend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "wallets")
data class WalletEntity(
    @Id
    val id: String, // UUID or other unique ID for the wallet itself

    @Column(nullable = false, unique = true) // Assuming one wallet per user for now
    val userId: String,

    @Column(nullable = false)
    var balance: Double = 0.0,

    @Column(nullable = false)
    var currency: String = "INR",

    @Column(nullable = false)
    var status: String = "ACTIVE", // e.g., ACTIVE, SUSPENDED, CLOSED

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    // No-arg constructor for JPA
    constructor() : this("", "", 0.0, "INR", "ACTIVE")
}
