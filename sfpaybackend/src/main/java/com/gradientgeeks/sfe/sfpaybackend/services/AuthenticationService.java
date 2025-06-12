package com.gradientgeeks.sfe.sfpaybackend.services;

import com.gradientgeeks.sfe.backendsdk.models.AttestationRequest;
import com.gradientgeeks.sfe.backendsdk.models.AttestationResponse;
import com.gradientgeeks.sfe.backendsdk.models.TelemetryEventRequest;
import com.gradientgeeks.sfe.backendsdk.services.AttestationVerificationService;
import com.gradientgeeks.sfe.backendsdk.services.PolicyEnforcementService;
import com.gradientgeeks.sfe.backendsdk.services.TelemetryService;
import com.gradientgeeks.sfe.backendsdk.utils.SecurityUtils;

import com.gradientgeeks.sfe.sfpaybackend.models.LoginRequest;
import com.gradientgeeks.sfe.sfpaybackend.models.LoginResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthenticationService {
    
    private final JwtTokenService jwtTokenService;
    private final AttestationVerificationService attestationService;
    private final PolicyEnforcementService policyService;
    private final TelemetryService telemetryService;
    private final PasswordEncoder passwordEncoder;
    private final Gson gson;
    private final Map<String, MockUser> users;
    
    public AuthenticationService(
            JwtTokenService jwtTokenService,
            AttestationVerificationService attestationService,
            PolicyEnforcementService policyService,
            TelemetryService telemetryService,
            PasswordEncoder passwordEncoder) {
        this.jwtTokenService = jwtTokenService;
        this.attestationService = attestationService;
        this.policyService = policyService;
        this.telemetryService = telemetryService;
        this.passwordEncoder = passwordEncoder;
        this.gson = new Gson();
        this.users = initializeMockUsers();
    }
    
    public LoginResponse authenticate(LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());
        
        try {
            MockUser user = validateCredentials(loginRequest.getUsername(), loginRequest.getPassword());
            if (user == null) {
                log.warn("Authentication failed for user: {}", loginRequest.getUsername());
                // Record failed authentication telemetry
                recordAuthenticationTelemetry(loginRequest.getUsername(), "HIGH", false);
                return LoginResponse.failure("Invalid username or password");
            }
            
            LoginResponse.SecurityAssessment securityAssessment = null;
            if (loginRequest.getSfePayload() != null && !loginRequest.getSfePayload().trim().isEmpty()) {
                securityAssessment = performSecurityAssessment(loginRequest.getSfePayload(), user.getUsername());
                
                if (securityAssessment != null && "CRITICAL".equals(securityAssessment.getRiskLevel())) {
                    log.warn("Login blocked due to critical security risk for user: {}", loginRequest.getUsername());
                    return LoginResponse.failure("Authentication blocked due to security concerns");
                }
            } else {
                securityAssessment = LoginResponse.SecurityAssessment.builder()
                    .riskLevel("MEDIUM")
                    .deviceTrusted(false)
                    .assessmentId(UUID.randomUUID().toString())
                    .assessmentTimestamp(Instant.now().toEpochMilli())
                    .build();
            }
            
            String token = jwtTokenService.generateTokenWithSecurity(
                user.getUsername(),
                user.getUserId(),
                user.getRole(),
                securityAssessment.getRiskLevel(),
                securityAssessment.isDeviceTrusted()
            );
            
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
            
            // Record authentication telemetry
            recordAuthenticationTelemetry(loginRequest.getUsername(), securityAssessment.getRiskLevel(), true);
            
            log.info("Authentication successful for user: {}", loginRequest.getUsername());
            return LoginResponse.success(token, userInfo, securityAssessment);
            
        } catch (Exception e) {
            log.error("Error during authentication", e);
            return LoginResponse.failure("Authentication failed");
        }
    }
    
    private MockUser validateCredentials(String username, String password) {
        MockUser user = users.get(username.toLowerCase());
        if (user != null && passwordEncoder.matches(password, user.getHashedPassword())) {
            return user;
        }
        return null;
    }
    
    private LoginResponse.SecurityAssessment performSecurityAssessment(String sfePayload, String username) {
        try {
            AttestationRequest attestationRequest = gson.fromJson(sfePayload, AttestationRequest.class);
            if (attestationRequest == null) {
                return null;
            }
            
            AttestationResponse attestationResponse = attestationService.verifyAttestation(attestationRequest);
            String riskLevel = policyService.assessRisk(attestationRequest, attestationResponse);
            
            return LoginResponse.SecurityAssessment.builder()
                .riskLevel(riskLevel)
                .deviceTrusted(attestationResponse.meetsIntegrityRequirements())
                .assessmentId(UUID.randomUUID().toString())
                .assessmentTimestamp(Instant.now().toEpochMilli())
                .build();
                
        } catch (Exception e) {
            log.error("Error performing security assessment", e);
            return LoginResponse.SecurityAssessment.builder()
                .riskLevel("HIGH")
                .deviceTrusted(false)
                .assessmentId(UUID.randomUUID().toString())
                .assessmentTimestamp(Instant.now().toEpochMilli())
                .build();
        }
    }
    
    private Map<String, MockUser> initializeMockUsers() {
        Map<String, MockUser> mockUsers = new HashMap<>();
        
        mockUsers.put("demo@gradientgeeks.com", MockUser.builder()
            .userId("user_001")
            .username("demo@gradientgeeks.com")
            .email("demo@gradientgeeks.com")
            .hashedPassword(passwordEncoder.encode("demo123"))
            .role("USER")
            .enabled(true)
            .build());
            
        mockUsers.put("admin@gradientgeeks.com", MockUser.builder()
            .userId("user_002")
            .username("admin@gradientgeeks.com")
            .email("admin@gradientgeeks.com")
            .hashedPassword(passwordEncoder.encode("admin123"))
            .role("ADMIN")
            .enabled(true)
            .build());
        
        return mockUsers;
    }
    
    public MockUser getUserByUsername(String username) {
        return users.get(username.toLowerCase());
    }
    
    /**
     * Record authentication telemetry event
     */
    private void recordAuthenticationTelemetry(String username, String riskLevel, boolean successful) {
        try {
            String deviceFingerprint = SecurityUtils.hashDeviceId(username);
            
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("authenticationMethod", "sfe_enhanced");
            eventData.put("successful", successful);
            
            TelemetryEventRequest telemetryEvent = TelemetryEventRequest.builder()
                .eventType(TelemetryEventRequest.EventType.SECURITY_CHECK)
                .timestamp(System.currentTimeMillis())
                .deviceFingerprint(deviceFingerprint)
                .riskLevel(riskLevel)
                .eventData(eventData)
                .build();
            
            telemetryService.recordTelemetry(telemetryEvent);
            
        } catch (Exception e) {
            log.error("Error recording authentication telemetry", e);
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class MockUser {
        private String userId;
        private String username;
        private String email;
        private String hashedPassword;
        private String role;
        private boolean enabled;
    }
}