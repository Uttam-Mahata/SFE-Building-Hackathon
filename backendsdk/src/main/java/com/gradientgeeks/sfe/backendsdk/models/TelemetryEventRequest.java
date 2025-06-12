package com.gradientgeeks.sfe.backendsdk.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryEventRequest {
    
    public enum EventType {
        SECURITY_CHECK,
        ATTESTATION_VERIFICATION,
        POLICY_VIOLATION,
        RISK_ASSESSMENT,
        TRANSACTION_BLOCKED,
        DEVICE_BINDING_FAILURE
    }
    
    private EventType eventType;
    private String eventId;
    private long timestamp;
    private String deviceFingerprint; // Anonymized device identifier
    private String riskLevel;
    private Map<String, Object> eventData;
    private boolean isAnonymized;
    
    /**
     * Factory method for security check events
     */
    public static TelemetryEventRequest securityCheck(String deviceFingerprint, String riskLevel, Map<String, Object> data) {
        return TelemetryEventRequest.builder()
            .eventType(EventType.SECURITY_CHECK)
            .eventId(generateEventId())
            .timestamp(Instant.now().toEpochMilli())
            .deviceFingerprint(deviceFingerprint)
            .riskLevel(riskLevel)
            .eventData(data)
            .isAnonymized(false)
            .build();
    }
    
    /**
     * Factory method for attestation verification events
     */
    public static TelemetryEventRequest attestationVerification(String deviceFingerprint, String riskLevel, boolean successful) {
        Map<String, Object> data = Map.of(
            "successful", successful,
            "attestationType", "play_integrity"
        );
        
        return TelemetryEventRequest.builder()
            .eventType(EventType.ATTESTATION_VERIFICATION)
            .eventId(generateEventId())
            .timestamp(Instant.now().toEpochMilli())
            .deviceFingerprint(deviceFingerprint)
            .riskLevel(riskLevel)
            .eventData(data)
            .isAnonymized(false)
            .build();
    }
    
    /**
     * Factory method for policy violation events
     */
    public static TelemetryEventRequest policyViolation(String deviceFingerprint, String violationType, String action) {
        Map<String, Object> data = Map.of(
            "violationType", violationType,
            "actionTaken", action
        );
        
        return TelemetryEventRequest.builder()
            .eventType(EventType.POLICY_VIOLATION)
            .eventId(generateEventId())
            .timestamp(Instant.now().toEpochMilli())
            .deviceFingerprint(deviceFingerprint)
            .riskLevel("HIGH")
            .eventData(data)
            .isAnonymized(false)
            .build();
    }
    
    private static String generateEventId() {
        return "evt_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * Mark this event as anonymized
     */
    public TelemetryEventRequest anonymize() {
        this.isAnonymized = true;
        return this;
    }
}