package com.gradientgeeks.sfe.sfpaybackend.services;

import com.gradientgeeks.sfe.backendsdk.models.AttestationRequest;
import com.gradientgeeks.sfe.backendsdk.models.AttestationResponse;
import com.gradientgeeks.sfe.backendsdk.models.TelemetryEventRequest;
import com.gradientgeeks.sfe.backendsdk.services.AttestationVerificationService;
import com.gradientgeeks.sfe.backendsdk.services.PolicyEnforcementService;
import com.gradientgeeks.sfe.backendsdk.services.TelemetryService;
import com.gradientgeeks.sfe.backendsdk.utils.SecurityUtils;

import com.gradientgeeks.sfe.sfpaybackend.models.InitiateTransactionRequest;
import com.gradientgeeks.sfe.sfpaybackend.models.TransactionResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PaymentProcessingService {
    
    private final AttestationVerificationService attestationService;
    private final PolicyEnforcementService policyService;
    private final TelemetryService telemetryService;
    private final MockPaymentGatewayService paymentGatewayService;
    private final Gson gson;
    
    public PaymentProcessingService(
            AttestationVerificationService attestationService,
            PolicyEnforcementService policyService,
            TelemetryService telemetryService,
            MockPaymentGatewayService paymentGatewayService) {
        this.attestationService = attestationService;
        this.policyService = policyService;
        this.telemetryService = telemetryService;
        this.paymentGatewayService = paymentGatewayService;
        this.gson = new Gson();
    }
    
    /**
     * Process a payment transaction with SFE security verification
     */
    public TransactionResponse processPayment(InitiateTransactionRequest request) {
        String transactionId = request.getTransactionId();
        log.info("Processing payment transaction: {}", transactionId);
        
        try {
            // Step 1: Parse and validate SFE payload
            AttestationRequest attestationRequest = parseSfePayload(request.getSfePayload());
            if (attestationRequest == null) {
                log.warn("Invalid SFE payload for transaction: {}", transactionId);
                return TransactionResponse.blocked(transactionId, "Invalid security payload");
            }
            
            // Step 2: Verify attestation with Google Play Integrity
            AttestationResponse attestationResponse = attestationService.verifyAttestation(attestationRequest);
            
            // Step 3: Assess risk based on security policies
            String riskLevel = policyService.assessRisk(attestationRequest, attestationResponse);
            
            // Step 4: Make transaction decision based on risk assessment
            PolicyEnforcementService.PolicyAction action = policyService.determineAction(riskLevel);
            
            // Step 5: Record telemetry for compliance
            recordTransactionTelemetry(attestationRequest, attestationResponse, action, transactionId);
            
            // Step 6: Process transaction based on security assessment
            TransactionResponse response = executeTransactionDecision(request, action, riskLevel, attestationResponse);
            
            log.info("Transaction {} processed with status: {} (Risk: {})", 
                    transactionId, response.getStatus(), riskLevel);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing payment transaction: {}", transactionId, e);
            recordErrorTelemetry(transactionId, e);
            return TransactionResponse.declined(transactionId, "Payment processing failed due to technical error");
        }
    }
    
    /**
     * Parse SFE payload from JSON to AttestationRequest
     */
    private AttestationRequest parseSfePayload(String sfePayload) {
        try {
            if (sfePayload == null || sfePayload.trim().isEmpty()) {
                return null;
            }
            
            return gson.fromJson(sfePayload, AttestationRequest.class);
            
        } catch (JsonSyntaxException e) {
            log.error("Error parsing SFE payload", e);
            return null;
        }
    }
    
    /**
     * Execute transaction decision based on security assessment
     */
    private TransactionResponse executeTransactionDecision(
            InitiateTransactionRequest request, 
            PolicyEnforcementService.PolicyAction action,
            String riskLevel,
            AttestationResponse attestationResponse) {
        
        String transactionId = request.getTransactionId();
        
        // Create security info for response
        TransactionResponse.SecurityInfo securityInfo = TransactionResponse.SecurityInfo.builder()
            .riskLevel(riskLevel)
            .attestationStatus(attestationResponse.getStatus().toString())
            .deviceTrusted(attestationResponse.meetsIntegrityRequirements())
            .securityAssessmentId(UUID.randomUUID().toString())
            .policyDecision(action.toString())
            .build();
        
        switch (action) {
            case BLOCK:
                log.warn("Transaction {} blocked due to high security risk", transactionId);
                return TransactionResponse.blocked(transactionId, "Transaction blocked due to security concerns");
                
            case REQUIRE_ADDITIONAL_AUTH:
                log.info("Transaction {} requires additional authentication", transactionId);
                return TransactionResponse.requiresAdditionalAuth(transactionId, 
                    "Additional verification required due to elevated risk level");
                
            case MONITOR:
                log.info("Transaction {} approved with monitoring", transactionId);
                // Process with additional monitoring
                return processWithMonitoring(request, securityInfo);
                
            case ALLOW:
            default:
                log.info("Transaction {} approved", transactionId);
                return processNormalTransaction(request, securityInfo);
        }
    }
    
    /**
     * Process transaction with normal flow
     */
    private TransactionResponse processNormalTransaction(
            InitiateTransactionRequest request, 
            TransactionResponse.SecurityInfo securityInfo) {
        
        // Simulate payment gateway processing
        boolean paymentSuccess = paymentGatewayService.processPayment(
            request.getAmount(), 
            request.getCurrency(), 
            request.getPaymentMethod()
        );
        
        if (paymentSuccess) {
            return TransactionResponse.approved(
                request.getTransactionId(),
                request.getAmount(),
                request.getCurrency(),
                request.getRecipient(),
                securityInfo
            );
        } else {
            return TransactionResponse.declined(
                request.getTransactionId(),
                "Payment gateway declined the transaction"
            );
        }
    }
    
    /**
     * Process transaction with enhanced monitoring
     */
    private TransactionResponse processWithMonitoring(
            InitiateTransactionRequest request, 
            TransactionResponse.SecurityInfo securityInfo) {
        
        // Add additional monitoring and logging
        log.info("Processing transaction {} with enhanced monitoring", request.getTransactionId());
        
        // Record additional telemetry for monitoring
        recordMonitoringTelemetry(request);
        
        // Process the payment with stricter validation
        boolean paymentSuccess = paymentGatewayService.processPaymentWithEnhancedValidation(
            request.getAmount(), 
            request.getCurrency(), 
            request.getPaymentMethod()
        );
        
        if (paymentSuccess) {
            return TransactionResponse.approved(
                request.getTransactionId(),
                request.getAmount(),
                request.getCurrency(),
                request.getRecipient(),
                securityInfo
            );
        } else {
            return TransactionResponse.declined(
                request.getTransactionId(),
                "Payment declined after enhanced validation"
            );
        }
    }
    
    /**
     * Record transaction telemetry for compliance
     */
    private void recordTransactionTelemetry(
            AttestationRequest attestationRequest,
            AttestationResponse attestationResponse,
            PolicyEnforcementService.PolicyAction action,
            String transactionId) {
        
        try {
            String deviceFingerprint = SecurityUtils.generateDeviceFingerprint(
                attestationRequest.getDeviceInfo().getDeviceModel(),
                attestationRequest.getDeviceInfo().getOsVersion(),
                attestationRequest.getBindingInfo().getNetworkOperator(),
                attestationRequest.getBindingInfo().getDeviceBindingToken()
            );
            
            Map<String, Object> eventData = Map.of(
                "transactionId", transactionId,
                "actionTaken", action.toString(),
                "attestationStatus", attestationResponse.getStatus().toString(),
                "deviceTrusted", attestationResponse.meetsIntegrityRequirements()
            );
            
            TelemetryEventRequest telemetryEvent = TelemetryEventRequest.securityCheck(
                deviceFingerprint,
                attestationResponse.getRiskLevel(),
                eventData
            );
            
            telemetryService.recordTelemetry(telemetryEvent);
            
        } catch (Exception e) {
            log.error("Error recording transaction telemetry", e);
        }
    }
    
    /**
     * Record monitoring telemetry for medium-risk transactions
     */
    private void recordMonitoringTelemetry(InitiateTransactionRequest request) {
        try {
            Map<String, Object> eventData = Map.of(
                "transactionId", request.getTransactionId(),
                "amount", request.getAmount().toString(),
                "currency", request.getCurrency(),
                "monitoringLevel", "ENHANCED"
            );
            
            TelemetryEventRequest telemetryEvent = TelemetryEventRequest.builder()
                .eventType(TelemetryEventRequest.EventType.RISK_ASSESSMENT)
                .timestamp(System.currentTimeMillis())
                .riskLevel("MEDIUM")
                .eventData(eventData)
                .build();
            
            telemetryService.recordTelemetry(telemetryEvent);
            
        } catch (Exception e) {
            log.error("Error recording monitoring telemetry", e);
        }
    }
    
    /**
     * Record error telemetry
     */
    private void recordErrorTelemetry(String transactionId, Exception error) {
        try {
            Map<String, Object> eventData = Map.of(
                "transactionId", transactionId,
                "errorType", error.getClass().getSimpleName(),
                "errorMessage", error.getMessage() != null ? error.getMessage() : "Unknown error"
            );
            
            TelemetryEventRequest telemetryEvent = TelemetryEventRequest.builder()
                .eventType(TelemetryEventRequest.EventType.TRANSACTION_BLOCKED)
                .timestamp(System.currentTimeMillis())
                .riskLevel("HIGH")
                .eventData(eventData)
                .build();
            
            telemetryService.recordTelemetry(telemetryEvent);
            
        } catch (Exception e) {
            log.error("Error recording error telemetry", e);
        }
    }
} 