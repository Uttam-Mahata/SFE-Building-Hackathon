package com.sfe.paymentbackend.config

import com.sfe.sdk.backend.SFEBackendSDK
import com.sfe.sdk.backend.SFEEnvironment
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AppConfig {

    @Value("\${sfe.backend-sdk.api-key}")
    private lateinit var apiKey: String

    @Value("\${sfe.backend-sdk.environment}")
    private lateinit var environmentStr: String

    @Value("\${sfe.backend-sdk.mock-mode:true}") // Default to true if not specified
    private var mockMode: Boolean = true

    // Optional SDK Builder properties - uncomment and add to application.properties if needed
    // @Value("\${sfe.backend-sdk.database-url:#{null}}")
    // private var databaseUrl: String? = null

    // @Value("\${sfe.backend-sdk.fraud-detection-enabled:true}")
    // private var fraudDetectionEnabled: Boolean = true

    // @Value("\${sfe.backend-sdk.audit-logging-enabled:true}")
    // private var auditLoggingEnabled: Boolean = true

    // @Value("\${sfe.backend-sdk.latency-simulation-ms:0}")
    // private var latencySimulationMs: Int = 0


    @Bean
    fun sfeBackendSDK(): SFEBackendSDK {
        val environment = try {
            SFEEnvironment.valueOf(environmentStr.uppercase())
        } catch (e: IllegalArgumentException) {
            SFEEnvironment.SANDBOX // Default to SANDBOX if parse fails
        }

        val builder = SFEBackendSDK.Builder()
            .setApiKey(apiKey)
            .setEnvironment(environment)
            .enableMockMode(mockMode)
            // Example of how to use optional properties:
            // databaseUrl?.let { builder.setDatabaseUrl(it) }
            // builder.enableFraudDetection(fraudDetectionEnabled)
            // builder.enableAuditLogging(auditLoggingEnabled)
            // builder.setLatencySimulation(latencySimulationMs)

            // For now, keeping these as they were, can be externalized fully later
            .enableFraudDetection(true)
            .enableAuditLogging(true)

        return builder.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
