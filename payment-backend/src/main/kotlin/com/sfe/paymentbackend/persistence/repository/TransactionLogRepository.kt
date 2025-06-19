package com.sfe.paymentbackend.persistence.repository

import com.sfe.paymentbackend.persistence.entity.TransactionLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionLogRepository : JpaRepository<TransactionLogEntity, String> {
    // Add custom query methods if needed, e.g., findByUserId, findBySdkTransactionId
    fun findBySdkTransactionId(sdkTransactionId: String): TransactionLogEntity?
    fun findByUserId(userId: String): List<TransactionLogEntity> // A user can have multiple transactions
}
