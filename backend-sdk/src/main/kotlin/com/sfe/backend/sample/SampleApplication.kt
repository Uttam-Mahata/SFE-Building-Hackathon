package com.sfe.backend.sample

import com.sfe.backend.models.*
import com.sfe.backend.services.*
import com.sfe.backend.sdk.EnableSFEBackendSDK
import com.sfe.backend.sdk.SFEBackendSDK
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.*

/**
 * Sample Spring Boot application demonstrating SFE Backend SDK usage
 */
@SpringBootApplication
@EnableSFEBackendSDK
class SampleApplication

fun main(args: Array<String>) {
    runApplication<SampleApplication>(*args)
}

/**
 * Sample payment controller demonstrating SDK integration
 */
@RestController
@RequestMapping("/api/payments")
class PaymentController {
    
    @Autowired
    private lateinit var sfeBackendSDK: SFEBackendSDK
    
    @PostMapping("/initiate")
    suspend fun initiatePayment(@RequestBody request: PaymentRequest): ResponseEntity<PaymentResponse> {
        return try {
            // SDK handles all complexity: validation, fraud check, NPCI routing
            val result = sfeBackendSDK.payments()
                .validateRequest(request)           // RBI compliance validation
                .performFraudAnalysis(request)      // AI-based fraud detection
                .processTransaction(request)        // Route to NPCI/PSP
                .also { 
                    sfeBackendSDK.payments().generateAuditLog() // Automatic compliance logging
                }
            
            ResponseEntity.ok(result)
        } catch (e: SFEValidationException) {
            ResponseEntity.badRequest().body(PaymentResponse.error(e.message ?: "Validation error"))
        } catch (e: SFEFraudException) {
            ResponseEntity.status(403).body(PaymentResponse.blocked(e.reason))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(PaymentResponse.error("Processing failed: ${e.message}"))
        }
    }
    
    @GetMapping("/{transactionId}/status")
    fun getPaymentStatus(@PathVariable transactionId: String): ResponseEntity<TransactionStatus> {
        return try {
            val status = sfeBackendSDK.transactions().getStatus(transactionId)
            ResponseEntity.ok(status)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/history")
    fun getTransactionHistory(
        @RequestParam userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<TransactionHistoryResponse> {
        
        val filter = TransactionFilter.Builder()
            .setUserId(userId)
            .setPageNumber(page)
            .setPageSize(size)
            .setIncludeMetadata(true)
            .build()
            
        val history = sfeBackendSDK.reporting()
            .getTransactionHistory(filter)
            .applyPrivacyFilters()              // Mask sensitive data
            .generateComplianceMetadata()       // Add RBI reporting fields
            
        return ResponseEntity.ok(history)
    }
    
    @PostMapping("/sample")
    fun createSamplePayment(): ResponseEntity<PaymentResponse> {
        val sampleRequest = PaymentRequest(
            userId = "user123",
            amount = BigDecimal("100.00"),
            transactionType = TransactionType.UPI,
            recipientDetails = RecipientDetails(
                name = "John Doe",
                upiId = "john.doe@paytm"
            ),
            deviceInfo = DeviceInfo(
                deviceId = "device123",
                deviceType = "mobile",
                ipAddress = "192.168.1.1"
            )
        )
        
        return try {
            val result = sfeBackendSDK.payments()
                .validateRequest(sampleRequest)
                .performFraudAnalysis(sampleRequest)
                .let { 
                    // For demo purposes, return a success response
                    PaymentResponse.success(sampleRequest.transactionId, "DEMO${System.currentTimeMillis()}")
                }
            
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.status(500).body(PaymentResponse.error("Demo failed: ${e.message}"))
        }
    }
}

/**
 * Sample webhook controller
 */
@RestController
@RequestMapping("/api/webhooks")
class WebhookController {
    
    @Autowired
    private lateinit var sfeBackendSDK: SFEBackendSDK
    
    @PostMapping("/payment-status")
    fun handlePaymentStatusWebhook(
        @RequestBody payload: String,
        @RequestHeader("X-SFE-Signature") signature: String
    ): ResponseEntity<String> {
        
        return try {
            // SDK automatically validates webhook signature and parses payload
            val webhook = sfeBackendSDK.webhooks()
                .validateSignature(payload, signature)
                .parsePaymentStatusUpdate(payload)
            
            // Your business logic here
            println("Payment status updated: $webhook")
            
            ResponseEntity.ok("Webhook processed successfully")
        } catch (e: SFEWebhookValidationException) {
            ResponseEntity.status(401).body("Invalid webhook signature")
        } catch (e: Exception) {
            ResponseEntity.status(500).body("Webhook processing failed: ${e.message}")
        }
    }
}

/**
 * Sample user management controller
 */
@RestController
@RequestMapping("/api/users")
class UserController {
    
    @Autowired
    private lateinit var sfeBackendSDK: SFEBackendSDK
    
    @PostMapping("/register")
    fun registerUser(@RequestBody request: UserRegistrationRequest): ResponseEntity<UserResponse> {
        return try {
            // Perform KYC verification using SDK
            val kycResult = sfeBackendSDK.kyc()
                .verifyAadhaar(request.aadhaarNumber)
                .verifyPAN(request.panNumber)
                .verifyBankAccount(request.bankAccount)
                .let { 
                    request.videoKYCData?.let { videoData ->
                        sfeBackendSDK.kyc().performVideoKYC(videoData)
                    } ?: KYCResult(
                        isVerified = true,
                        verificationScore = 0.95,
                        riskProfile = RiskProfile(RiskLevel.LOW, 0.1)
                    )
                }
            
            if (!kycResult.isVerified) {
                return ResponseEntity.badRequest().body(
                    UserResponse("", "", "", "", KYCStatus.REJECTED, UserStatus.INACTIVE, java.time.Instant.now())
                )
            }
            
            // Create user with compliance data
            val user = User(
                id = UUID.randomUUID().toString(),
                email = request.email,
                phone = request.phone,
                fullName = request.fullName,
                kycStatus = KYCStatus.VERIFIED,
                riskProfile = kycResult.riskProfile,
                createdAt = java.time.Instant.now()
            )
            
            // Register with SFE for transaction limits and monitoring
            sfeBackendSDK.users().register(user)
            
            ResponseEntity.ok(UserResponse.from(user))
        } catch (e: Exception) {
            ResponseEntity.status(500).body(
                UserResponse("", "", "", "", KYCStatus.REJECTED, UserStatus.INACTIVE, java.time.Instant.now())
            )
        }
    }
}