package com.sfe.backend.sdk

import com.sfe.backend.models.*
import com.sfe.backend.services.*
import org.springframework.stereotype.Component
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap

/**
 * Main SFE Backend SDK class - Entry point for all SDK operations
 */
@Component
class SFEBackendSDK private constructor(
    private val config: SFEConfiguration,
    private val paymentService: PaymentService,
    private val fraudService: FraudService,
    private val kycService: KYCService,
    private val userService: UserService,
    private val transactionService: TransactionService,
    private val webhookService: WebhookService,
    private val complianceService: ComplianceService,
    private val monitoringService: MonitoringService,
    private val authService: AuthService,
    private val reportingService: ReportingService,
    private val walletService: WalletService,
    private val bankingService: BankingService,
    private val qrService: QRService,
    private val adminService: AdminService
) {

    /**
     * Get payment service instance
     */
    fun payments(): PaymentService = paymentService

    /**
     * Get fraud detection service instance
     */
    fun fraud(): FraudService = fraudService

    /**
     * Get KYC service instance
     */
    fun kyc(): KYCService = kycService

    /**
     * Get user management service instance
     */
    fun users(): UserService = userService

    /**
     * Get transaction service instance
     */
    fun transactions(): TransactionService = transactionService

    /**
     * Get webhook service instance
     */
    fun webhooks(): WebhookService = webhookService

    /**
     * Get compliance service instance
     */
    fun compliance(): ComplianceService = complianceService

    /**
     * Get monitoring service instance
     */
    fun monitoring(): MonitoringService = monitoringService

    /**
     * Get authentication service instance
     */
    fun auth(): AuthService = authService

    /**
     * Get reporting service instance
     */
    fun reporting(): ReportingService = reportingService

    /**
     * Get wallet service instance
     */
    fun wallet(): WalletService = walletService

    /**
     * Get banking service instance
     */
    fun banking(): BankingService = bankingService

    /**
     * Get QR service instance
     */
    fun qr(): QRService = qrService

    /**
     * Get admin service instance
     */
    fun admin(): AdminService = adminService

    /**
     * Get current SDK configuration
     */
    fun getConfiguration(): SFEConfiguration = config

    /**
     * Builder class for creating SFE Backend SDK instances
     */
    class Builder {
        private var apiKey: String? = null
        private var environment: SFEEnvironment = SFEEnvironment.SANDBOX
        private var databaseUrl: String? = null
        private var fraudDetectionEnabled: Boolean = true
        private var auditLoggingEnabled: Boolean = true
        private var encryptionLevel: EncryptionLevel = EncryptionLevel.AES_256
        private var mockModeEnabled: Boolean = false
        private var latencySimulation: Int = 0
        private var baseUrl: String = "https://api.sfe.com"
        private var rateLimitEnabled: Boolean = true
        private var maxRequestsPerMinute: Int = 1000
        private var customProperties: MutableMap<String, Any> = mutableMapOf()

        fun setApiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }

        fun setEnvironment(environment: SFEEnvironment): Builder {
            this.environment = environment
            return this
        }

        fun setDatabaseUrl(databaseUrl: String?): Builder {
            this.databaseUrl = databaseUrl
            return this
        }

        fun enableFraudDetection(enabled: Boolean): Builder {
            this.fraudDetectionEnabled = enabled
            return this
        }

        fun enableAuditLogging(enabled: Boolean): Builder {
            this.auditLoggingEnabled = enabled
            return this
        }

        fun setEncryptionLevel(encryptionLevel: EncryptionLevel): Builder {
            this.encryptionLevel = encryptionLevel
            return this
        }

        fun enableMockMode(enabled: Boolean): Builder {
            this.mockModeEnabled = enabled
            return this
        }

        fun setLatencySimulation(latencyMs: Int): Builder {
            this.latencySimulation = latencyMs
            return this
        }

        fun setBaseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this
        }

        fun enableRateLimit(enabled: Boolean): Builder {
            this.rateLimitEnabled = enabled
            return this
        }

        fun setMaxRequestsPerMinute(maxRequests: Int): Builder {
            this.maxRequestsPerMinute = maxRequests
            return this
        }

        fun setProperty(key: String, value: Any): Builder {
            this.customProperties[key] = value
            return this
        }

        fun build(): SFEBackendSDK {
            // Validate required configuration
            if (apiKey.isNullOrBlank()) {
                throw SFEConfigurationException("API key is required")
            }

            if (environment == SFEEnvironment.PRODUCTION && databaseUrl.isNullOrBlank()) {
                throw SFEConfigurationException("Database URL is required for production environment")
            }

            // Create configuration
            val config = SFEConfiguration(
                apiKey = apiKey!!,
                environment = environment,
                databaseUrl = databaseUrl,
                fraudDetectionEnabled = fraudDetectionEnabled,
                auditLoggingEnabled = auditLoggingEnabled,
                encryptionLevel = encryptionLevel,
                mockModeEnabled = mockModeEnabled,
                latencySimulation = latencySimulation,
                baseUrl = baseUrl,
                rateLimitEnabled = rateLimitEnabled,
                maxRequestsPerMinute = maxRequestsPerMinute,
                customProperties = customProperties.toMap()
            )

            // Create service instances
            val paymentService = PaymentService(config)
            val fraudService = FraudService(config)
            val kycService = KYCService(config)
            val userService = UserService(config)
            val transactionService = TransactionService(config)
            val webhookService = WebhookService(config)
            val complianceService = ComplianceService(config)
            val monitoringService = MonitoringService(config)
            val authService = AuthService(config)
            val reportingService = ReportingService(config)
            val walletService = WalletService(config)
            val bankingService = BankingService(config)
            val qrService = QRService(config)
            val adminService = AdminService(config)

            return SFEBackendSDK(
                config = config,
                paymentService = paymentService,
                fraudService = fraudService,
                kycService = kycService,
                userService = userService,
                transactionService = transactionService,
                webhookService = webhookService,
                complianceService = complianceService,
                monitoringService = monitoringService,
                authService = authService,
                reportingService = reportingService,
                walletService = walletService,
                bankingService = bankingService,
                qrService = qrService,
                adminService = adminService
            )
        }
    }

    companion object {
        private val instances = ConcurrentHashMap<String, SFEBackendSDK>()

        /**
         * Get a singleton instance of the SDK for the given API key
         */
        fun getInstance(apiKey: String): SFEBackendSDK? {
            return instances[apiKey]
        }

        /**
         * Register a new SDK instance
         */
        internal fun registerInstance(apiKey: String, sdk: SFEBackendSDK) {
            instances[apiKey] = sdk
        }

        /**
         * Create a new builder instance
         */
        fun builder(): Builder = Builder()
    }
}

/**
 * Configuration class for SFE Backend SDK
 */
data class SFEConfiguration(
    val apiKey: String,
    val environment: SFEEnvironment,
    val databaseUrl: String? = null,
    val fraudDetectionEnabled: Boolean = true,
    val auditLoggingEnabled: Boolean = true,
    val encryptionLevel: EncryptionLevel = EncryptionLevel.AES_256,
    val mockModeEnabled: Boolean = false,
    val latencySimulation: Int = 0,
    val baseUrl: String = "https://api.sfe.com",
    val rateLimitEnabled: Boolean = true,
    val maxRequestsPerMinute: Int = 1000,
    val customProperties: Map<String, Any> = emptyMap()
)

/**
 * Spring configuration for SFE Backend SDK
 */
@Configuration
@ConfigurationProperties(prefix = "sfe.backend-sdk")
class SFESpringConfiguration {
    
    var apiKey: String = ""
    var environment: String = "sandbox"
    var databaseUrl: String? = null
    var fraudDetectionEnabled: Boolean = true
    var auditLoggingEnabled: Boolean = true
    var encryptionLevel: String = "AES_256"
    var mockModeEnabled: Boolean = false
    var latencySimulation: Int = 0
    var baseUrl: String = "https://api.sfe.com"
    var rateLimitEnabled: Boolean = true
    var maxRequestsPerMinute: Int = 1000

    @Bean
    fun sfeBackendSDK(): SFEBackendSDK {
        return SFEBackendSDK.Builder()
            .setApiKey(apiKey)
            .setEnvironment(SFEEnvironment.valueOf(environment.uppercase()))
            .setDatabaseUrl(databaseUrl)
            .enableFraudDetection(fraudDetectionEnabled)
            .enableAuditLogging(auditLoggingEnabled)
            .setEncryptionLevel(EncryptionLevel.valueOf(encryptionLevel))
            .enableMockMode(mockModeEnabled)
            .setLatencySimulation(latencySimulation)
            .setBaseUrl(baseUrl)
            .enableRateLimit(rateLimitEnabled)
            .setMaxRequestsPerMinute(maxRequestsPerMinute)
            .build()
    }
}