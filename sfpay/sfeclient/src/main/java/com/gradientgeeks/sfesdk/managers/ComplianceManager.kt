package com.gradientgeeks.sfesdk.managers

import android.content.Context
import com.gradientgeeks.sfesdk.SfeFrontendSdk
import com.gradientgeeks.sfesdk.models.ComplianceEvent
import com.gradientgeeks.sfesdk.models.ComplianceStatus
import com.gradientgeeks.sfesdk.models.PolicyViolation
import com.gradientgeeks.sfesdk.models.SecurityThreat

/**
 * Manager for regulatory compliance and reporting
 * 
 * Handles compliance events, regulatory reporting, and audit trail management
 */
interface ComplianceManager {
    
    /**
     * Initialize compliance manager with regulatory configuration
     */
    fun initialize(context: Context, config: SfeFrontendSdk.SdkConfig)
    
    /**
     * Record a compliance event for regulatory reporting
     */
    suspend fun recordComplianceEvent(event: ComplianceEvent): Result<Unit>
    
    /**
     * Record a policy violation
     */
    suspend fun recordPolicyViolation(violation: PolicyViolation): Result<Unit>
    
    /**
     * Record a security threat
     */
    suspend fun recordSecurityThreat(threat: SecurityThreat): Result<Unit>
    
    /**
     * Get current compliance status
     */
    suspend fun getComplianceStatus(context: Context): Result<ComplianceStatus>
    
    /**
     * Submit pending events to regulatory authority
     */
    suspend fun submitRegulatoryReport(context: Context): Result<String>
    
    /**
     * Check if specific regulatory requirements are met
     */
    suspend fun checkRegulatoryCompliance(context: Context, requirements: List<String>): Result<Map<String, Boolean>>
    
    /**
     * Get audit trail for compliance verification
     */
    suspend fun getAuditTrail(fromTimestamp: Long, toTimestamp: Long): Result<List<ComplianceEvent>>
    
    /**
     * Validate data localization compliance
     */
    suspend fun validateDataLocalization(context: Context): Result<Boolean>
    
    /**
     * Emergency compliance notification
     */
    suspend fun emergencyNotification(threat: SecurityThreat, context: Context): Result<Unit>
} 