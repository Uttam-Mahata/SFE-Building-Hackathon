# SFE Sample Backend Service - Demo Payment Backend -  - IIEST-UCO Bank Hackathon

[![](https://jitpack.io/v/Uttam-Mahata/SFE-Building-Hackathon.svg)](https://jitpack.io/#Uttam-Mahata/SFE-Building-Hackathon)

> **🚀 Hackathon Demo**: Sample backend service showcasing SFE Backend SDK integration for secure payment processing.

## Overview

This is a demonstration Spring Boot backend service that showcases how to integrate the SFE Backend SDK for building secure, scalable payment processing backends with automatic RBI/NPCI compliance.

## ✨ Demo Features

- 🏦 **Payment Processing** - UPI, wallet, and QR code payment handling
- 🔐 **Secure APIs** - JWT authentication with encryption
- 🚨 **Fraud Detection** - Real-time transaction risk assessment
- 📊 **Compliance Reporting** - Automatic RBI/NPCI reporting
- 🔔 **Webhook Management** - Payment status notifications
- 📈 **Analytics Dashboard** - Transaction monitoring and metrics
- 🛡️ **Security Features** - Rate limiting, audit logging, encryption

## 🚀 Quick Start

### Prerequisites

- Java 17 or later
- Maven 3.6+ or Gradle 7+
- PostgreSQL 13+ (for demo database)
- Redis (for caching and sessions)

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/Uttam-Mahata/SFE-Building-Hackathon.git
cd SFE-Building-Hackathon/sample-backend-service
```

2. **Configure environment:**
```bash
# Copy environment template
cp .env.example .env

# Edit configuration
SFE_API_KEY=your-sfe-api-key
DATABASE_URL=postgresql://localhost:5432/sfe_demo
REDIS_URL=redis://localhost:6379
JWT_SECRET=your-jwt-secret-key
```

3. **Start dependencies:**
```bash
# Using Docker Compose
docker-compose up -d postgres redis

# Or install locally
# PostgreSQL: https://postgresql.org/download/
# Redis: https://redis.io/download
```

4. **Run the application:**
```bash
# Using Maven
./mvnw spring-boot:run

# Using Gradle
./gradlew bootRun

# Using Docker
docker build -t sfe-backend .
docker run -p 8080:8080 sfe-backend
```

5. **Access the application:**
```
http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
Health Check: http://localhost:8080/actuator/health
```

## 📋 API Documentation

### Authentication Endpoints

```kotlin
// AuthController.kt - User Authentication
@RestController
@RequestMapping("/api/auth")
class AuthController {
    
    @Autowired
    private lateinit var sfeBackendSDK: SFEBackendSDK
    
    @PostMapping("/register")
    fun registerUser(@RequestBody request: UserRegistrationRequest): ResponseEntity<AuthResponse> {
        return try {
            // SDK handles KYC verification and compliance
            val kycResult = sfeBackendSDK.kyc()
                .verifyAadhaar(request.aadhaarNumber)
                .verifyPAN(request.panNumber)
                .performVideoKYC(request.videoKYCData)
            
            if (!kycResult.isVerified) {
                return ResponseEntity.badRequest()
                    .body(AuthResponse.error("KYC verification failed: ${kycResult.reason}"))
            }
            
            val user = userService.createUser(request, kycResult)
            val tokens = jwtService.generateTokens(user)
            
            ResponseEntity.ok(AuthResponse.success(user, tokens))
        } catch (e: SFEValidationException) {
            ResponseEntity.badRequest().body(AuthResponse.error(e.message))
        }
    }
    
    @PostMapping("/login")
    fun loginUser(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userService.authenticateUser(request.email, request.password)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.error("Invalid credentials"))
        
        // Check if additional verification is needed
        val riskAssessment = sfeBackendSDK.fraud()
            .assessLoginRisk(user.id, request.deviceInfo)
        
        when (riskAssessment.riskLevel) {
            RiskLevel.HIGH -> {
                // Require OTP verification
                val otpToken = sfeBackendSDK.auth().generateOTP(user.phoneNumber)
                return ResponseEntity.ok(AuthResponse.requireOTP(otpToken))
            }
            RiskLevel.MEDIUM -> {
                // Require device verification
                return ResponseEntity.ok(AuthResponse.requireDeviceVerification())
            }
            RiskLevel.LOW -> {
                val tokens = jwtService.generateTokens(user)
                return ResponseEntity.ok(AuthResponse.success(user, tokens))
            }
        }
    }
}
```

### Payment Processing Endpoints

```kotlin
// PaymentController.kt - Payment Processing
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasRole('USER')")
class PaymentController {
    
    @PostMapping("/initiate")
    fun initiatePayment(
        @RequestBody request: PaymentRequest,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<PaymentResponse> {
        
        return try {
            // Comprehensive payment processing using SDK
            val result = sfeBackendSDK.payments()
                .validatePaymentRequest(request)           // RBI compliance checks
                .checkUserLimits(user.id, request.amount)  // Transaction limits
                .verifyRecipient(request.recipientVPA)     // Recipient validation
                .performFraudAnalysis(user.id, request)    // Real-time fraud check
                .processUPITransaction(request)            // NPCI processing
                .generateAuditLog(user.id, request)        // Compliance logging
            
            // Update user wallet
            walletService.debitAmount(user.id, request.amount)
            
            // Send notifications
            notificationService.sendPaymentInitiated(user.id, result.transactionId)
            
            ResponseEntity.ok(PaymentResponse.success(result))
            
        } catch (e: InsufficientBalanceException) {
            ResponseEntity.badRequest().body(PaymentResponse.error("Insufficient balance"))
        } catch (e: SFEFraudException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(PaymentResponse.blocked("Transaction blocked: ${e.reason}"))
        } catch (e: SFEValidationException) {
            ResponseEntity.badRequest().body(PaymentResponse.error(e.message))
        }
    }
    
    @GetMapping("/{transactionId}")
    fun getPaymentStatus(
        @PathVariable transactionId: String,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<TransactionStatusResponse> {
        
        val transaction = sfeBackendSDK.transactions()
            .getTransactionStatus(transactionId, user.id)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(TransactionStatusResponse.from(transaction))
    }
    
    @PostMapping("/qr/generate")
    fun generateQRCode(
        @RequestBody request: QRGenerationRequest,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<QRGenerationResponse> {
        
        val qrData = sfeBackendSDK.qr()
            .generatePaymentQR(user.id, request.amount, request.description)
            .setExpiryTime(request.expiryMinutes)
            .enableDynamicPricing(request.isDynamic)
        
        return ResponseEntity.ok(QRGenerationResponse.success(qrData))
    }
}
```

### Wallet Management

```kotlin
// WalletController.kt - Digital Wallet Operations
@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("hasRole('USER')")
class WalletController {
    
    @GetMapping("/balance")
    fun getWalletBalance(@AuthenticationPrincipal user: UserPrincipal): ResponseEntity<WalletResponse> {
        val wallet = walletService.getWallet(user.id)
        
        // SDK provides encrypted balance with audit trail
        val encryptedBalance = sfeBackendSDK.wallet()
            .getEncryptedBalance(user.id)
            .withAuditLog()
        
        return ResponseEntity.ok(WalletResponse.success(wallet, encryptedBalance))
    }
    
    @PostMapping("/add-money")
    fun addMoney(
        @RequestBody request: AddMoneyRequest,
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<AddMoneyResponse> {
        
        // Process bank transfer or card payment
        val bankTransfer = sfeBackendSDK.banking()
            .initiateBankTransfer(request.bankAccount, request.amount)
            .validateIFSC(request.ifscCode)
            .performAccountVerification()
        
        if (bankTransfer.isSuccessful) {
            walletService.creditAmount(user.id, request.amount)
            return ResponseEntity.ok(AddMoneyResponse.success(bankTransfer.transactionId))
        }
        
        return ResponseEntity.badRequest()
            .body(AddMoneyResponse.error("Bank transfer failed"))
    }
    
    @GetMapping("/transactions")
    fun getWalletTransactions(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<TransactionHistoryResponse> {
        
        val filter = TransactionFilter.Builder()
            .setUserId(user.id)
            .setPageNumber(page)
            .setPageSize(size)
            .setStartDate(parseDate(startDate))
            .setEndDate(parseDate(endDate))
            .build()
        
        val transactions = sfeBackendSDK.reporting()
            .getWalletTransactions(filter)
            .applyPrivacyFilters()              // Mask sensitive data
            .includeComplianceMetadata()        // RBI reporting data
        
        return ResponseEntity.ok(TransactionHistoryResponse.success(transactions))
    }
}
```

### Webhook Handlers

```kotlin
// WebhookController.kt - External Webhook Processing
@RestController
@RequestMapping("/api/webhooks")
class WebhookController {
    
    @PostMapping("/npci/payment-status")
    fun handleNPCIWebhook(
        @RequestBody payload: String,
        @RequestHeader("X-NPCI-Signature") signature: String
    ): ResponseEntity<String> {
        
        return try {
            // SDK automatically validates NPCI webhook signatures
            val webhook = sfeBackendSDK.webhooks()
                .validateNPCISignature(payload, signature)
                .parsePaymentStatusUpdate(payload)
            
            // Update transaction status
            transactionService.updateStatus(webhook.transactionId, webhook.status)
            
            // Handle different payment statuses
            when (webhook.status) {
                PaymentStatus.SUCCESS -> {
                    walletService.confirmTransaction(webhook.transactionId)
                    notificationService.sendPaymentSuccess(webhook.userId)
                    analyticsService.trackSuccessfulPayment(webhook)
                }
                PaymentStatus.FAILED -> {
                    walletService.revertTransaction(webhook.transactionId)
                    notificationService.sendPaymentFailure(webhook.userId, webhook.failureReason)
                }
                PaymentStatus.PENDING -> {
                    // Keep monitoring
                    monitoringService.trackPendingPayment(webhook.transactionId)
                }
            }
            
            ResponseEntity.ok("Webhook processed successfully")
            
        } catch (e: SFEWebhookValidationException) {
            logger.warn("Invalid NPCI webhook signature: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature")
        }
    }
    
    @PostMapping("/fraud/alert")
    fun handleFraudAlert(@RequestBody payload: String): ResponseEntity<String> {
        val alert = sfeBackendSDK.webhooks().parseFraudAlert(payload)
        
        // Immediate response to fraud
        when (alert.severity) {
            FraudSeverity.CRITICAL -> {
                userService.freezeAccount(alert.userId, "Critical fraud detected")
                securityService.notifySecurityTeam(alert)
                complianceService.generateIncidentReport(alert)
            }
            FraudSeverity.HIGH -> {
                userService.requireStepUpAuth(alert.userId)
                securityService.flagForReview(alert.userId)
            }
            FraudSeverity.MEDIUM -> {
                securityService.increasedMonitoring(alert.userId, Duration.ofHours(24))
            }
        }
        
        return ResponseEntity.ok("Fraud alert processed")
    }
}
```

### Admin & Compliance

```kotlin
// AdminController.kt - Administrative Functions
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController {
    
    @GetMapping("/compliance/daily-report")
    fun getDailyComplianceReport(
        @RequestParam date: String
    ): ResponseEntity<ComplianceReportResponse> {
        
        val reportDate = LocalDate.parse(date)
        val report = sfeBackendSDK.compliance()
            .generateDailyReport(reportDate)
            .includeTransactionSummary()
            .includeFraudStatistics()
            .includeUserMetrics()
            .formatForRBI()
        
        return ResponseEntity.ok(ComplianceReportResponse.success(report))
    }
    
    @GetMapping("/transactions/suspicious")
    fun getSuspiciousTransactions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<SuspiciousTransactionsResponse> {
        
        val suspiciousTransactions = sfeBackendSDK.fraud()
            .getSuspiciousTransactions(page, size)
            .includeRiskScores()
            .includeUserProfiles()
        
        return ResponseEntity.ok(SuspiciousTransactionsResponse.success(suspiciousTransactions))
    }
    
    @PostMapping("/users/{userId}/freeze")
    fun freezeUserAccount(
        @PathVariable userId: String,
        @RequestBody request: AccountFreezeRequest
    ): ResponseEntity<String> {
        
        sfeBackendSDK.admin()
            .freezeAccount(userId, request.reason)
            .notifyUser(request.notifyUser)
            .generateAuditLog()
        
        return ResponseEntity.ok("Account frozen successfully")
    }
}
```

## 🔧 Configuration

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: sfe-sample-backend
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/sfe_demo}
    username: ${DB_USERNAME:sfe_user}
    password: ${DB_PASSWORD:sfe_password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
  
  redis:
    url: ${REDIS_URL:redis://localhost:6379}
    timeout: 2000ms

# SFE Backend SDK Configuration
sfe:
  backend-sdk:
    api-key: ${SFE_API_KEY}
    environment: ${SFE_ENVIRONMENT:sandbox}
    base-url: ${SFE_BASE_URL:https://api.sfe-hackathon.com}
    
    fraud-detection:
      enabled: true
      ml-model-version: "v2.1"
      risk-threshold: 0.7
      
    compliance:
      rbi-reporting: true
      audit-retention-days: 2555 # 7 years
      
    security:
      encryption-level: AES_256
      jwt-expiry: 3600
      refresh-token-expiry: 86400

# API Documentation
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

# Monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

## 🔍 Database Schema

```sql
-- Database schema for demo backend
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    kyc_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    risk_profile VARCHAR(50) NOT NULL DEFAULT 'LOW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    transaction_id VARCHAR(255) UNIQUE NOT NULL, -- SFE SDK transaction ID
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(50) NOT NULL,
    recipient_vpa VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    transaction_id UUID REFERENCES transactions(id),
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

## 🧪 Testing

```kotlin
// PaymentControllerTest.kt - Integration Tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentControllerTest {
    
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate
    
    @MockBean
    private lateinit var sfeBackendSDK: SFEBackendSDK
    
    @Test
    fun `should process payment successfully`() {
        // Mock SDK response
        val mockResult = PaymentProcessingResult.success("txn_123", 100.0)
        `when`(sfeBackendSDK.payments().processUPITransaction(any()))
            .thenReturn(mockResult)
        
        val request = PaymentRequest(
            recipientVPA = "test@paytm",
            amount = 100.0,
            description = "Test payment"
        )
        
        val response = testRestTemplate.postForEntity(
            "/api/payments/initiate",
            request,
            PaymentResponse::class.java
        )
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.transactionId).isEqualTo("txn_123")
    }
    
    @Test
    fun `should handle fraud detection`() {
        // Mock fraud detection
        `when`(sfeBackendSDK.payments().performFraudAnalysis(any(), any()))
            .thenThrow(SFEFraudException("High risk transaction"))
        
        val request = PaymentRequest(
            recipientVPA = "suspicious@user",
            amount = 50000.0,
            description = "Large payment"
        )
        
        val response = testRestTemplate.postForEntity(
            "/api/payments/initiate",
            request,
            PaymentResponse::class.java
        )
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
```

## 📊 Monitoring & Metrics

```kotlin
// MetricsConfiguration.kt - Custom Metrics
@Configuration
class MetricsConfiguration {
    
    @Bean
    fun paymentMetrics(): MeterRegistry {
        return PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }
    
    @EventListener
    fun onPaymentProcessed(event: PaymentProcessedEvent) {
        Metrics.counter("sfe.payments.processed", 
                       "status", event.status,
                       "type", event.type)
               .increment()
        
        Metrics.timer("sfe.payments.processing_time")
               .record(event.processingTime, TimeUnit.MILLISECONDS)
    }
    
    @EventListener
    fun onFraudDetected(event: FraudDetectedEvent) {
        Metrics.counter("sfe.frau