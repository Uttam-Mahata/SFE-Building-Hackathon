package com.gradientgeeks.sfe.backendsdk.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttestationResponse {
    
    public enum Status {
        SUCCESS,
        FAILED,
        INVALID_TOKEN,
        VERIFICATION_ERROR
    }
    
    private Status status;
    private String message;
    private IntegrityVerdicts integrityVerdicts;
    private String riskLevel;
    private long timestamp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntegrityVerdicts {
        private boolean meetsDeviceIntegrity;
        private boolean meetsBasicIntegrity;
        private String appIntegrityVerdict;
        private String deviceRecognitionVerdict;
        private String environmentDetails;
    }
    
    /**
     * Factory method for successful response
     */
    public static AttestationResponse success(IntegrityVerdicts verdicts, String riskLevel) {
        return AttestationResponse.builder()
            .status(Status.SUCCESS)
            .message("Attestation verification successful")
            .integrityVerdicts(verdicts)
            .riskLevel(riskLevel)
            .timestamp(Instant.now().toEpochMilli())
            .build();
    }
    
    /**
     * Factory method for failed response
     */
    public static AttestationResponse failed(String message) {
        return AttestationResponse.builder()
            .status(Status.FAILED)
            .message(message)
            .riskLevel("CRITICAL")
            .timestamp(Instant.now().toEpochMilli())
            .build();
    }
    
    /**
     * Factory method for invalid token response
     */
    public static AttestationResponse invalidToken(String message) {
        return AttestationResponse.builder()
            .status(Status.INVALID_TOKEN)
            .message(message)
            .riskLevel("HIGH")
            .timestamp(Instant.now().toEpochMilli())
            .build();
    }
    
    /**
     * Check if the attestation was successful
     */
    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }
    
    /**
     * Check if the device meets integrity requirements
     */
    public boolean meetsIntegrityRequirements() {
        return integrityVerdicts != null && 
               integrityVerdicts.meetsDeviceIntegrity && 
               integrityVerdicts.meetsBasicIntegrity;
    }
} 