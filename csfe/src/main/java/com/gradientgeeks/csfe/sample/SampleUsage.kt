package com.gradientgeeks.csfe.sample

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.gradientgeeks.csfe.SFEClientSDK
import com.gradientgeeks.csfe.auth.BiometricResult
import com.gradientgeeks.csfe.auth.LoginData
import com.gradientgeeks.csfe.auth.LoginResult
import com.gradientgeeks.csfe.auth.RegistrationData
import com.gradientgeeks.csfe.auth.RegistrationResult
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.config.SFEEnvironment
import com.gradientgeeks.csfe.config.LogLevel
import com.gradientgeeks.csfe.fraud.FraudDetectionResult
import com.gradientgeeks.csfe.payment.PaymentRequest
import com.gradientgeeks.csfe.payment.PaymentResult
import com.gradientgeeks.csfe.qr.QRGenerationRequest
import com.gradientgeeks.csfe.qr.QRGenerationResult
import com.gradientgeeks.csfe.qr.QRScanResult
import com.gradientgeeks.csfe.transaction.TransactionFilter
import com.gradientgeeks.csfe.transaction.TransactionHistoryResult
import com.gradientgeeks.csfe.wallet.WalletBalanceResult
import java.util.Date

/**
 * Sample implementation demonstrating how to use the SFE Client SDK.
 */
class SampleUsage {
    
    companion object {
        
        /**
         * Initialize the SFE Client SDK in your Application class.
         */
        fun initializeSFESDK(context: Context, apiKey: String, isDebug: Boolean = false) {
            val config = SFEConfig.Builder()
                .setApiKey(apiKey)
                .setEnvironment(if (isDebug) SFEEnvironment.SANDBOX else SFEEnvironment.PRODUCTION)
                .setLogLevel(if (isDebug) LogLevel.DEBUG else LogLevel.ERROR)
                .setDebugMode(isDebug)
                .enableBiometrics(true)
                .enableDeviceBinding(true)
                .enableMockPayments(isDebug) // Enable mock payments for testing
                .setConnectionTimeout(30)
                .setReadTimeout(30)
                .build()
            
            SFEClientSDK.initialize(context, config)
        }
        
        /**
         * Alternative initialization using Builder pattern.
         */
        fun initializeSFESDKWithBuilder(context: Context, apiKey: String) {
            val sfeSDK = SFEClientSDK.Builder(context)
                .setApiKey(apiKey)
                .setEnvironment(SFEEnvironment.SANDBOX)
                .enableBiometrics(true)
                .setDebugMode(true)
                .enableMockPayments(true)
                .build()
            
            // Use the sfeSDK instance directly
            // or call SFEClientSDK.initialize() if you prefer singleton pattern
        }
        
        /**
         * User registration example.
         */
        fun registerUser(
            phoneNumber: String,
            email: String,
            name: String,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            val registrationData = RegistrationData(
                phoneNumber = phoneNumber,
                email = email,
                name = name
            )
            
            sfeSDK.auth().register(registrationData) { result ->
                when (result) {
                    is RegistrationResult.Success -> {
                        println("User registered successfully. User ID: ${result.userId}")
                        callback(true, result.userId)
                    }
                    is RegistrationResult.Error -> {
                        println("Registration failed: ${result.errorMessage}")
                        callback(false, result.errorMessage)
                    }
                }
            }
        }
        
        /**
         * User login example.
         */
        fun loginUser(
            phoneNumber: String,
            otp: String,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            val loginData = LoginData(
                phoneNumber = phoneNumber,
                otp = otp
            )
            
            sfeSDK.auth().login(loginData) { result ->
                when (result) {
                    is LoginResult.Success -> {
                        println("Login successful. Token: ${result.token}")
                        callback(true, result.token)
                    }
                    is LoginResult.Error -> {
                        println("Login failed: ${result.errorMessage}")
                        callback(false, result.errorMessage)
                    }
                }
            }
        }
        
        /**
         * Biometric authentication example.
         */
        fun authenticateWithBiometrics(
            activity: FragmentActivity,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            sfeSDK.auth().authenticateWithBiometrics(
                activity = activity,
                title = "Authenticate Payment",
                subtitle = "Use your fingerprint to confirm",
                description = "Place your finger on the sensor to authenticate the payment"
            ) { result ->
                when (result) {
                    is BiometricResult.Success -> {
                        println("Biometric authentication successful. Token: ${result.authToken}")
                        callback(true, result.authToken)
                    }
                    is BiometricResult.Error -> {
                        println("Biometric authentication failed: ${result.errorMessage}")
                        callback(false, result.errorMessage)
                    }
                    is BiometricResult.Cancelled -> {
                        println("Biometric authentication cancelled")
                        callback(false, "Authentication cancelled")
                    }
                }
            }
        }
        
        /**
         * Payment processing example.
         */
        fun processPayment(
            amount: Double,
            recipientVPA: String,
            description: String,
            authToken: String,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            val paymentRequest = PaymentRequest.Builder()
                .setAmount(amount)
                .setRecipientVPA(recipientVPA)
                .setDescription(description)
                .setTransactionNote("Payment via SFE SDK")
                .build()
            
            // First, perform fraud detection
            sfeSDK.fraud().analyzeTransaction(paymentRequest) { fraudResult ->
                when (fraudResult) {
                    is FraudDetectionResult.Allowed -> {
                        println("Fraud check passed. Processing payment...")
                        
                        // Proceed with payment
                        sfeSDK.payments().initiatePayment(paymentRequest, authToken) { result ->
                            when (result) {
                                is PaymentResult.Success -> {
                                    println("Payment successful! Transaction ID: ${result.transactionId}")
                                    callback(true, result.transactionId)
                                }
                                is PaymentResult.Error -> {
                                    println("Payment failed: ${result.errorMessage}")
                                    callback(false, result.errorMessage)
                                }
                                is PaymentResult.Pending -> {
                                    println("Payment pending. Transaction ID: ${result.transactionId}")
                                    callback(true, result.transactionId)
                                }
                            }
                        }
                    }
                    is FraudDetectionResult.Blocked -> {
                        println("Transaction blocked due to fraud risk: ${result.reason}")
                        callback(false, "Transaction blocked: ${result.reason}")
                    }
                    is FraudDetectionResult.RequiresVerification -> {
                        println("Additional verification required: ${result.verificationType}")
                        callback(false, "Additional verification required")
                    }
                    is FraudDetectionResult.Error -> {
                        println("Fraud detection error: ${result.errorMessage}")
                        callback(false, "Security check failed")
                    }
                }
            }
        }
        
        /**
         * QR code generation example.
         */
        fun generateQRCode(
            amount: Double,
            description: String,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            val qrRequest = QRGenerationRequest.Builder()
                .setAmount(amount)
                .setDescription(description)
                .setExpiryMinutes(15)
                .build()
            
            sfeSDK.qr().generateQRCode(qrRequest) { result ->
                when (result) {
                    is QRGenerationResult.Success -> {
                        println("QR code generated successfully")
                        // result.qrCodeBitmap can be displayed in ImageView
                        callback(true, result.qrCodeContent)
                    }
                    is QRGenerationResult.Error -> {
                        println("QR generation failed: ${result.errorMessage}")
                        callback(false, result.errorMessage)
                    }
                }
            }
        }
        
        /**
         * QR code scanning example.
         */
        fun scanQRCode(
            activity: Activity,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            sfeSDK.qr().scanQRCode(activity) { result ->
                when (result) {
                    is QRScanResult.Success -> {
                        val paymentData = result.paymentData
                        println("QR scanned successfully. VPA: ${paymentData.recipientVPA}")
                        callback(true, paymentData.recipientVPA)
                    }
                    is QRScanResult.InvalidQR -> {
                        println("Invalid QR code: ${result.scannedContent}")
                        callback(false, "Invalid payment QR code")
                    }
                    is QRScanResult.Error -> {
                        println("QR scan error: ${result.errorMessage}")
                        callback(false, result.errorMessage)
                    }
                }
            }
        }
        
        /**
         * Transaction history example.
         */
        fun getTransactionHistory(
            authToken: String,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            val filter = TransactionFilter.Builder()
                .setStartDate(Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000)) // Last 30 days
                .setEndDate(Date())
                .setPageSize(20)
                .build()
            
            sfeSDK.transactions().getTransactionHistory(filter, authToken) { result ->
                when (result) {
                    is TransactionHistoryResult.Success -> {
                        println("Transaction history loaded. Count: ${result.transactions.size}")
                        callback(true, "Loaded ${result.transactions.size} transactions")
                    }
                    is TransactionHistoryResult.Error -> {
                        println("Failed to load transaction history: ${result.errorMessage}")
                        callback(false, result.errorMessage)
                    }
                }
            }
        }
        
        /**
         * Wallet balance example.
         */
        fun getWalletBalance(
            authToken: String,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            sfeSDK.wallet().getBalance(authToken) { result ->
                when (result) {
                    is WalletBalanceResult.Success -> {
                        println("Wallet balance: ₹${result.balance}")
                        callback(true, result.balance.toString())
                    }
                    is WalletBalanceResult.Error -> {
                        println("Failed to get wallet balance: ${result.errorMessage}")
                        callback(false, result.errorMessage)
                    }
                }
            }
        }
        
        /**
         * Complete payment flow example.
         */
        fun completePaymentFlow(
            activity: FragmentActivity,
            amount: Double,
            recipientVPA: String,
            description: String,
            callback: (Boolean, String?) -> Unit
        ) {
            val sfeSDK = SFEClientSDK.getInstance()
            
            // Step 1: Authenticate with biometrics
            sfeSDK.auth().authenticateWithBiometrics(
                activity = activity,
                title = "Authenticate Payment",
                subtitle = "Confirm payment of ₹$amount",
                description = "Use your fingerprint to authorize this payment"
            ) { authResult ->
                when (authResult) {
                    is BiometricResult.Success -> {
                        // Step 2: Process payment with auth token
                        processPayment(amount, recipientVPA, description, authResult.authToken) { success, message ->
                            callback(success, message)
                        }
                    }
                    is BiometricResult.Error -> {
                        callback(false, "Authentication failed: ${authResult.errorMessage}")
                    }
                    is BiometricResult.Cancelled -> {
                        callback(false, "Authentication cancelled")
                    }
                }
            }
        }
    }
}