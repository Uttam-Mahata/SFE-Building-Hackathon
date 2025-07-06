package com.sfe.backend.services

import com.sfe.backend.models.*
import com.sfe.backend.sdk.SFEConfiguration
import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Payment service for processing financial transactions
 */
@Service
class PaymentService(private val config: SFEConfiguration) {
    
    private val transactionStore = ConcurrentHashMap<String, TransactionRecord>()
    private val fraudService = FraudService(config)
    
    /**
     * Validate payment request
     */
    fun validateRequest(request: PaymentRequest): PaymentService {
        if (request.amount <= BigDecimal.ZERO) {
            throw SFEValidationException("Payment amount must be greater than zero")
        }
        
        if (request.amount > getMaxTransactionLimit()) {
            throw SFEValidationException("Payment amount exceeds maximum limit")
        }
        
        if (request.userId.isBlank()) {
            throw SFEValidationException("User ID is required")
        }
        
        if (request.recipientDetails.name.isBlank()) {
            throw SFEValidationException("Recipient name is required")
        }
        
        // Validate transaction type specific requirements
        when (request.transactionType) {
            TransactionType.UPI -> {
                if (request.recipientDetails.upiId.isNullOrBlank()) {
                    throw SFEValidationException("UPI ID is required for UPI transactions")
                }
            }
            TransactionType.NEFT, TransactionType.RTGS, TransactionType.IMPS -> {
                if (request.recipientDetails.accountNumber.isNullOrBlank() ||
                    request.recipientDetails.ifscCode.isNullOrBlank()) {
                    throw SFEValidationException("Account number and IFSC code are required for bank transfers")
                }
            }
            else -> {
                // Additional validation for other transaction types
            }
        }
        
        return this
    }
    
    /**
     * Perform fraud analysis on payment request
     */
    fun performFraudAnalysis(request: PaymentRequest): PaymentService {
        if (!config.fraudDetectionEnabled) {
            return this
        }
        
        try {
            val riskAssessment = fraudService.analyzeTransaction(request)
            
            if (riskAssessment.riskLevel == RiskLevel.HIGH || riskAssessment.riskLevel == RiskLevel.CRITICAL) {
                throw SFEFraudException(
                    "Transaction blocked due to high fraud risk",
                    riskAssessment.reason,
                    riskAssessment.riskLevel
                )
            }
            
            // Log medium risk transactions for monitoring
            if (riskAssessment.riskLevel == RiskLevel.MEDIUM) {
                logRiskTransaction(request, riskAssessment)
            }
            
        } catch (e: Exception) {
            if (e is SFEFraudException) throw e
            // In case of fraud service failure, log and continue in sandbox mode
            if (config.environment == SFEEnvironment.PRODUCTION) {
                throw SFEExternalServiceException("Fraud detection service unavailable", e)
            }
        }
        
        return this
    }
    
    /**
     * Process the payment transaction
     */
    suspend fun processTransaction(request: PaymentRequest): PaymentResponse {
        return try {
            // Simulate processing time
            if (config.latencySimulation > 0) {
                delay(config.latencySimulation.toLong())
            }
            
            // Mock mode returns predefined responses
            if (config.mockModeEnabled) {
                return generateMockResponse(request)
            }
            
            // Create transaction record
            val transaction = TransactionRecord(
                id = request.transactionId,
                userId = request.userId,
                amount = request.amount,
                currency = request.currency,
                transactionType = request.transactionType,
                status = PaymentStatus.PROCESSING,
                recipientDetails = request.recipientDetails,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            transactionStore[request.transactionId] = transaction
            
            // Process based on transaction type
            val result = when (request.transactionType) {
                TransactionType.UPI -> processUPITransaction(request)
                TransactionType.NEFT -> processNEFTTransaction(request)
                TransactionType.RTGS -> processRTGSTransaction(request)
                TransactionType.IMPS -> processIMPSTransaction(request)
                TransactionType.CARD_PAYMENT -> processCardPayment(request)
                TransactionType.WALLET_TRANSFER -> processWalletTransfer(request)
                TransactionType.QR_CODE_PAYMENT -> processQRPayment(request)
                TransactionType.BANK_TRANSFER -> processBankTransfer(request)
            }
            
            // Update transaction status
            transaction.status = result.status
            transaction.updatedAt = Instant.now()
            transaction.referenceId = result.referenceId
            
            // Log transaction for audit
            if (config.auditLoggingEnabled) {
                logTransaction(transaction)
            }
            
            result
            
        } catch (e: Exception) {
            // Update transaction status to failed
            transactionStore[request.transactionId]?.let { transaction ->
                transaction.status = PaymentStatus.FAILED
                transaction.updatedAt = Instant.now()
                transaction.failureReason = e.message
            }
            
            when (e) {
                is SFEException -> throw e
                else -> throw SFEPaymentException("Payment processing failed: ${e.message}", e)
            }
        }
    }
    
    /**
     * Generate audit log for transaction
     */
    fun generateAuditLog(): PaymentService {
        // Audit logging implementation would be here
        return this
    }
    
    // Private helper methods
    
    private fun processUPITransaction(request: PaymentRequest): PaymentResponse {
        // Simulate UPI processing
        val success = (Math.random() * 100) > 5 // 95% success rate
        
        return if (success) {
            PaymentResponse.success(
                transactionId = request.transactionId,
                referenceId = "UPI${System.currentTimeMillis()}"
            )
        } else {
            PaymentResponse.error("UPI transaction failed")
        }
    }
    
    private fun processNEFTTransaction(request: PaymentRequest): PaymentResponse {
        // NEFT processing logic
        return PaymentResponse.success(
            transactionId = request.transactionId,
            referenceId = "NEFT${System.currentTimeMillis()}"
        )
    }
    
    private fun processRTGSTransaction(request: PaymentRequest): PaymentResponse {
        // RTGS processing logic
        if (request.amount < BigDecimal("200000")) {
            return PaymentResponse.error("RTGS minimum amount is ₹2,00,000")
        }
        
        return PaymentResponse.success(
            transactionId = request.transactionId,
            referenceId = "RTGS${System.currentTimeMillis()}"
        )
    }
    
    private fun processIMPSTransaction(request: PaymentRequest): PaymentResponse {
        // IMPS processing logic
        return PaymentResponse.success(
            transactionId = request.transactionId,
            referenceId = "IMPS${System.currentTimeMillis()}"
        )
    }
    
    private fun processCardPayment(request: PaymentRequest): PaymentResponse {
        // Card payment processing logic
        return PaymentResponse.success(
            transactionId = request.transactionId,
            referenceId = "CARD${System.currentTimeMillis()}"
        )
    }
    
    private fun processWalletTransfer(request: PaymentRequest): PaymentResponse {
        // Wallet transfer processing logic
        return PaymentResponse.success(
            transactionId = request.transactionId,
            referenceId = "WALLET${System.currentTimeMillis()}"
        )
    }
    
    private fun processQRPayment(request: PaymentRequest): PaymentResponse {
        // QR code payment processing logic
        return PaymentResponse.success(
            transactionId = request.transactionId,
            referenceId = "QR${System.currentTimeMillis()}"
        )
    }
    
    private fun processBankTransfer(request: PaymentRequest): PaymentResponse {
        // Bank transfer processing logic
        return PaymentResponse.success(
            transactionId = request.transactionId,
            referenceId = "BANK${System.currentTimeMillis()}"
        )
    }
    
    private fun generateMockResponse(request: PaymentRequest): PaymentResponse {
        // Generate predictable mock responses for testing
        return when {
            request.amount >= BigDecimal("100000") -> 
                PaymentResponse.error("Mock: Large amount transaction blocked")
            request.userId.contains("fraud") -> 
                PaymentResponse.blocked("Mock: Fraud detected")
            else -> 
                PaymentResponse.success(request.transactionId, "MOCK${System.currentTimeMillis()}")
        }
    }
    
    private fun getMaxTransactionLimit(): BigDecimal {
        return when (config.environment) {
            SFEEnvironment.SANDBOX -> BigDecimal("1000000") // 10 Lakh
            SFEEnvironment.PRODUCTION -> BigDecimal("10000000") // 1 Crore
            SFEEnvironment.TEST -> BigDecimal("100000") // 1 Lakh
        }
    }
    
    private fun logRiskTransaction(request: PaymentRequest, riskAssessment: RiskAssessment) {
        // Log medium risk transactions for monitoring
        println("RISK ALERT: Transaction ${request.transactionId} flagged with risk level ${riskAssessment.riskLevel}")
    }
    
    private fun logTransaction(transaction: TransactionRecord) {
        // Log transaction for audit purposes
        println("AUDIT: Transaction ${transaction.id} processed with status ${transaction.status}")
    }
}

/**
 * Internal transaction record for tracking
 */
data class TransactionRecord(
    val id: String,
    val userId: String,
    val amount: BigDecimal,
    val currency: String,
    val transactionType: TransactionType,
    var status: PaymentStatus,
    val recipientDetails: RecipientDetails,
    val createdAt: Instant,
    var updatedAt: Instant,
    var referenceId: String? = null,
    var failureReason: String? = null
)