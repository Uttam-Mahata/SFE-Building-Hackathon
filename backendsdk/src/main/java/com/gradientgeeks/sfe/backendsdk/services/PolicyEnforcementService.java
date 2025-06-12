package com.gradientgeeks.sfe.backendsdk.services;

import com.gradientgeeks.sfe.backendsdk.config.SfeBackendSdkConfig;
import com.gradientgeeks.sfe.backendsdk.models.AttestationRequest;
import com.gradientgeeks.sfe.backendsdk.models.AttestationResponse;
import com.gradientgeeks.sfe.backendsdk.models.TelemetryEventRequest;
import com.gradientgeeks.sfe.backendsdk.utils.SecurityUtils;
import com.gradientgeeks.sfe.backendsdk.security.TenantContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PolicyEnforcementService {
    
    private final SfeBackendSdkConfig defaultConfig;
    private final TelemetryService telemetryService;
    
    public PolicyEnforcementService(SfeBackendSdkConfig config, TelemetryService telemetryService) {
        this.defaultConfig = config;
        this.telemetryService = telemetryService;
    }
    
    private SfeBackendSdkConfig.PolicyConfig getPoliciesForCurrentTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null && defaultConfig.getMultiTenant().isEnableMultiTenant()) {
            SfeBackendSdkConfig.TenantConfig tenantConfig = defaultConfig.getMultiTenant().getTenants().get(tenantId);
            if (tenantConfig != null && tenantConfig.getPolicies() != null) {
                log.debug("Using policies for tenant: {}", tenantId);
                return tenantConfig.getPolicies();
            }
        }
        log.debug("Using default policies");
        return defaultConfig.getPolicies();
    }
    
    /**
     * Assess risk level based on attestation data and security policies
     */
    public String assessRisk(AttestationRequest frontendData, AttestationResponse attestationResult) {
        log.debug("Assessing risk for device: {}", 
                SecurityUtils.hashDeviceId(frontendData.getBindingInfo().getDeviceBindingToken()));
        
        SfeBackendSdkConfig.PolicyConfig currentPolicies = getPoliciesForCurrentTenant();
        String highestRiskLevel = "LOW";
        Map<String, Object> policyViolations = new HashMap<>();
        
        // Check root detection policy
        if (frontendData.getDeviceInfo() != null && frontendData.getDeviceInfo().isRooted()) {
            String riskLevel = currentPolicies.getRootDetection().getRiskLevel();
            String action = currentPolicies.getRootDetection().getAction();
            
            policyViolations.put("rootDetection", Map.of("action", action, "riskLevel", riskLevel));
            highestRiskLevel = getHigherRiskLevel(highestRiskLevel, riskLevel);
            
            log.warn("Root detection violation - Action: {}, Risk: {}", action, riskLevel);
        }
        
        // Check debugger detection policy
        if (frontendData.getDeviceInfo() != null && frontendData.getDeviceInfo().isDebuggerAttached()) {
            String riskLevel = currentPolicies.getDebuggerDetection().getRiskLevel();
            String action = currentPolicies.getDebuggerDetection().getAction();
            
            policyViolations.put("debuggerDetection", Map.of("action", action, "riskLevel", riskLevel));
            highestRiskLevel = getHigherRiskLevel(highestRiskLevel, riskLevel);
            
            log.warn("Debugger detection violation - Action: {}, Risk: {}", action, riskLevel);
        }
        
        // Check app tampering policy
        if (frontendData.getDeviceInfo() != null && frontendData.getDeviceInfo().isAppTampered()) {
            String riskLevel = currentPolicies.getAppTampering().getRiskLevel();
            String action = currentPolicies.getAppTampering().getAction();
            
            policyViolations.put("appTampering", Map.of("action", action, "riskLevel", riskLevel));
            highestRiskLevel = getHigherRiskLevel(highestRiskLevel, riskLevel);
            
            log.warn("App tampering violation - Action: {}, Risk: {}", action, riskLevel);
        }
        
        // Check attestation result
        if (attestationResult != null && !attestationResult.meetsIntegrityRequirements()) {
            highestRiskLevel = getHigherRiskLevel(highestRiskLevel, "HIGH");
            policyViolations.put("attestationFailure", Map.of("status", attestationResult.getStatus()));
            
            log.warn("Attestation integrity failure");
        }
        
        // Check device binding
        if (frontendData.getBindingInfo() != null && !frontendData.getBindingInfo().isSimPresent()) {
            highestRiskLevel = getHigherRiskLevel(highestRiskLevel, "MEDIUM");
            policyViolations.put("deviceBinding", Map.of("issue", "sim_not_present"));
            
            log.info("Device binding issue: SIM not present");
        }
        
        // Record telemetry if there are violations
        if (!policyViolations.isEmpty()) {
            recordPolicyViolations(frontendData, policyViolations);
        }
        
        log.info("Risk assessment completed: {}", highestRiskLevel);
        return highestRiskLevel;
    }
    
    /**
     * Check if this is a high-risk device
     */
    public boolean isHighRiskDevice(AttestationRequest request) {
        if (request.getDeviceInfo() == null) {
            return false;
        }
        
        return request.getDeviceInfo().isRooted() || 
               request.getDeviceInfo().isAppTampered() || 
               request.getDeviceInfo().isDebuggerAttached();
    }
    
    /**
     * Determine the action to take based on risk level
     */
    public PolicyAction determineAction(String riskLevel) {
        switch (riskLevel.toUpperCase()) {
            case "CRITICAL":
                return PolicyAction.BLOCK;
            case "HIGH":
                return PolicyAction.REQUIRE_ADDITIONAL_AUTH;
            case "MEDIUM":
                return PolicyAction.MONITOR;
            case "LOW":
            default:
                return PolicyAction.ALLOW;
        }
    }
    
    /**
     * Evaluate specific security policy
     */
    public PolicyEvaluation evaluatePolicy(String policyType, AttestationRequest request) {
        SfeBackendSdkConfig.PolicyConfig currentPolicies = getPoliciesForCurrentTenant();
        switch (policyType.toLowerCase()) {
            case "root_detection":
                return evaluateRootDetectionPolicy(request, currentPolicies);
            case "debugger_detection":
                return evaluateDebuggerDetectionPolicy(request, currentPolicies);
            case "app_tampering":
                return evaluateAppTamperingPolicy(request, currentPolicies);
            default:
                return PolicyEvaluation.builder()
                    .policyType(policyType)
                    .violated(false)
                    .action(PolicyAction.ALLOW)
                    .riskLevel("LOW")
                    .message("Unknown policy type")
                    .build();
        }
    }
    
    private PolicyEvaluation evaluateRootDetectionPolicy(AttestationRequest request, SfeBackendSdkConfig.PolicyConfig currentPolicies) {
        boolean violated = request.getDeviceInfo() != null && request.getDeviceInfo().isRooted();
        SfeBackendSdkConfig.PolicyConfig.SecurityPolicy policy = currentPolicies.getRootDetection();
        
        return PolicyEvaluation.builder()
            .policyType("root_detection")
            .violated(violated)
            .action(violated ? PolicyAction.valueOf(policy.getAction()) : PolicyAction.ALLOW)
            .riskLevel(violated ? policy.getRiskLevel() : "LOW")
            .message(violated ? "Device is rooted" : "Device is not rooted")
            .build();
    }
    
    private PolicyEvaluation evaluateDebuggerDetectionPolicy(AttestationRequest request, SfeBackendSdkConfig.PolicyConfig currentPolicies) {
        boolean violated = request.getDeviceInfo() != null && request.getDeviceInfo().isDebuggerAttached();
        SfeBackendSdkConfig.PolicyConfig.SecurityPolicy policy = currentPolicies.getDebuggerDetection();
        
        return PolicyEvaluation.builder()
            .policyType("debugger_detection")
            .violated(violated)
            .action(violated ? PolicyAction.valueOf(policy.getAction()) : PolicyAction.ALLOW)
            .riskLevel(violated ? policy.getRiskLevel() : "LOW")
            .message(violated ? "Debugger is attached" : "No debugger detected")
            .build();
    }
    
    private PolicyEvaluation evaluateAppTamperingPolicy(AttestationRequest request, SfeBackendSdkConfig.PolicyConfig currentPolicies) {
        boolean violated = request.getDeviceInfo() != null && request.getDeviceInfo().isAppTampered();
        SfeBackendSdkConfig.PolicyConfig.SecurityPolicy policy = currentPolicies.getAppTampering();
        
        return PolicyEvaluation.builder()
            .policyType("app_tampering")
            .violated(violated)
            .action(violated ? PolicyAction.valueOf(policy.getAction()) : PolicyAction.ALLOW)
            .riskLevel(violated ? policy.getRiskLevel() : "LOW")
            .message(violated ? "App has been tampered with" : "App integrity verified")
            .build();
    }
    
    private void recordPolicyViolations(AttestationRequest request, Map<String, Object> violations) {
        try {
            String deviceFingerprint = SecurityUtils.generateDeviceFingerprint(
                request.getDeviceInfo().getDeviceModel(),
                request.getDeviceInfo().getOsVersion(),
                request.getBindingInfo().getNetworkOperator(),
                request.getBindingInfo().getDeviceBindingToken()
            );
            
            TelemetryEventRequest telemetryEvent = TelemetryEventRequest.policyViolation(
                deviceFingerprint, 
                violations.keySet().toString(),
                "MULTIPLE_VIOLATIONS"
            );
            
            telemetryService.recordTelemetry(telemetryEvent);
        } catch (Exception e) {
            log.error("Error recording policy violations telemetry", e);
        }
    }
    
    private String getHigherRiskLevel(String current, String candidate) {
        if (SecurityUtils.compareRiskLevels(candidate, current) > 0) {
            return candidate;
        }
        return current;
    }
    
    public enum PolicyAction {
        ALLOW,
        MONITOR,
        REQUIRE_ADDITIONAL_AUTH,
        BLOCK
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PolicyEvaluation {
        private String policyType;
        private boolean violated;
        private PolicyAction action;
        private String riskLevel;
        private String message;
    }
} 