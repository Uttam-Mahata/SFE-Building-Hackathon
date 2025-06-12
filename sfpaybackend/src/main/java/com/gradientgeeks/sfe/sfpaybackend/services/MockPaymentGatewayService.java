package com.gradientgeeks.sfe.sfpaybackend.services;

import com.gradientgeeks.sfe.sfpaybackend.models.InitiateTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Slf4j
@Service
public class MockPaymentGatewayService {
    
    private final Random random = new Random();
    
    /**
     * Process payment with standard validation
     */
    public boolean processPayment(BigDecimal amount, String currency, InitiateTransactionRequest.PaymentMethod paymentMethod) {
        log.info("Processing payment: {} {}", amount, currency);
        
        try {
            // Simulate network delay
            Thread.sleep(100 + random.nextInt(200));
            
            // Mock payment logic - 90% success rate
            boolean success = random.nextDouble() > 0.1;
            
            if (success) {
                log.info("Payment processed successfully");
            } else {
                log.warn("Payment declined by gateway");
            }
            
            return success;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted", e);
            return false;
        }
    }
    
    /**
     * Process payment with enhanced validation for higher-risk transactions
     */
    public boolean processPaymentWithEnhancedValidation(BigDecimal amount, String currency, InitiateTransactionRequest.PaymentMethod paymentMethod) {
        log.info("Processing payment with enhanced validation: {} {}", amount, currency);
        
        try {
            // Longer processing time for enhanced validation
            Thread.sleep(200 + random.nextInt(300));
            
            // Enhanced validation checks
            if (!performAmountValidation(amount)) {
                log.warn("Payment failed amount validation");
                return false;
            }
            
            if (!performCurrencyValidation(currency)) {
                log.warn("Payment failed currency validation");
                return false;
            }
            
            if (paymentMethod != null && !performPaymentMethodValidation(paymentMethod)) {
                log.warn("Payment failed payment method validation");
                return false;
            }
            
            // Mock enhanced processing - 85% success rate (slightly lower due to stricter validation)
            boolean success = random.nextDouble() > 0.15;
            
            if (success) {
                log.info("Payment processed successfully with enhanced validation");
            } else {
                log.warn("Payment declined after enhanced validation");
            }
            
            return success;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Enhanced payment processing interrupted", e);
            return false;
        }
    }
    
    private boolean performAmountValidation(BigDecimal amount) {
        // Mock validation - reject very large amounts (over 10000)
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            log.warn("Amount {} exceeds maximum allowed", amount);
            return false;
        }
        
        // Mock validation - reject very small amounts (under 0.01)
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            log.warn("Amount {} below minimum allowed", amount);
            return false;
        }
        
        return true;
    }
    
    private boolean performCurrencyValidation(String currency) {
        // Mock validation - only support specific currencies
        return currency != null && 
               (currency.equals("USD") || currency.equals("EUR") || currency.equals("GBP") || currency.equals("CAD"));
    }
    
    private boolean performPaymentMethodValidation(InitiateTransactionRequest.PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        // Mock validation - check payment method type
        String type = paymentMethod.getType();
        if (type == null || (!type.equals("CARD") && !type.equals("BANK_TRANSFER") && !type.equals("WALLET"))) {
            log.warn("Unsupported payment method type: {}", type);
            return false;
        }
        
        // Mock validation - check if payment method has required fields
        if (paymentMethod.getToken() == null || paymentMethod.getToken().isEmpty()) {
            log.warn("Payment method missing required token");
            return false;
        }
        
        return true;
    }
    
    /**
     * Verify payment method (for pre-transaction validation)
     */
    public boolean verifyPaymentMethod(InitiateTransactionRequest.PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        log.info("Verifying payment method: {}", paymentMethod.getType());
        
        try {
            // Simulate verification delay
            Thread.sleep(50 + random.nextInt(100));
            
            // Mock verification - 95% success rate
            boolean verified = random.nextDouble() > 0.05;
            
            if (verified) {
                log.info("Payment method verified successfully");
            } else {
                log.warn("Payment method verification failed");
            }
            
            return verified;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment method verification interrupted", e);
            return false;
        }
    }
    
    /**
     * Get payment gateway status (for health checks)
     */
    public boolean isGatewayHealthy() {
        try {
            // Simulate health check
            Thread.sleep(10 + random.nextInt(20));
            
            // Mock health status - 98% uptime
            return random.nextDouble() > 0.02;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
} 