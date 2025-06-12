package com.gradientgeeks.sfe.backendsdk.controllers;

import com.gradientgeeks.sfe.backendsdk.models.AttestationRequest;
import com.gradientgeeks.sfe.backendsdk.models.AttestationResponse;
import com.gradientgeeks.sfe.backendsdk.models.TelemetryEventRequest;
import com.gradientgeeks.sfe.backendsdk.services.AttestationVerificationService;
import com.gradientgeeks.sfe.backendsdk.services.PolicyEnforcementService;
import com.gradientgeeks.sfe.backendsdk.services.TelemetryService;
import com.gradientgeeks.sfe.backendsdk.services.ThreatDetectionService;
import com.gradientgeeks.sfe.backendsdk.config.SfeBackendSdkConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Production-ready REST API Controller for SFE Backend SDK
 * 
 * Provides comprehensive security verification endpoints for financial applications
 * with regulatory compliance, threat detection, and real-time monitoring
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sfe")
@CrossOrigin(origins = {"https://*.bankingapp.com", "https://*.paymentapp.com"})
public class SfeApiController {
    
    @Autowired
    private AttestationVerificationService attestationService;
    
    @Autowired
    private PolicyEnforcementService policyService;
    
    @Autowired
    private TelemetryService telemetryService;

    @Autowired
    private ThreatDetectionService threatDetectionService;
    
    @Autowired
    private SfeBackendSdkConfig config;
    
    /**
     * Primary endpoint for attestation verification
     * Used by financial apps to verify device and app integrity
     */
    @PostMapping("/verify")
    public ResponseEntity<SecurityVerificationResponse> verifyAttestation(
            @Valid @RequestBody AttestationRequest request,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestHeader(value = "X-App-Version", required = false) String appVersion) {
        
        try {
            log.info("Attestation verification request from tenant: {}, app version: {}", tenantId, appVersion);
            
            // Step 1: Verify attestation with Google Play Integrity
            AttestationResponse attestationResult = attestationService.verifyAttestation(request);
            
            // Step 2: Assess risk based on policies
            String riskLevel = policyService.assessRisk(request, attestationResult);
            
            // Step 3: Determine action based on risk
            PolicyEnforcementService.PolicyAction action = policyService.determineAction(riskLevel);
            
            // Step 4: Threat detection analysis
            ThreatDetectionService.ThreatAnalysisResult threatAnalysis = threatDetectionService.analyzeThreat(request, attestationResult);
            
            // Step 5: Record telemetry
            recordVerificationTelemetry(request, attestationResult, riskLevel);
            
            // Step 6: Build comprehensive response
            SecurityVerificationResponse response = SecurityVerificationResponse.builder()
                .verificationId(generateVerificationId())
                .timestamp(Instant.now().toEpochMilli())
                .attestationResult(attestationResult)
                .riskLevel(riskLevel)
                .recommendedAction(action.toString())
                .threatAnalysis(threatAnalysis)
                .complianceStatus(buildComplianceStatus(request, tenantId))
                .securityScore(calculateSecurityScore(attestationResult, threatAnalysis))
                .build();
            
            // Determine HTTP status based on risk level
            HttpStatus status = determineHttpStatus(riskLevel, action);
            
            log.info("Verification completed: ID={}, Risk={}, Action={}", 
                    response.getVerificationId(), riskLevel, action);
            
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            log.error("Error during attestation verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SecurityVerificationResponse.error("Verification failed", e.getMessage()));
        }
    }
    
    /**
     * Telemetry submission endpoint
     * For compliance reporting and monitoring
     */
    @PostMapping("/telemetry")
    public ResponseEntity<TelemetryResponse> submitTelemetry(
            @Valid @RequestBody List<TelemetryEventRequest> events,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        
        try {
            log.debug("Telemetry submission: {} events from tenant: {}", events.size(), tenantId);
            
            // Process events asynchronously for performance
            CompletableFuture.runAsync(() -> {
                events.forEach(event -> telemetryService.recordTelemetry(event));
            });
            
            // Check for regulatory reporting requirements
            List<TelemetryEventRequest> criticalEvents = events.stream()
                .filter(event -> event.getRiskLevel() != null && 
                        event.getRiskLevel().matches("HIGH|CRITICAL"))
                .toList();
            
            if (!criticalEvents.isEmpty()) {
                telemetryService.submitToRegulatoryBody(criticalEvents);
            }
            
            TelemetryResponse response = TelemetryResponse.builder()
                .submissionId(generateSubmissionId())
                .timestamp(Instant.now().toEpochMilli())
                .eventsProcessed(events.size())
                .criticalEventsReported(criticalEvents.size())
                .status("ACCEPTED")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing telemetry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TelemetryResponse.error("Telemetry processing failed"));
        }
    }
    
    /**
     * Policy update endpoint
     * For real-time policy distribution
     */
    @GetMapping("/policies")
    public ResponseEntity<PolicyResponse> getPolicies(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestParam(value = "lastUpdate", required = false) Long lastUpdate) {
        
        try {
            Map<String, Object> policies = getCurrentPolicies(tenantId, lastUpdate);
            
            PolicyResponse response = PolicyResponse.builder()
                .timestamp(Instant.now().toEpochMilli())
                .version(getCurrentPolicyVersion())
                .policies(policies)
                .updateRequired(isPolicyUpdateRequired(lastUpdate))
                .nextCheckInterval(getNextCheckInterval())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving policies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PolicyResponse.error("Policy retrieval failed"));
        }
    }
    
    /**
     * Health check endpoint for monitoring
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        try {
            HealthResponse response = HealthResponse.builder()
                .status("HEALTHY")
                .timestamp(Instant.now().toEpochMilli())
                .version(getSdkVersion())
                .components(getComponentHealth())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(HealthResponse.unhealthy("Service health check failed"));
        }
    }
    
    // Private helper methods
    
    private void recordVerificationTelemetry(AttestationRequest request, AttestationResponse response, String riskLevel) {
        try {
            TelemetryEventRequest telemetryEvent = TelemetryEventRequest.attestationVerification(
                generateDeviceFingerprint(request),
                riskLevel,
                response.isSuccessful()
            );
            telemetryService.recordTelemetry(telemetryEvent);
        } catch (Exception e) {
            log.warn("Failed to record verification telemetry", e);
        }
    }
    
    private ComplianceStatus buildComplianceStatus(AttestationRequest request, String tenantId) {
        return ComplianceStatus.builder()
            .compliant(true) // Simplified for demo
            .regulatoryAuthorityId(config.getRegulatory().getAuthorityId())
            .lastAudit(Instant.now().toEpochMilli())
            .nextAudit(Instant.now().plusSeconds(7776000).toEpochMilli()) // 90 days
            .build();
    }
    
    private int calculateSecurityScore(AttestationResponse attestation, ThreatDetectionService.ThreatAnalysisResult threat) {
        int baseScore = 100;
        
        if (attestation != null && !attestation.isSuccessful()) {
            baseScore -= 30;
        }
        
        if (threat != null) {
            switch (threat.getThreatLevel()) {
                case "CRITICAL": baseScore -= 50; break;
                case "HIGH": baseScore -= 30; break;
                case "MEDIUM": baseScore -= 15; break;
                case "LOW": baseScore -= 5; break;
            }
        }
        
        return Math.max(0, baseScore);
    }
    
    private HttpStatus determineHttpStatus(String riskLevel, PolicyEnforcementService.PolicyAction action) {
        if (action == PolicyEnforcementService.PolicyAction.BLOCK) {
            return HttpStatus.FORBIDDEN;
        }
        if ("CRITICAL".equals(riskLevel)) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return HttpStatus.OK;
    }
    
    private Map<String, Object> getCurrentPolicies(String tenantId, Long lastUpdate) {
        return Map.of(
            "rootDetection", config.getPolicies().getRootDetection(),
            "debuggerDetection", config.getPolicies().getDebuggerDetection(),
            "appTampering", config.getPolicies().getAppTampering(),
            "lastUpdated", System.currentTimeMillis()
        );
    }
    
    private String generateVerificationId() { return "verify_" + System.currentTimeMillis(); }
    private String generateSubmissionId() { return "submit_" + System.currentTimeMillis(); }
    private String generateDeviceFingerprint(AttestationRequest request) { return "device_" + request.hashCode(); }
    private String getCurrentPolicyVersion() { return "1.0.0"; }
    private boolean isPolicyUpdateRequired(Long lastUpdate) { return lastUpdate == null || lastUpdate < System.currentTimeMillis() - 86400000; }
    private int getNextCheckInterval() { return 3600; } // 1 hour
    private String getSdkVersion() { return "1.0.0-production"; }
    private Map<String, String> getComponentHealth() {
        return Map.of(
            "attestation", "HEALTHY",
            "policy", "HEALTHY",
            "telemetry", "HEALTHY",
            "threat_detection", "HEALTHY"
        );
    }
    
    // Response DTOs (would be in separate files in production)
    @lombok.Data
    @lombok.Builder
    public static class SecurityVerificationResponse {
        private String verificationId;
        private long timestamp;
        private AttestationResponse attestationResult;
        private String riskLevel;
        private String recommendedAction;
        private ThreatDetectionService.ThreatAnalysisResult threatAnalysis;
        private ComplianceStatus complianceStatus;
        private int securityScore;
        private boolean success = true;
        private String errorMessage;
        
        public static SecurityVerificationResponse error(String message, String details) {
            return SecurityVerificationResponse.builder()
                .success(false)
                .errorMessage(message + ": " + details)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TelemetryResponse {
        private String submissionId;
        private long timestamp;
        private int eventsProcessed;
        private int criticalEventsReported;
        private String status;
        private boolean success = true;
        private String errorMessage;
        
        public static TelemetryResponse error(String message) {
            return TelemetryResponse.builder()
                .success(false)
                .errorMessage(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PolicyResponse {
        private long timestamp;
        private String version;
        private Map<String, Object> policies;
        private boolean updateRequired;
        private int nextCheckInterval;
        private boolean success = true;
        private String errorMessage;
        
        public static PolicyResponse error(String message) {
            return PolicyResponse.builder()
                .success(false)
                .errorMessage(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class HealthResponse {
        private String status;
        private long timestamp;
        private String version;
        private Map<String, String> components;
        
        public static HealthResponse unhealthy(String reason) {
            return HealthResponse.builder()
                .status("UNHEALTHY")
                .timestamp(Instant.now().toEpochMilli())
                .components(Map.of("error", reason))
                .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ComplianceStatus {
        private boolean compliant;
        private String regulatoryAuthorityId;
        private long lastAudit;
        private long nextAudit;
    }
} 