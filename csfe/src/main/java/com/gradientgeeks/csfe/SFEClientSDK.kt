package com.gradientgeeks.csfe

import android.content.Context
import com.gradientgeeks.csfe.auth.AuthModule
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.config.SFEEnvironment
import com.gradientgeeks.csfe.config.LogLevel
import com.gradientgeeks.csfe.config.EncryptionLevel
import com.gradientgeeks.csfe.config.FraudDetectionLevel
import com.gradientgeeks.csfe.fraud.FraudDetectionModule
import com.gradientgeeks.csfe.payment.PaymentModule
import com.gradientgeeks.csfe.qr.QRCodeModule
import com.gradientgeeks.csfe.security.SecurityModule
import com.gradientgeeks.csfe.transaction.TransactionModule
import com.gradientgeeks.csfe.wallet.WalletModule
import com.gradientgeeks.csfe.utils.Logger

/**
 * Main entry point for the SFE Client SDK.
 * This class manages all the functionality offered by the SDK.
 */
class SFEClientSDK private constructor(
    private val context: Context,
    private val config: SFEConfig
) {
    // Module instances
    private val authModule: AuthModule by lazy { AuthModule(context, config) }
    private val paymentModule: PaymentModule by lazy { PaymentModule(context, config) }
    private val qrCodeModule: QRCodeModule by lazy { QRCodeModule(context, config) }
    private val securityModule: SecurityModule by lazy { SecurityModule(context, config) }
    private val fraudDetectionModule: FraudDetectionModule by lazy { FraudDetectionModule(context, config) }
    private val transactionModule: TransactionModule by lazy { TransactionModule(context, config) }
    private val walletModule: WalletModule by lazy { WalletModule(context, config) }
    
    init {
        Logger.init(config.logLevel)
        Logger.d(TAG, "SFE Client SDK initialized with environment: ${config.environment}")
        securityModule.performSecurityChecks()
    }
    
    /**
     * Access the authentication module for biometric auth, login, and registration.
     */
    fun auth(): AuthModule = authModule
    
    /**
     * Access the payment module for initiating UPI and other transactions.
     */
    fun payments(): PaymentModule = paymentModule
    
    /**
     * Access the QR code module for generating and scanning payment QR codes.
     */
    fun qr(): QRCodeModule = qrCodeModule
    
    /**
     * Access the security module for device binding and security checks.
     */
    fun security(): SecurityModule = securityModule
    
    /**
     * Access the fraud detection module for transaction risk analysis.
     */
    fun fraud(): FraudDetectionModule = fraudDetectionModule
    
    /**
     * Access the transaction module for fetching transaction history.
     */
    fun transactions(): TransactionModule = transactionModule
    
    /**
     * Access the wallet module for balance checks and wallet operations.
     */
    fun wallet(): WalletModule = walletModule
    
    /**
     * Builder class for creating SFEClientSDK instances.
     */
    class Builder(private val context: Context) {
        private var apiKey: String? = null
        private var environment: SFEEnvironment = SFEEnvironment.SANDBOX
        private var apiBaseUrl: String? = null
        private var enableBiometrics: Boolean = true
        private var enableDeviceBinding: Boolean = true
        private var debugMode: Boolean = false
        private var connectionTimeout: Int = 30
        private var readTimeout: Int = 30
        private var fraudDetectionLevel: FraudDetectionLevel = FraudDetectionLevel.MEDIUM
        private var encryptionLevel: EncryptionLevel = EncryptionLevel.AES_256
        private var logLevel: LogLevel = LogLevel.ERROR
        private var enableMockPayments: Boolean = false
        
        /**
         * Set the API key for authenticating with the SFE backend.
         */
        fun setApiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }
        
        /**
         * Set the environment to use (SANDBOX or PRODUCTION).
         */
        fun setEnvironment(environment: SFEEnvironment): Builder {
            this.environment = environment
            return this
        }
        
        /**
         * Set the base URL for API calls.
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
         * Enable or disable device binding for enhanced security.
         */
        fun enableDeviceBinding(enable: Boolean): Builder {
            this.enableDeviceBinding = enable
            return this
        }
        
        /**
         * Enable or disable debug mode.
         */
        fun setDebugMode(enable: Boolean): Builder {
            this.debugMode = enable
            return this
        }
        
        /**
         * Set connection timeout for API calls in seconds.
         */
        fun setConnectionTimeout(seconds: Int): Builder {
            this.connectionTimeout = seconds
            return this
        }
        
        /**
         * Set read timeout for API calls in seconds.
         */
        fun setReadTimeout(seconds: Int): Builder {
            this.readTimeout = seconds
            return this
        }
        
        /**
         * Set the fraud detection level.
         */
        fun setFraudDetectionLevel(level: FraudDetectionLevel): Builder {
            this.fraudDetectionLevel = level
            return this
        }
        
        /**
         * Set the encryption level for the SDK.
         */
        fun setEncryptionLevel(level: EncryptionLevel): Builder {
            this.encryptionLevel = level
            return this
        }
        
        /**
         * Set logging level for the SDK.
         */
        fun setLogLevel(level: LogLevel): Builder {
            this.logLevel = level
            return this
        }
        
        /**
         * Enable mock payments for testing purposes.
         */
        fun enableMockPayments(enable: Boolean): Builder {
            this.enableMockPayments = enable
            return this
        }
        
        /**
         * Build the SFEClientSDK instance with the configured options.
         */
        fun build(): SFEClientSDK {
            if (apiKey == null) {
                throw IllegalStateException("API key must be provided")
            }
            
            val baseUrl = apiBaseUrl ?: when (environment) {
                SFEEnvironment.SANDBOX -> "https://sandbox.sfe-hackathon.com/"
                SFEEnvironment.PRODUCTION -> "https://api.sfe-hackathon.com/"
            }
            
            val config = SFEConfig(
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
                logLevel = if (debugMode) LogLevel.DEBUG else logLevel,
                enableMockPayments = enableMockPayments
            )
            
            return SFEClientSDK(context.applicationContext, config)
        }
    }
    
    companion object {
        private const val TAG = "SFEClientSDK"
        private var instance: SFEClientSDK? = null
        
        /**
         * Initialize the SDK with the provided configuration.
         */
        @JvmStatic
        fun initialize(context: Context, config: SFEConfig) {
            synchronized(this) {
                if (instance == null) {
                    instance = SFEClientSDK(context.applicationContext, config)
                    Logger.i(TAG, "SFE Client SDK initialized successfully")
                } else {
                    Logger.w(TAG, "SFE Client SDK already initialized. Ignoring.")
                }
            }
        }
        
        /**
         * Get the initialized SDK instance.
         */
        @JvmStatic
        fun getInstance(): SFEClientSDK {
            return instance ?: throw IllegalStateException(
                "SFE Client SDK not initialized. Call initialize() first."
            )
        }
    }
}
