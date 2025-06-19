package com.sfe.paymentbackend.persistence.repository

import com.sfe.paymentbackend.persistence.entity.WalletEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletRepository : JpaRepository<WalletEntity, String> {
    fun findByUserId(userId: String): WalletEntity?
}
