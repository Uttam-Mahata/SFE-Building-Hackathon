package com.gradientgeeks.sfesdk.models

import java.util.Date

/**
 * Comprehensive data models for the secure payload structure
 * Production-ready models with regulatory compliance support
 */

data class SecurePayload(
    val appVersion: String,
    val sdkVersion: String,
    val timestamp: String,
    val deviceInfo: DeviceInfo,
    val bindingInfo: BindingInfo,
    val attestationToken: String?,
    // Enhanced fields for production
    val securityScore: Int = 0,
    val riskLevel: String = "UNKNOWN",
    val complianceLevel: String? = null,
    val regulatoryContext: RegulatoryContext? = null
)

data class DeviceInfo(
    val osVersion: String,
    val deviceModel: String,
    val isRooted: Boolean,
    val isDebuggerAttached: Boolean,
    val isAppTampered: Boolean,
    // Enhanced security fields
    val securityPatchLevel: String? = null,
    val bootloaderStatus: String? = null,
    val encryptionStatus: String? = null,
    val deviceIntegrityScore: Int = 0,
    val suspiciousApps: List<String> = emptyList(),
    val securityThreats: List<SecurityThreat> = emptyList()
)

data class BindingInfo(
    val simPresent: Boolean,
    val networkOperator: String?,
    val deviceBindingToken: String,
    // Enhanced binding fields
    val simOperatorCode: String? = null,
    val networkType: String? = null,
    val deviceFingerprint: String? = null,
    val hardwareAttestation: String? = null,
    val locationConsistency: Boolean = true
)

/**
 * Regulatory context for compliance reporting
 */
data class RegulatoryContext(
    val authorityId: String,
    val jurisdictionCode: String,
    val complianceLevel: String,
    val dataLocalization: Boolean = true,
    val auditTrail: List<ComplianceEvent> = emptyList()
)

/**
 * Security threat model
 */
data class SecurityThreat(
    val id: String,
    val type: ThreatType,
    val severity: ThreatSeverity,
    val description: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val mitigation: String? = null,
    val reportedToAuthority: Boolean = false,
    val metadata: Map<String, Any> = emptyMap()
) {
    enum class ThreatType {
        ROOT_DETECTION,
        DEBUGGER_ATTACHED,
        APP_TAMPERING,
        MALWARE_DETECTION,
        NETWORK_ATTACK,
        DEVICE_COMPROMISE,
        BEHAVIORAL_ANOMALY,
        POLICY_VIOLATION,
        UNAUTHORIZED_ACCESS
    }
    
    enum class ThreatSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    companion object {
        fun critical(type: String, description: String) = SecurityThreat(
            id = generateThreatId(),
            type = ThreatType.valueOf(type),
            severity = ThreatSeverity.CRITICAL,
            description = description
        )
        
        private fun generateThreatId(): String = "threat_${System.currentTimeMillis()}"
    }
}

/**
 * Policy violation model
 */
data class PolicyViolation(
    val id: String,
    val policyType: String,
    val violation: String,
    val severity: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis(),
    val context: Map<String, Any> = emptyMap(),
    val reportedToAuthority: Boolean = false
) {
    companion object {
        fun create(policyType: String, violation: String, severity: String, action: String) = 
            PolicyViolation(
                id = "violation_${System.currentTimeMillis()}",
                policyType = policyType,
                violation = violation,
                severity = severity,
                action = action
            )
    }
}

/**
 * Compliance event model
 */
data class ComplianceEvent(
    val id: String,
    val eventType: EventType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val regulatoryAuthorityId: String? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val requiresReporting: Boolean = false
) {
    enum class EventType {
        SDK_INITIALIZED,
        SECURITY_CHECK_PERFORMED,
        THREAT_DETECTED,
        POLICY_VIOLATION,
        COMPLIANCE_VERIFIED,
        DATA_LOCALIZED,
        AUDIT_EVENT,
        REGULATORY_REPORT
    }
    
    companion object {
        fun sdkInitialized(authorityId: String?) = ComplianceEvent(
            id = "event_${System.currentTimeMillis()}",
            eventType = EventType.SDK_INITIALIZED,
            description = "SFE SDK initialized with compliance mode",
            regulatoryAuthorityId = authorityId,
            requiresReporting = true
        )
        
        fun threatDetected(threat: SecurityThreat, authorityId: String?) = ComplianceEvent(
            id = "event_${System.currentTimeMillis()}",
            eventType = EventType.THREAT_DETECTED,
            description = "Security threat detected: ${threat.description}",
            regulatoryAuthorityId = authorityId,
            metadata = mapOf("threatId" to threat.id, "severity" to threat.severity),
            requiresReporting = threat.severity in listOf(SecurityThreat.ThreatSeverity.HIGH, SecurityThreat.ThreatSeverity.CRITICAL)
        )
    }
}

/**
 * Enhanced device binding information with security details
 */
data class DeviceBindingDetails(
    val bindingToken: String,
    val bindingStrength: BindingStrength,
    val validationMethods: List<ValidationMethod>,
    val lastValidated: Long,
    val trustScore: Int,
    val anomalies: List<String> = emptyList()
) {
    enum class BindingStrength {
        WEAK, MODERATE, STRONG, CRYPTOGRAPHIC
    }
    
    enum class ValidationMethod {
        SIM_BASED, HARDWARE_ATTESTATION, BIOMETRIC, BEHAVIORAL, LOCATION
    }
}

/**
 * Comprehensive security assessment result
 */
data class SecurityAssessment(
    val deviceId: String,
    val assessmentId: String,
    val timestamp: Long,
    val overallScore: Int,
    val riskLevel: String,
    val components: Map<String, ComponentAssessment>,
    val recommendations: List<SecurityRecommendation>,
    val complianceStatus: ComplianceStatus
)

data class ComponentAssessment(
    val component: String,
    val score: Int,
    val status: String,
    val issues: List<String>,
    val lastChecked: Long
)

data class SecurityRecommendation(
    val priority: Priority,
    val category: String,
    val description: String,
    val actionRequired: Boolean = false
) {
    enum class Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}

data class ComplianceStatus(
    val isCompliant: Boolean,
    val authorityId: String?,
    val certificationLevel: String?,
    val lastAudit: Long?,
    val violations: List<PolicyViolation> = emptyList(),
    val nextAuditDue: Long?
) 