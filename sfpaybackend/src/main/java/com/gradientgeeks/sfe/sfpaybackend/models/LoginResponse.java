package com.gradientgeeks.sfe.sfpaybackend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private boolean success;
    private String message;
    private String token;
    @lombok.Builder.Default
    private String tokenType = "Bearer";
    private long expiresIn; // Token expiration time in seconds
    private UserInfo userInfo;
    private SecurityAssessment securityAssessment;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private String userId;
        private String username;
        private String email;
        private String role;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SecurityAssessment {
        private String riskLevel;
        private boolean deviceTrusted;
        private String assessmentId;
        private long assessmentTimestamp;
    }
    
    public static LoginResponse success(String token, UserInfo userInfo, SecurityAssessment security) {
        return LoginResponse.builder()
            .success(true)
            .message("Login successful")
            .token(token)
            .tokenType("Bearer")
            .expiresIn(3600) // 1 hour
            .userInfo(userInfo)
            .securityAssessment(security)
            .build();
    }
    
    public static LoginResponse failure(String message) {
        return LoginResponse.builder()
            .success(false)
            .message(message)
            .build();
    }
}