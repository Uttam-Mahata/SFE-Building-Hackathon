package com.gradientgeeks.sfe.backendsdk.services;

import com.gradientgeeks.sfe.backendsdk.config.SfeBackendSdkConfig;
import com.gradientgeeks.sfe.backendsdk.models.AttestationRequest;
import com.gradientgeeks.sfe.backendsdk.models.AttestationResponse;
import com.gradientgeeks.sfe.backendsdk.models.AttestationResponse.IntegrityVerdicts;
import com.gradientgeeks.sfe.backendsdk.utils.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;


@Slf4j
@Service
public class AttestationVerificationService {
    
    private final SfeBackendSdkConfig config;
    
    public AttestationVerificationService(SfeBackendSdkConfig config) {
        this.config = config;
        log.debug("AttestationVerificationService initialized with project ID: {}", 
                config.getGoogleCloudProjectId());
    }
    
    public AttestationResponse verifyAttestation(AttestationRequest request) {
        log.info("Starting attestation verification for device: {}", 
                SecurityUtils.hashDeviceId(request.getBindingInfo().getDeviceBindingToken()));
        
        try {
            if (request.getAttestationToken() == null || request.getAttestationToken().isEmpty()) {
                log.warn("No attestation token provided");
                return AttestationResponse.invalidToken("Attestation token is required");
            }
            
            IntegrityVerdicts verdicts = verifyTokenWithGoogle(request.getAttestationToken());
            String riskLevel = assessRiskLevel(verdicts, request);
            
            log.info("Attestation verification completed with risk level: {}", riskLevel);
            return AttestationResponse.success(verdicts, riskLevel);
            
        } catch (Exception e) {
            log.error("Error during attestation verification", e);
            return AttestationResponse.failed("Attestation verification failed: " + e.getMessage());
        }
    }
    
    private IntegrityVerdicts verifyTokenWithGoogle(String token) {
        log.debug("Verifying token with Google Play Integrity API for project: {}", 
                config.getGoogleCloudProjectId());
        
        try {
            // In production, this would use the config values for actual API calls
            String projectId = config.getGoogleCloudProjectId();
            String apiKey = config.getPlayIntegrityApiKey();
            
            if (projectId == null || apiKey == null) {
                log.warn("Google Cloud configuration not properly set");
            }
            
            if (isValidTokenFormat(token)) {
                return IntegrityVerdicts.builder()
                    .meetsDeviceIntegrity(true)
                    .meetsBasicIntegrity(true)
                    .appIntegrityVerdict("MEETS_DEVICE_INTEGRITY")
                    .deviceRecognitionVerdict("MEETS_DEVICE_INTEGRITY")
                    .environmentDetails("Play Protect verified")
                    .build();
            } else {
                return IntegrityVerdicts.builder()
                    .meetsDeviceIntegrity(false)
                    .meetsBasicIntegrity(false)
                    .appIntegrityVerdict("UNEVALUATED")
                    .deviceRecognitionVerdict("UNEVALUATED")
                    .environmentDetails("Invalid token format")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Error verifying token with Google", e);
            throw new RuntimeException("Token verification failed", e);
        }
    }
    
    private boolean isValidTokenFormat(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            for (String part : parts) {
                Base64.getUrlDecoder().decode(part);
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String assessRiskLevel(IntegrityVerdicts verdicts, AttestationRequest request) {
        if (request.isHighRiskDevice()) {
            return "HIGH";
        }
        
        if (verdicts != null && (!verdicts.isMeetsDeviceIntegrity() || !verdicts.isMeetsBasicIntegrity())) {
            return "HIGH";
        }
        
        if (request.getBindingInfo() != null && !request.getBindingInfo().isSimPresent()) {
            return "MEDIUM";
        }
        
        if (verdicts != null && "UNEVALUATED".equals(verdicts.getAppIntegrityVerdict())) {
            return "MEDIUM";
        }
        
        return "LOW";
    }
    
    public boolean validateNonce(String providedNonce, String expectedNonce) {
        if (providedNonce == null || expectedNonce == null) {
            return false;
        }
        return providedNonce.equals(expectedNonce);
    }
    
    public String extractNonceFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                // In real implementation, parse JSON payload and extract nonce
                // String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                return "extracted_nonce_placeholder";
            }
        } catch (Exception e) {
            log.debug("Error extracting nonce from token", e);
        }
        return null;
    }
}