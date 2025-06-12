package com.gradientgeeks.sfe.sfpaybackend.controllers;

import com.gradientgeeks.sfe.sfpaybackend.models.LoginRequest;
import com.gradientgeeks.sfe.sfpaybackend.models.LoginResponse;
import com.gradientgeeks.sfe.sfpaybackend.services.AuthenticationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/payment-app/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    /**
     * Login endpoint with SFE security assessment
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        
        try {
            LoginResponse response = authenticationService.authenticate(request);
            
            if (response.isSuccess()) {
                log.info("Login successful for user: {} (Risk: {})", 
                        request.getUsername(), 
                        response.getSecurityAssessment() != null ? 
                            response.getSecurityAssessment().getRiskLevel() : "UNKNOWN");
                return ResponseEntity.ok(response);
            } else {
                log.warn("Login failed for user: {} - {}", request.getUsername(), response.getMessage());
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing login request for user: {}", request.getUsername(), e);
            
            LoginResponse errorResponse = LoginResponse.failure("Authentication service temporarily unavailable");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.builder()
            .status("UP")
            .service("Authentication Service")
            .timestamp(System.currentTimeMillis())
            .build());
    }
    
    /**
     * Get authentication info (for testing/debugging)
     */
    @GetMapping("/info")
    public ResponseEntity<AuthInfo> getAuthInfo() {
        return ResponseEntity.ok(AuthInfo.builder()
            .supportedMethods(new String[]{"SFE_ENHANCED", "BASIC"})
            .tokenType("JWT")
            .tokenValiditySeconds(3600)
            .requiresSfePayload(false) // Optional for enhanced security
            .build());
    }
    
    @lombok.Data
    @lombok.Builder
    public static class HealthResponse {
        private String status;
        private String service;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AuthInfo {
        private String[] supportedMethods;
        private String tokenType;
        private long tokenValiditySeconds;
        private boolean requiresSfePayload;
    }
} 