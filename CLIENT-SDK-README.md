# SFE Client SDK - Secure Financial Environment -  - IIEST-UCO Bank Hackathon

[![](https://jitpack.io/v/Uttam-Mahata/SFE-Building-Hackathon.svg)](https://jitpack.io/#Uttam-Mahata/SFE-Building-Hackathon)

> **🚀 Hackathon Project**: Secure Financial Environment controlled by RBI/NPCI for mobile financial applications.

## Overview

The SFE Client SDK provides a headless, secure financial transaction layer for Android applications, ensuring RBI/NPCI compliance and enterprise-grade security for payment apps.

## ✨ Key Features

- 🔐 **End-to-end Encryption** - AES-256 encryption for all transactions
- 🛡️ **Biometric Authentication** - Fingerprint, Face ID, PIN support
- 🏦 **RBI/NPCI Compliant** - Automatic regulatory compliance
- 📱 **Cross-Platform** - Kotlin/Java support for Android
- 🛡️ **Security Features** - Device binding, fraud alerts, encryption, SIM Check, Runtime Security Check

## 🚀 Quick Start

### Installation

Add JitPack repository to your root `build.gradle`:

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.Uttam-Mahata.SFE-Building-Hackathon:client-sdk:v1.0.0'
}
```

### Basic Usage

```kotlin
// Initialize SDK
val sfeSDK = SFEClientSDK.Builder(this)
    .setApiKey("your-api-key")
    .setEnvironment(SFEEnvironment.SANDBOX)
    .enableBiometrics(true)
    .setDebugMode(BuildConfig.DEBUG)
    .build()

// Make a UPI Payment
val paymentRequest = PaymentRequest.Builder()
    .setAmount(100.0)
    .setRecipientVPA("user@paytm")
    .setDescription("Coffee payment")
    .setTransactionNote("Thanks for the coffee!")
    .build()

sfeSDK.initiatePayment(paymentRequest) { result ->
    when (result) {
        is PaymentResult.Success -> {
            println("Payment successful: ${result.transactionId}")
            // Update UI, show success message
        }
        is PaymentResult.Error -> {
            println("Payment failed: ${result.errorMessage}")
            // Handle error, show user-friendly message
        }
        is PaymentResult.Pending -> {
            println("Payment pending verification")
            // Show pending status
        }
    }
}
```

## 📋 Complete Integration Guide

### 1. Permissions

Add required permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
<uses-permission android:name="android.permission.CAMERA" /> <!-- For QR scanning -->
```

### 2. Initialize SDK in Application Class

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        SFEClientSDK.initialize(
            context = this,
            config = SFEConfig.Builder()
                .setApiBaseUrl("https://api.sfe-hackathon.com/")
                .setApiKey(BuildConfig.SFE_API_KEY)
                .setEnvironment(if (BuildConfig.DEBUG) SFEEnvironment.SANDBOX else SFEEnvironment.PRODUCTION)
                .setLogLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR)
                .build()
        )
    }
}
```

### 3. Authentication Flow

```kotlin
class AuthenticationActivity : AppCompatActivity() {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    private fun authenticateUser() {
        // Biometric Authentication
        sfeSDK.authenticateWithBiometrics(
            activity = this,
            title = "Authenticate Payment",
            subtitle = "Use your fingerprint to confirm",
            description = "Place your finger on the sensor"
        ) { result ->
            when (result) {
                is BiometricResult.Success -> {
                    // User authenticated, proceed with payment
                    proceedWithPayment()
                }
                is BiometricResult.Error -> {
                    // Handle authentication error
                    showError("Authentication failed: ${result.errorMessage}")
                }
                is BiometricResult.Cancelled -> {
                    // User cancelled authentication
                    showMessage("Authentication cancelled")
                }
            }
        }
    }
}
```

### 4. QR Code Payments

```kotlin
class QRPaymentActivity : AppCompatActivity() {
    
    private fun scanQRCode() {
        sfeSDK.scanQRCode(this) { result ->
            when (result) {
                is QRScanResult.Success -> {
                    val paymentData = result.paymentData
                    // Pre-fill payment form with QR data
                    showPaymentConfirmation(paymentData)
                }
                is QRScanResult.Error -> {
                    showError("QR scan failed: ${result.errorMessage}")
                }
            }
        }
    }
    
    private fun generateQRCode(amount: Double, description: String) {
        val qrRequest = QRGenerationRequest.Builder()
            .setAmount(amount)
            .setDescription(description)
            .setExpiryMinutes(15)
            .build()
            
        sfeSDK.generateQRCode(qrRequest) { result ->
            when (result) {
                is QRGenerationResult.Success -> {
                    // Display QR code bitmap
                    imageViewQR.setImageBitmap(result.qrCodeBitmap)
                }
                is QRGenerationResult.Error -> {
                    showError("QR generation failed: ${result.errorMessage}")
                }
            }
        }
    }
}
```

### 5. Transaction History

```kotlin
class TransactionHistoryActivity : AppCompatActivity() {
    
    private fun loadTransactionHistory() {
        val filter = TransactionFilter.Builder()
            .setStartDate(Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000)) // Last 30 days
            .setEndDate(Date())
            .setTransactionTypes(listOf(TransactionType.UPI, TransactionType.WALLET))
            .setPageSize(20)
            .build()
            
        sfeSDK.getTransactionHistory(filter) { result ->
            when (result) {
                is TransactionHistoryResult.Success -> {
                    // Update RecyclerView with transactions
                    transactionAdapter.updateTransactions(result.transactions)
                }
                is TransactionHistoryResult.Error -> {
                    showError("Failed to load history: ${result.errorMessage}")
                }
            }
        }
    }
}
```

## 🔧 Configuration Options

```kotlin
val config = SFEConfig.Builder()
    .setApiKey("your-api-key")
    .setEnvironment(SFEEnvironment.SANDBOX) // or PRODUCTION
    .setConnectionTimeout(30) // seconds
    .setReadTimeout(30) // seconds
    .enableBiometrics(true)
    .enableDeviceBinding(true)
    .setFraudDetectionLevel(FraudDetectionLevel.HIGH)
    .setEncryptionLevel(EncryptionLevel.AES_256)
    .setLogLevel(LogLevel.INFO)
    .build()
```

## 🛡️ Security Features

- **Device Binding**: Links transactions to specific devices
- **Certificate Pinning**: Prevents man-in-the-middle attacks
- **Root/Jailbreak Detection**: Blocks compromised devices
- **Transaction Signing**: Cryptographic verification of all transactions
- **Biometric Authentication**: Hardware-backed security

## 📊 Error Handling

```kotlin
sealed class PaymentResult {
    data class Success(
        val transactionId: String,
        val amount: Double,
        val timestamp: Date,
        val recipient: String
    ) : PaymentResult()
    
    data class Error(
        val errorCode: String,
        val errorMessage: String,
        val isRetryable: Boolean
    ) : PaymentResult()
    
    data class Pending(
        val transactionId: String,
        val estimatedCompletionTime: Date?
    ) : PaymentResult()
}
```

## 🔍 Testing

```kotlin
// For testing, use sandbox environment
val testSDK = SFEClientSDK.Builder(context)
    .setEnvironment(SFEEnvironment.SANDBOX)
    .setApiKey("test-api-key-12345")
    .enableMockPayments(true) // Returns mock successful responses
    .build()
```

## 📞 Support

- **Documentation**: [Full API Reference](https://uttam-mahata.github.io/SFE-Building-Hackathon/client-sdk/)
- **Issues**: [GitHub Issues](https://github.com/Uttam-Mahata/SFE-Building-Hackathon/issues)
- **Email**: uttam.mahata@example.com

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**⚠️ Hackathon Project Notice**: This is a prototype developed for hackathon demonstration. For production use, please ensure proper security audits and compliance validation.