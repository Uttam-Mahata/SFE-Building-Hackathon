package com.sfe.sdk.backend.ops

interface MonitoringOperations {
    fun recordTransactionMetric(transactionId: String, metricName: String, value: Double)
    fun updatePerformanceCounters(serviceName: String, counterName: String, increment: Long = 1L)
    fun checkSLACompliance(serviceName: String): Boolean // Returns true if within SLA
    fun getCurrentMetrics(): Map<String, Any> // Returns a map of current system metrics
}
