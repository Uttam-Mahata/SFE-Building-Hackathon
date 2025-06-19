package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.ComplianceReport

interface ComplianceOperations {
    fun generateDailyReport(reportType: String): ComplianceReport // reportType e.g., "AML", "TRANSACTION_VOLUME"
    fun processInquiry(inquiryId: String, details: Map<String, Any>): Boolean // For regulatory inquiries
    // Add more specific compliance methods as needed, e.g., related to AML, STR reporting
}
