package com.sfe.sdk.backend

import com.sfe.sdk.backend.ops.* // Import all operation interfaces
import com.sfe.sdk.backend.SFEEnvironment
import com.sfe.sdk.backend.EncryptionLevel

class SFEBackendSDK private constructor(
    private val apiKey: String,
    private val environment: SFEEnvironment,
    private val databaseUrl: String?,
    private val fraudDetectionEnabled: Boolean,
    private val auditLoggingEnabled: Boolean,
    private val encryptionLevel: EncryptionLevel?,
    private val mockModeEnabled: Boolean,
    private val latencySimulationMs: Int
) {

    // Placeholder implementations for operation interfaces
    // These would be initialized with actual implementations, possibly depending on the configuration above.
    private val paymentOperations: PaymentOperations = object : PaymentOperations { /* Default/mock implementation */ }
    private val transactionOperations: TransactionOperations = object : TransactionOperations { /* Default/mock implementation */ }
    private val kycOperations: KYCOperations = object : KYCOperations { /* Default/mock implementation */ }
    private val webhookOperations: WebhookOperations = object : WebhookOperations { /* Default/mock implementation */ }
    private val fraudOperations: FraudOperations = object : FraudOperations { /* Default/mock implementation */ }
    private val complianceOperations: ComplianceOperations = object : ComplianceOperations { /* Default/mock implementation */ }
    private val monitoringOperations: MonitoringOperations = object : MonitoringOperations { /* Default/mock implementation */ }
    private val userOperations: UserOperations = object : UserOperations { /* Default/mock implementation */ }
    private val authOperations: AuthOperations = object : AuthOperations { /* Default/mock implementation */ }
    private val adminOperations: AdminOperations = object : AdminOperations { /* Default/mock implementation */ }
    private val bankingOperations: BankingOperations = object : BankingOperations { /* Default/mock implementation */ }
    private val qrOperations: QROperations = object : QROperations { /* Default/mock implementation */ }
    private val walletOperations: WalletOperations = object : WalletOperations { /* Default/mock implementation */ }


    fun payments(): PaymentOperations = paymentOperations
    fun transactions(): TransactionOperations = transactionOperations
    fun kyc(): KYCOperations = kycOperations
    fun webhooks(): WebhookOperations = webhookOperations
    fun fraud(): FraudOperations = fraudOperations
    fun compliance(): ComplianceOperations = complianceOperations
    fun monitoring(): MonitoringOperations = monitoringOperations
    fun users(): UserOperations = userOperations
    fun auth(): AuthOperations = authOperations
    fun admin(): AdminOperations = adminOperations
    fun banking(): BankingOperations = bankingOperations
    fun qr(): QROperations = qrOperations
    fun wallet(): WalletOperations = walletOperations

    companion object {
        class Builder {
            private var apiKey: String? = null
            private var environment: SFEEnvironment = SFEEnvironment.SANDBOX // Default environment
            private var databaseUrl: String? = null
            private var fraudDetectionEnabled: Boolean = true // Default
            private var auditLoggingEnabled: Boolean = true // Default
            private var encryptionLevel: EncryptionLevel? = null
            private var mockModeEnabled: Boolean = false // Default
            private var latencySimulationMs: Int = 0 // Default

            fun setApiKey(apiKey: String): Builder = apply { this.apiKey = apiKey }
            fun setEnvironment(environment: SFEEnvironment): Builder = apply { this.environment = environment }
            fun setDatabaseUrl(databaseUrl: String): Builder = apply { this.databaseUrl = databaseUrl }
            fun enableFraudDetection(enabled: Boolean): Builder = apply { this.fraudDetectionEnabled = enabled }
            fun enableAuditLogging(enabled: Boolean): Builder = apply { this.auditLoggingEnabled = enabled }
            fun setEncryptionLevel(level: EncryptionLevel): Builder = apply { this.encryptionLevel = level }
            fun enableMockMode(enabled: Boolean): Builder = apply { this.mockModeEnabled = enabled }
            fun setLatencySimulation(latencyMs: Int): Builder = apply { this.latencySimulationMs = latencyMs }

            fun build(): SFEBackendSDK {
                val finalApiKey = apiKey ?: throw IllegalStateException("API Key must be set.")
                // Add other validations as necessary, e.g. for databaseUrl in production
                return SFEBackendSDK(
                    apiKey = finalApiKey,
                    environment = environment,
                    databaseUrl = databaseUrl,
                    fraudDetectionEnabled = fraudDetectionEnabled,
                    auditLoggingEnabled = auditLoggingEnabled,
                    encryptionLevel = encryptionLevel,
                    mockModeEnabled = mockModeEnabled,
                    latencySimulationMs = latencySimulationMs
                )
            }
        }
    }
}
