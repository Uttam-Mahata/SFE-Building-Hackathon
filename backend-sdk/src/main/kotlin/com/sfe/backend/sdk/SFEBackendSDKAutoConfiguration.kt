package com.sfe.backend.sdk

import com.sfe.backend.services.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Auto-configuration for SFE Backend SDK
 */
@Configuration
@ConditionalOnClass(SFEBackendSDK::class)
@EnableConfigurationProperties(SFESpringConfiguration::class)
@ComponentScan(basePackages = ["com.sfe.backend.services"])
class SFEBackendSDKAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun sfeBackendSDKConfiguration(springConfig: SFESpringConfiguration): SFEConfiguration {
        return SFEConfiguration(
            apiKey = springConfig.apiKey,
            environment = com.sfe.backend.models.SFEEnvironment.valueOf(springConfig.environment.uppercase()),
            databaseUrl = springConfig.databaseUrl,
            fraudDetectionEnabled = springConfig.fraudDetectionEnabled,
            auditLoggingEnabled = springConfig.auditLoggingEnabled,
            encryptionLevel = com.sfe.backend.models.EncryptionLevel.valueOf(springConfig.encryptionLevel),
            mockModeEnabled = springConfig.mockModeEnabled,
            latencySimulation = springConfig.latencySimulation,
            baseUrl = springConfig.baseUrl,
            rateLimitEnabled = springConfig.rateLimitEnabled,
            maxRequestsPerMinute = springConfig.maxRequestsPerMinute
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun paymentService(config: SFEConfiguration): PaymentService {
        return PaymentService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun fraudService(config: SFEConfiguration): FraudService {
        return FraudService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun kycService(config: SFEConfiguration): KYCService {
        return KYCService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun userService(config: SFEConfiguration): UserService {
        return UserService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun transactionService(config: SFEConfiguration): TransactionService {
        return TransactionService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun webhookService(config: SFEConfiguration): WebhookService {
        return WebhookService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun complianceService(config: SFEConfiguration): ComplianceService {
        return ComplianceService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun monitoringService(config: SFEConfiguration): MonitoringService {
        return MonitoringService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun authService(config: SFEConfiguration): AuthService {
        return AuthService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun reportingService(config: SFEConfiguration): ReportingService {
        return ReportingService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun walletService(config: SFEConfiguration): WalletService {
        return WalletService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun bankingService(config: SFEConfiguration): BankingService {
        return BankingService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun qrService(config: SFEConfiguration): QRService {
        return QRService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun adminService(config: SFEConfiguration): AdminService {
        return AdminService(config)
    }

    @Bean
    @ConditionalOnMissingBean
    fun sfeBackendSDK(
        config: SFEConfiguration,
        paymentService: PaymentService,
        fraudService: FraudService,
        kycService: KYCService,
        userService: UserService,
        transactionService: TransactionService,
        webhookService: WebhookService,
        complianceService: ComplianceService,
        monitoringService: MonitoringService,
        authService: AuthService,
        reportingService: ReportingService,
        walletService: WalletService,
        bankingService: BankingService,
        qrService: QRService,
        adminService: AdminService
    ): SFEBackendSDK {
        return SFEBackendSDK.Builder()
            .setApiKey(config.apiKey)
            .setEnvironment(config.environment)
            .setDatabaseUrl(config.databaseUrl)
            .enableFraudDetection(config.fraudDetectionEnabled)
            .enableAuditLogging(config.auditLoggingEnabled)
            .setEncryptionLevel(config.encryptionLevel)
            .enableMockMode(config.mockModeEnabled)
            .setLatencySimulation(config.latencySimulation)
            .setBaseUrl(config.baseUrl)
            .enableRateLimit(config.rateLimitEnabled)
            .setMaxRequestsPerMinute(config.maxRequestsPerMinute)
            .build()
    }
}