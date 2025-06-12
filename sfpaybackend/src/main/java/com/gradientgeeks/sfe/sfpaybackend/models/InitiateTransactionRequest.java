package com.gradientgeeks.sfe.sfpaybackend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiateTransactionRequest {
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code")
    private String currency;
    
    @NotBlank(message = "Recipient is required")
    private String recipient;
    
    private String description;
    
    @NotBlank(message = "SFE payload is required for security verification")
    private String sfePayload;
    
    // Payment method information
    private PaymentMethod paymentMethod;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethod {
        private String type; // "CARD", "BANK_TRANSFER", "WALLET"
        private String lastFourDigits;
        private String provider;
        private String token; // Tokenized payment method reference
    }
} 