package com.gradientgeeks.sfe.sfpaybackend.controllers;

import com.gradientgeeks.sfe.sfpaybackend.models.InitiateTransactionRequest;
import com.gradientgeeks.sfe.sfpaybackend.models.TransactionResponse;
import com.gradientgeeks.sfe.sfpaybackend.services.PaymentProcessingService;
import com.gradientgeeks.sfe.sfpaybackend.services.JwtTokenService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment-app/api/v1/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {
    
    private final PaymentProcessingService paymentProcessingService;
    private final JwtTokenService jwtTokenService;
    
    public TransactionController(PaymentProcessingService paymentProcessingService, JwtTokenService jwtTokenService) {
        this.paymentProcessingService = paymentProcessingService;
        this.jwtTokenService = jwtTokenService;
    }
    
    /**
     * Initiate a payment transaction with SFE security verification
     */
    @PostMapping("/initiate")
    public ResponseEntity<TransactionResponse> initiateTransaction(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody InitiateTransactionRequest request) {
        
        log.info("Transaction initiation request received: {}", request.getTransactionId());
        
        try {
            // Validate authentication token
            String token = extractTokenFromHeader(authHeader);
            if (token == null || !isValidToken(token)) {
                log.warn("Invalid or missing authentication token for transaction: {}", request.getTransactionId());
                return ResponseEntity.status(401).body(
                    TransactionResponse.declined(request.getTransactionId(), "Invalid authentication token")
                );
            }
            
            // Get user context from token
            String username = jwtTokenService.getUsernameFromToken(token);
            log.info("Processing transaction {} for user: {}", request.getTransactionId(), username);
            
            // Process payment with SFE security verification
            TransactionResponse response = paymentProcessingService.processPayment(request);
            
            // Determine HTTP status based on transaction status
            int httpStatus = switch (response.getStatus()) {
                case APPROVED -> 200;
                case REQUIRES_ADDITIONAL_AUTH -> 202; // Accepted but requires more action
                case DECLINED -> 400; // Bad request
                case BLOCKED -> 403; // Forbidden
                case PENDING -> 202; // Accepted
            };
            
            log.info("Transaction {} processed with status: {}", 
                    request.getTransactionId(), response.getStatus());
            
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            log.error("Error processing transaction: {}", request.getTransactionId(), e);
            
            TransactionResponse errorResponse = TransactionResponse.declined(
                request.getTransactionId(), 
                "Transaction processing failed due to technical error"
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Get transaction status
     */
    @GetMapping("/{transactionId}/status")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String transactionId) {
        
        log.info("Transaction status request for: {}", transactionId);
        
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null || !isValidToken(token)) {
                return ResponseEntity.status(401).build();
            }
            
            // Mock transaction status lookup
            TransactionStatusResponse status = TransactionStatusResponse.builder()
                .transactionId(transactionId)
                .status("COMPLETED") // Mock status
                .timestamp(Instant.now().toEpochMilli())
                .message("Transaction completed successfully")
                .build();
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error retrieving transaction status: {}", transactionId, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get user's transaction history
     */
    @GetMapping("/history")
    public ResponseEntity<TransactionHistoryResponse> getTransactionHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null || !isValidToken(token)) {
                return ResponseEntity.status(401).build();
            }
            
            String username = jwtTokenService.getUsernameFromToken(token);
            log.info("Transaction history request for user: {} (limit: {}, offset: {})", username, limit, offset);
            
            // Mock transaction history
            List<TransactionSummary> mockTransactions = List.of(
                TransactionSummary.builder()
                    .transactionId("txn_001")
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .recipient("john@example.com")
                    .status("COMPLETED")
                    .timestamp(Instant.now().minusSeconds(3600).toEpochMilli())
                    .build(),
                TransactionSummary.builder()
                    .transactionId("txn_002")
                    .amount(new BigDecimal("250.50"))
                    .currency("USD")
                    .recipient("jane@example.com")
                    .status("COMPLETED")
                    .timestamp(Instant.now().minusSeconds(7200).toEpochMilli())
                    .build()
            );
            
            TransactionHistoryResponse response = TransactionHistoryResponse.builder()
                .transactions(mockTransactions)
                .total(mockTransactions.size())
                .limit(limit)
                .offset(offset)
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving transaction history", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Cancel a pending transaction
     */
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<TransactionResponse> cancelTransaction(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String transactionId) {
        
        log.info("Transaction cancellation request for: {}", transactionId);
        
        try {
            String token = extractTokenFromHeader(authHeader);
            if (token == null || !isValidToken(token)) {
                return ResponseEntity.status(401).body(
                    TransactionResponse.declined(transactionId, "Invalid authentication token")
                );
            }
            
            // Mock cancellation logic
            TransactionResponse response = TransactionResponse.builder()
                .transactionId(transactionId)
                .status(TransactionResponse.Status.DECLINED)
                .message("Transaction cancelled by user")
                .timestamp(Instant.now().toEpochMilli())
                .build();
            
            log.info("Transaction {} cancelled", transactionId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling transaction: {}", transactionId, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get payment service health and statistics
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "Payment Processing Service",
            "timestamp", Instant.now().toEpochMilli(),
            "supportedCurrencies", List.of("USD", "EUR", "GBP", "CAD"),
            "maxTransactionAmount", 10000,
            "sfeIntegrationEnabled", true
        );
        
        return ResponseEntity.ok(health);
    }
    
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private boolean isValidToken(String token) {
        try {
            jwtTokenService.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TransactionStatusResponse {
        private String transactionId;
        private String status;
        private long timestamp;
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TransactionHistoryResponse {
        private List<TransactionSummary> transactions;
        private int total;
        private int limit;
        private int offset;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TransactionSummary {
        private String transactionId;
        private BigDecimal amount;
        private String currency;
        private String recipient;
        private String status;
        private long timestamp;
    }
} 