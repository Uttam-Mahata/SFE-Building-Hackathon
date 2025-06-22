# SFE Core Services Architecture

## Overview
The SFE Core Services are a collection of Spring Boot microservices that provide centralized financial processing, compliance, and security services.

## Services Architecture

### 1. Payment Processing Service
```yaml
# payment-service/application.yml
server:
  port: 8081
spring:
  application:
    name: sfe-payment-service
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/sfe_payments}
```

```kotlin
// PaymentProcessingService.kt
@RestController
@RequestMapping("/api/v1/payments")
class PaymentProcessingController {
    
    @PostMapping("/process")
    fun processPayment(@RequestBody request: PaymentProcessingRequest): PaymentProcessingResponse {
        // Direct NPCI integration
        val npciResult = npciGateway.submitTransaction(request)
        
        // Store transaction
        val transaction = transactionRepository.save(
            Transaction.fromRequest(request, npciResult.transactionId)
        )
        
        return PaymentProcessingResponse.success(transaction)
    }
    
    @GetMapping("/{transactionId}/status")
    fun getStatus(@PathVariable transactionId: String): TransactionStatus {
        return npciGateway.getTransactionStatus(transactionId)
    }
}
```

### 2. Fraud Detection Service
```yaml
# fraud-service/application.yml
server:
  port: 8082
spring:
  application:
    name: sfe-fraud-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

fraud-detection:
  ml-model-path: /models/fraud-detection-v2.1.pkl
  risk-threshold: 0.7
  feature-store-url: ${FEATURE_STORE_URL}
```

```kotlin
// FraudDetectionController.kt
@RestController
@RequestMapping("/api/v1/fraud")
class FraudDetectionController {
    
    @Autowired
    private lateinit var mlFraudDetector: MLFraudDetector
    
    @PostMapping("/analyze")
    fun analyzeFraud(@RequestBody request: FraudAnalysisRequest): FraudAnalysisResponse {
        val features = featureExtractor.extractFeatures(request)
        val riskScore = mlFraudDetector.predictRisk(features)
        val riskLevel = determineRiskLevel(riskScore)
        
        // Store analysis result
        fraudAnalysisRepository.save(
            FraudAnalysis(
                transactionId = request.transactionId,
                riskScore = riskScore,
                riskLevel = riskLevel,
                features = features
            )
        )
        
        return FraudAnalysisResponse(
            riskLevel = riskLevel,
            riskScore = riskScore,
            blockedReasons = if (riskLevel == RiskLevel.HIGH) listOf("High risk score") else emptyList()
        )
    }
}

@Component
class MLFraudDetector {
    private val model = loadModel("/models/fraud-detection-v2.1.pkl")
    
    fun predictRisk(features: Map<String, Double>): Double {
        // Use TensorFlow/Scikit-learn model for prediction
        return model.predict(features.values.toDoubleArray())[0]
    }
}
```

### 3. Compliance Service
```kotlin
// ComplianceController.kt
@RestController
@RequestMapping("/api/v1/compliance")
class ComplianceController {
    
    @PostMapping("/validate")
    fun validateCompliance(@RequestBody request: ComplianceValidationRequest): ComplianceValidationResponse {
        val validations = listOf(
            validateTransactionLimits(request),
            validateKYCRequirements(request),
            validateRBIGuidelines(request),
            validateNPCIRules(request)
        )
        
        val isCompliant = validations.all { it.isValid }
        
        return ComplianceValidationResponse(
            isCompliant = isCompliant,
            violations = validations.filter { !it.isValid },
            recommendations = generateRecommendations(validations)
        )
    }
    
    @PostMapping("/reports/daily")
    fun generateDailyReport(@RequestBody request: DailyReportRequest): DailyReportResponse {
        val report = RBIReport.Builder()
            .setDate(request.date)
            .setTransactionSummary(getTransactionSummary(request.date))
            .setFraudStatistics(getFraudStats(request.date))
            .setComplianceMetrics(getComplianceMetrics(request.date))
            .build()
            
        // Submit to RBI portal
        rbiReportingService.submitReport(report)
        
        return DailyReportResponse.success(report.reportId)
    }
}
```

### 4. KYC Verification Service
```kotlin
// KYCController.kt
@RestController
@RequestMapping("/api/v1/kyc")
class KYCController {
    
    @PostMapping("/verify")
    fun verifyKYC(@RequestBody request: KYCVerificationRequest): KYCVerificationResponse {
        val aadhaarResult = uidaiService.verifyAadhaar(request.aadhaarNumber)
        val panResult = nsdlService.verifyPAN(request.panNumber)
        val bankResult = bankingService.verifyBankAccount(request.bankAccount)
        
        val overallStatus = if (aadhaarResult.isValid && panResult.isValid && bankResult.isValid) {
            KYCStatus.VERIFIED
        } else {
            KYCStatus.PENDING
        }
        
        return KYCVerificationResponse(
            status = overallStatus,
            aadhaarVerification = aadhaarResult,
            panVerification = panResult,
            bankVerification = bankResult,
            riskProfile = calculateRiskProfile(aadhaarResult, panResult, bankResult)
        )
    }
}
```

### 5. Configuration Service
```kotlin
// ConfigurationController.kt
@RestController
@RequestMapping("/api/v1/config")
class ConfigurationController {
    
    @GetMapping("/policies")
    fun getSecurityPolicies(): SecurityPoliciesResponse {
        return SecurityPoliciesResponse(
            fraudDetectionLevel = configRepository.getFraudDetectionLevel(),
            transactionLimits = configRepository.getTransactionLimits(),
            securityRules = configRepository.getSecurityRules(),
            complianceSettings = configRepository.getComplianceSettings()
        )
    }
    
    @PutMapping("/policies")
    fun updatePolicies(@RequestBody request: UpdatePoliciesRequest): UpdatePoliciesResponse {
        // Update centralized policies
        configRepository.updatePolicies(request)
        
        // Notify all connected applications
        policyUpdateNotifier.notifyPolicyChange(request)
        
        return UpdatePoliciesResponse.success()
    }
}
```

## Service Communication

### 1. Inter-Service Communication
```kotlin
// Using OpenFeign for service-to-service communication
@FeignClient(name = "fraud-service", url = "${sfe.services.fraud-service.url}")
interface FraudServiceClient {
    @PostMapping("/api/v1/fraud/analyze")
    fun analyzeFraud(@RequestBody request: FraudAnalysisRequest): FraudAnalysisResponse
}

// Circuit breaker for resilience
@Component
class ResilientFraudService {
    
    @Autowired
    private lateinit var fraudServiceClient: FraudServiceClient
    
    @CircuitBreaker(name = "fraud-service", fallbackMethod = "fallbackFraudAnalysis")
    fun analyzeFraud(request: FraudAnalysisRequest): FraudAnalysisResponse {
        return fraudServiceClient.analyzeFraud(request)
    }
    
    fun fallbackFraudAnalysis(request: FraudAnalysisRequest, exception: Exception): FraudAnalysisResponse {
        // Fallback to basic fraud detection
        return FraudAnalysisResponse(RiskLevel.MEDIUM, 0.5, listOf("Service unavailable"))
    }
}
```

### 2. Event-Driven Architecture
```kotlin
// Using RabbitMQ/Kafka for async communication
@Component
class PaymentEventPublisher {
    
    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate
    
    fun publishPaymentProcessed(payment: Payment) {
        val event = PaymentProcessedEvent(
            transactionId = payment.transactionId,
            userId = payment.userId,
            amount = payment.amount,
            timestamp = Instant.now()
        )
        
        rabbitTemplate.convertAndSend("payment.processed", event)
    }
}

@RabbitListener(queues = ["payment.processed"])
@Component
class ComplianceEventHandler {
    
    fun handlePaymentProcessed(event: PaymentProcessedEvent) {
        // Update compliance metrics
        complianceMetricsService.updateTransactionMetrics(event)
        
        // Check if daily reporting thresholds are met
        if (shouldTriggerReport(event)) {
            complianceReportingService.generateIntermediateReport()
        }
    }
}
```

## Deployment Configuration

### 1. Docker Compose for Local Development
```yaml
# docker-compose.yml
version: '3.8'
services:
  payment-service:
    build: ./payment-service
    ports:
      - "8081:8081"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/sfe_payments
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis

  fraud-service:
    build: ./fraud-service
    ports:
      - "8082:8082"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/sfe_fraud
      - ML_MODEL_PATH=/models/fraud-detection-v2.1.pkl
    volumes:
      - ./models:/models
    depends_on:
      - postgres

  compliance-service:
    build: ./compliance-service
    ports:
      - "8083:8083"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/sfe_compliance
    depends_on:
      - postgres

  postgres:
    image: postgres:13
    environment:
      - POSTGRES_DB=sfe
      - POSTGRES_USER=sfe_user
      - POSTGRES_PASSWORD=sfe_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

### 2. Kubernetes Deployment
```yaml
# k8s/payment-service.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment-service
  template:
    metadata:
      labels:
        app: payment-service
    spec:
      containers:
      - name: payment-service
        image: sfe/payment-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: sfe-secrets
              key: database-url
        - name: NPCI_API_KEY
          valueFrom:
            secretKeyRef:
              name: sfe-secrets
              key: npci-api-key
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: payment-service
spec:
  selector:
    app: payment-service
  ports:
  - port: 80
    targetPort: 8081
  type: ClusterIP
```

## Monitoring and Observability

### 1. Distributed Tracing
```kotlin
// Add to each service
@Bean
public Sender sender() {
    return OkHttpSender.create("http://jaeger:14268/api/traces");
}

// Automatic trace correlation
@RestController
class PaymentController {
    
    @NewSpan("process-payment")
    @PostMapping("/process")
    fun processPayment(@RequestBody request: PaymentRequest): PaymentResponse {
        // Automatic distributed tracing across services
        return paymentService.processPayment(request)
    }
}
```

### 2. Metrics and Monitoring
```kotlin
// Custom metrics
@Component
class PaymentMetrics {
    
    private val paymentCounter = Counter.builder("payments.processed")
        .description("Number of payments processed")
        .register(Metrics.globalRegistry)
    
    private val paymentTimer = Timer.builder("payments.processing.time")
        .description("Payment processing time")
        .register(Metrics.globalRegistry)
    
    fun recordPaymentProcessed() {
        paymentCounter.increment()
    }
    
    fun recordProcessingTime(duration: Duration) {
        paymentTimer.record(duration)
    }
}
```

This architecture provides:
- **Scalability**: Each service can scale independently
- **Resilience**: Circuit breakers and fallbacks
- **Maintainability**: Clear separation of concerns
- **Compliance**: Centralized compliance and reporting
- **Security**: Distributed security with central policy management 