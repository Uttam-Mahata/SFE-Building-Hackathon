package com.sfe.backend.services

import com.sfe.backend.models.*
import com.sfe.backend.sdk.SFEConfiguration
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class MonitoringService(private val config: SFEConfiguration) {
    
    private val transactionCounter = AtomicLong(0)
    private val successfulTransactions = AtomicLong(0)
    private val failedTransactions = AtomicLong(0)
    private val fraudDetections = AtomicLong(0)
    private val metrics = ConcurrentHashMap<String, Any>()
    
    fun recordTransactionMetric(transaction: TransactionRecord): MonitoringService {
        transactionCounter.incrementAndGet()
        
        when (transaction.status) {
            PaymentStatus.COMPLETED -> successfulTransactions.incrementAndGet()
            PaymentStatus.FAILED -> failedTransactions.incrementAndGet()
            PaymentStatus.BLOCKED -> fraudDetections.incrementAndGet()
            else -> {}
        }
        
        return this
    }
    
    fun updatePerformanceCounters(): MonitoringService {
        metrics["last_updated"] = Instant.now()
        return this
    }
    
    fun checkSLACompliance(): MonitoringService {
        val successRate = if (transactionCounter.get() > 0) {
            successfulTransactions.get().toDouble() / transactionCounter.get().toDouble()
        } else {
            0.0
        }
        
        metrics["sla_compliance"] = successRate >= 0.99
        return this
    }
    
    fun getCurrentMetrics(): TransactionMetrics {
        val total = transactionCounter.get()
        val successful = successfulTransactions.get()
        val failed = failedTransactions.get()
        val fraud = fraudDetections.get()
        
        return TransactionMetrics(
            transactionCount = total,
            successfulCount = successful,
            failedCount = failed,
            fraudCount = fraud,
            successRate = if (total > 0) successful.toDouble() / total.toDouble() else 0.0,
            fraudDetectionRate = if (total > 0) fraud.toDouble() / total.toDouble() else 0.0,
            timestamp = Instant.now()
        )
    }
    
    fun resetMetrics(): MonitoringService {
        transactionCounter.set(0)
        successfulTransactions.set(0)
        failedTransactions.set(0)
        fraudDetections.set(0)
        metrics.clear()
        return this
    }
}

data class TransactionMetrics(
    val transactionCount: Long,
    val successfulCount: Long,
    val failedCount: Long,
    val fraudCount: Long,
    val successRate: Double,
    val fraudDetectionRate: Double,
    val timestamp: Instant
)