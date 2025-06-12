package com.gradientgeeks.sfe.sfpaybackend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    
    public enum Status {
        APPROVED,
        DECLINED,
        PENDING,
        REQUIRES_ADDITIONAL_AUTH,
        BLOCKED
    }
    
    private String transactionId;
    private Status status;
    private String message;
    private String authorizationCode;
    private BigDecimal amount;
    private String currency;
    private String recipient;
    private long timestamp;
    private SecurityInfo securityInfo;
    private String trackingReference;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SecurityInfo {
        private String riskLevel;
        private String attestationStatus;
        private boolean deviceTrusted;
        private String securityAssessmentId;
        private String policyDecision;
    }
    
    public static TransactionResponse approved(String transactionId, BigDecimal amount, String currency, String recipient, SecurityInfo securityInfo) {
        return TransactionResponse.builder()
            .transactionId(transactionId)
            .status(Status.APPROVED)
            .message("Transaction approved successfully")
            .authorizationCode(generateAuthCode())
            .amount(amount)
            .currency(currency)
            .recipient(recipient)
            .timestamp(Instant.now().toEpochMilli())
            .securityInfo(securityInfo)
            .trackingReference(generateTrackingRef())
            .build();
    }
    
    public static TransactionResponse declined(String transactionId, String reason) {
        return TransactionResponse.builder()
            .transactionId(transactionId)
            .status(Status.DECLINED)
            .message(reason)
            .timestamp(Instant.now().toEpochMilli())
            .build();
    }
    
    public static TransactionResponse blocked(String transactionId, String reason) {
        return TransactionResponse.builder()
            .transactionId(transactionId)
            .status(Status.BLOCKED)
            .message(reason)
            .timestamp(Instant.now().toEpochMilli())
            .build();
    }
    
    public static TransactionResponse requiresAdditionalAuth(String transactionId, String reason) {
        return TransactionResponse.builder()
            .transactionId(transactionId)
            .status(Status.REQUIRES_ADDITIONAL_AUTH)
            .message(reason)
            .timestamp(Instant.now().toEpochMilli())
            .build();
    }
    
    private static String generateAuthCode() {
        return "AUTH" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    private static String generateTrackingRef() {
        return "TRK" + System.currentTimeMillis() + (int)(Math.random() * 10000);
    }
} 