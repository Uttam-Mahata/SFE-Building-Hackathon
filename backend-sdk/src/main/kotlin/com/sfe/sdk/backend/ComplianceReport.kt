package com.sfe.sdk.backend

// This could be a simple data class or a more complex structure
// depending on the reporting needs.
data class ComplianceReport(
    val reportId: String,
    val reportType: String, // e.g., "DAILY_TRANSACTION_SUMMARY", "SUSPICIOUS_ACTIVITY_REPORT"
    val generatedAt: Long, // Timestamp
    val data: Map<String, Any> // Flexible data structure
)
