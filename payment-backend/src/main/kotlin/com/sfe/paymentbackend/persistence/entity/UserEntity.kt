package com.sfe.paymentbackend.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    val id: String, // UUID or other unique ID

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var passwordHash: String, // TODO: Ensure this is properly hashed in practice

    var fullName: String?,
    var phoneNumber: String?,

    @Column(nullable = false)
    var kycStatus: String, // e.g., PENDING, VERIFIED, FAILED

    var riskProfile: String?, // e.g., LOW, MEDIUM, HIGH

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    // No-arg constructor for JPA
    constructor() : this("", "", "", null, null, "PENDING", null)
}
