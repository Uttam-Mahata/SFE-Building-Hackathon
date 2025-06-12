package com.gradientgeeks.sfe.backendsdk.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttestationRequest {
    
    @NotBlank
    private String appVersion;
    
    @NotBlank
    private String sdkVersion;
    
    @NotNull
    private String timestamp;
    
    @NotNull
    private DeviceInfo deviceInfo;
    
    @NotNull
    private BindingInfo bindingInfo;
    
    private String attestationToken;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceInfo {
        private String osVersion;
        private String deviceModel;
        private boolean isRooted;
        private boolean isDebuggerAttached;
        private boolean isAppTampered;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BindingInfo {
        private boolean simPresent;
        private String networkOperator;
        private String deviceBindingToken;
    }
    
    /**
     * Parse timestamp as Instant
     */
    public Instant getTimestampAsInstant() {
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return Instant.now();
        }
    }
    
    /**
     * Check if this is a high-risk device based on security flags
     */
    public boolean isHighRiskDevice() {
        return deviceInfo != null && (
            deviceInfo.isRooted || 
            deviceInfo.isAppTampered || 
            deviceInfo.isDebuggerAttached
        );
    }
} 