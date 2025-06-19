package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.TransactionInitiatedEvent
import com.sfe.sdk.backend.RiskProfile
import com.sfe.sdk.backend.RiskLevel
import com.sfe.sdk.backend.TransactionFilter
import com.sfe.sdk.backend.TransactionStatus

interface FraudOperations {
    fun analyzeTransaction(event: TransactionInitiatedEvent): RiskProfile // Assuming TransactionInitiatedEvent is relevant input
    fun checkVelocityLimits(userId: String, amount: Double): Boolean
    fun validateDeviceFingerprint(userId: String, fingerprint: String): Boolean
    fun analyzeUserBehavior(userId: String, eventType: String): RiskLevel // eventType e.g. "LOGIN", "PROFILE_UPDATE"
    fun assessLoginRisk(userId: String, ipAddress: String, userAgent: String): RiskLevel
    fun getSuspiciousTransactions(filter: TransactionFilter): List<TransactionStatus> // Reusing TransactionFilter and TransactionStatus
}
