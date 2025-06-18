package com.gradientgeeks.csfe.config

/**
 * Configuration for the SFE Client SDK.
 */
data class SFEConfig(
    val apiKey: String,
    val environment: SFEEnvironment,
    val apiBaseUrl: String,
    val enableBiometrics: Boolean,
    val enableDeviceBinding: Boolean,
    val debugMode: Boolean,
    val connectionTimeout: Int,
    val readTimeout: Int,
    val fraudDetectionLevel: FraudDetectionLevel,
    val encryptionLevel: EncryptionLevel,
    val logLevel: LogLevel,
    val enableMockPayments: Boolean
) {
    /**
     * Builder for creating SFEConfig instances.
     */
    class Builder {
        private var apiKey: String? = null
        private var environment: SFEEnvironment = SFEEnvironment.SANDBOX
        private var apiBaseUrl: String? = null
        private var enableBiometrics: Boolean = true
        private var enableDeviceBinding: Boolean = true
        private var connectionTimeout: Int = 30
        private var readTimeout: Int = 30
        private var fraudDetectionLevel: FraudDetectionLevel = FraudDetectionLevel.MEDIUM
        private var encryptionLevel: EncryptionLevel = EncryptionLevel.AES_256
        private var logLevel: LogLevel = LogLevel.INFO
        private var debugMode: Boolean = false
        private var enableMockPayments: Boolean = false
        
        /**
         * Set the API key for backend authentication.
         */
        fun setApiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }
        
        /**
         * Set the environment (SANDBOX or PRODUCTION).
         */
        fun setEnvironment(environment: SFEEnvironment): Builder {
            this.environment = environment
            return this
        }
        
        /**
         * Set the API base URL.
         */
        fun setApiBaseUrl(url: String): Builder {
            this.apiBaseUrl = url
            return this
        }
        
        /**
         * Enable or disable biometric authentication.
         */
        fun enableBiometrics(enable: Boolean): Builder {
            this.enableBiometrics = enable
            return this
        }
        
        /**
         * Enable or disable device binding.
         */
        fun enableDeviceBinding(enable: Boolean): Builder {
            this.enableDeviceBinding = enable
            return this
        }
        
        /**
         * Set connection timeout in seconds.
         */
        fun setConnectionTimeout(seconds: Int): Builder {
            this.connectionTimeout = seconds
            return this
        }
        
        /**
         * Set read timeout in seconds.
         */
        fun setReadTimeout(seconds: Int): Builder {
            this.readTimeout = seconds
            return this
        }
        
        /**
         * Set fraud detection level.
         */
        fun setFraudDetectionLevel(level: FraudDetectionLevel): Builder {
            this.fraudDetectionLevel = level
            return this
        }
        
        /**
         * Set encryption level.
         */
        fun setEncryptionLevel(level: EncryptionLevel): Builder {
            this.encryptionLevel = level
            return this
        }
        
        /**
         * Set log level.
         */
        fun setLogLevel(level: LogLevel): Builder {
            this.logLevel = level
            return this
        }
        
        /**
         * Enable or disable debug mode.
         */
        fun setDebugMode(enable: Boolean): Builder {
            this.debugMode = enable
            this.logLevel = if (enable) LogLevel.DEBUG else this.logLevel
            return this
        }
        
        /**
         * Enable mock payments for testing.
         */
        fun enableMockPayments(enable: Boolean): Builder {
            this.enableMockPayments = enable
            return this
        }
        
        /**
         * Build the config object.
         */
        fun build(): SFEConfig {
            if (apiKey == null) {
                throw IllegalStateException("API key must be provided")
            }
            
            val baseUrl = apiBaseUrl ?: when (environment) {
                SFEEnvironment.SANDBOX -> "https://sandbox.sfe-hackathon.com/"
                SFEEnvironment.PRODUCTION -> "https://api.sfe-hackathon.com/"
            }
            
            return SFEConfig(
                apiKey = apiKey!!,
                environment = environment,
                apiBaseUrl = baseUrl,
                enableBiometrics = enableBiometrics,
                enableDeviceBinding = enableDeviceBinding,
                debugMode = debugMode,
                connectionTimeout = connectionTimeout,
                readTimeout = readTimeout,
                fraudDetectionLevel = fraudDetectionLevel,
                encryptionLevel = encryptionLevel,
                logLevel = logLevel,
                enableMockPayments = enableMockPayments
            )
        }
    }
}

/**
 * Available environments for the SDK.
 */
enum class SFEEnvironment {
    /**
     * Sandbox environment for testing.
     */
    SANDBOX,
    
    /**
     * Production environment for live transactions.
     */
    PRODUCTION
}

/**
 * Log levels for the SDK.
 */
enum class LogLevel {
    /**
     * No logs.
     */
    NONE,
    
    /**
     * Only errors.
     */
    ERROR,
    
    /**
     * Errors and warnings.
     */
    WARNING,
    
    /**
     * Errors, warnings, and info.
     */
    INFO,
    
    /**
     * All logs including debug information.
     */
    DEBUG
}

/**
 * Encryption levels supported by the SDK.
 */
enum class EncryptionLevel {
    /**
     * AES-128 encryption.
     */
    AES_128,
    
    /**
     * AES-256 encryption (recommended).
     */
    AES_256
}

/**
 * Fraud detection sensitivity levels.
 */
enum class FraudDetectionLevel {
    /**
     * Low sensitivity fraud detection.
     */
    LOW,
    
    /**
     * Medium sensitivity fraud detection (default).
     */
    MEDIUM,
    
    /**
     * High sensitivity fraud detection.
     */
    HIGH
}
