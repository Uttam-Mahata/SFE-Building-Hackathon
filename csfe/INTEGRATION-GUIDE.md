# SFE Client SDK - Integration Guide

## Quick Start

### 1. Add Dependency

Add to your app-level `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.Uttam-Mahata.SFE-Building-Hackathon:csfe:v1.0.0'
}
```

### 2. Initialize SDK

In your Application class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        SFEClientSDK.initialize(
            context = this,
            config = SFEConfig.Builder()
                .setApiKey(BuildConfig.SFE_API_KEY)
                .setEnvironment(if (BuildConfig.DEBUG) SFEEnvironment.SANDBOX else SFEEnvironment.PRODUCTION)
                .enableBiometrics(true)
                .setFraudDetectionLevel(FraudDetectionLevel.HIGH)
                .enableMockPayments(BuildConfig.DEBUG)
                .build()
        )
    }
}
```

### 3. Basic Payment Flow

```kotlin
class PaymentActivity : AppCompatActivity() {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    private fun makePayment() {
        // Step 1: Authenticate user
        authenticateUser { success ->
            if (success) {
                processPayment()
            }
        }
    }
    
    private fun authenticateUser(callback: (Boolean) -> Unit) {
        sfeSDK.auth().authenticateWithBiometrics(
            activity = this,
            title = "Authenticate Payment",
            subtitle = "Confirm payment with biometric"
        ) { result ->
            when (result) {
                is BiometricResult.Success -> callback(true)
                is BiometricResult.Error -> {
                    showError("Authentication failed: ${result.errorMessage}")
                    callback(false)
                }
                is BiometricResult.Cancelled -> callback(false)
                is BiometricResult.NotAvailable -> {
                    // Fallback to PIN/password
                    callback(true)
                }
            }
        }
    }
    
    private fun processPayment() {
        val paymentRequest = PaymentRequest.Builder()
            .setAmount(500.0)
            .setRecipientVPA("merchant@paytm")
            .setDescription("Coffee payment")
            .setPaymentMode(PaymentMode.UPI)
            .build()
        
        sfeSDK.payments().initiatePayment(
            request = paymentRequest,
            userId = getCurrentUserId(),
            authToken = getAuthToken()
        ) { result ->
            handlePaymentResult(result)
        }
    }
    
    private fun handlePaymentResult(result: PaymentResult) {
        when (result) {
            is PaymentResult.Success -> {
                showSuccess("Payment successful! TxnID: ${result.transactionId}")
            }
            is PaymentResult.Error -> {
                showError("Payment failed: ${result.errorMessage}")
                if (result.isRetryable) {
                    showRetryOption()
                }
            }
            is PaymentResult.Pending -> {
                showPending("Payment is being processed. TxnID: ${result.transactionId}")
                trackPaymentStatus(result.transactionId)
            }
        }
    }
}
```

## Advanced Features

### Security Configuration

```kotlin
// High security configuration
val secureConfig = SFEConfig.Builder()
    .setApiKey("your-api-key")
    .setEnvironment(SFEEnvironment.PRODUCTION)
    .enableBiometrics(true)
    .enableDeviceBinding(true)
    .setFraudDetectionLevel(FraudDetectionLevel.HIGH)
    .setEncryptionLevel(EncryptionLevel.AES_256)
    .setLogLevel(LogLevel.ERROR)
    .build()
```

### QR Code Operations

```kotlin
class QRPaymentActivity : AppCompatActivity() {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    // Generate payment QR code
    private fun generatePaymentQR() {
        val qrRequest = QRGenerationRequest.Builder()
            .setAmount(1000.0)
            .setDescription("Restaurant bill")
            .setMerchantId("MERCHANT_001")
            .setExpiryMinutes(15)
            .build()
        
        sfeSDK.qr().generatePaymentQR(qrRequest) { result ->
            when (result) {
                is QRGenerationResult.Success -> {
                    displayQRCode(result.qrCodeBitmap)
                    scheduleExpiry(result.expiryTime)
                }
                is QRGenerationResult.Error -> {
                    showError("QR generation failed: ${result.errorMessage}")
                }
            }
        }
    }
    
    // Scan and parse QR code
    private fun handleQRScanResult(qrData: String) {
        sfeSDK.qr().parsePaymentQR(qrData) { result ->
            when (result) {
                is QRScanResult.Success -> {
                    val paymentData = result.paymentData
                    showPaymentConfirmation(paymentData)
                }
                is QRScanResult.Error -> {
                    showError("Invalid QR code: ${result.errorMessage}")
                }
            }
        }
    }
}
```

### Transaction Management

```kotlin
class TransactionHistoryActivity : AppCompatActivity() {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    private fun loadTransactionHistory() {
        val filter = TransactionFilter.Builder()
            .setStartDate(getLastMonthDate())
            .setEndDate(Date())
            .setTransactionTypes(listOf(TransactionType.UPI, TransactionType.WALLET))
            .setPageSize(20)
            .setPageNumber(0)
            .build()
        
        sfeSDK.transactions().getTransactionHistory(filter) { result ->
            when (result) {
                is TransactionHistoryResult.Success -> {
                    updateUI(result.transactions)
                    updatePagination(result.hasMore, result.currentPage)
                }
                is TransactionHistoryResult.Error -> {
                    showError("Failed to load history: ${result.errorMessage}")
                }
            }
        }
    }
    
    private fun searchTransactions(query: String) {
        sfeSDK.transactions().searchTransactions(query) { result ->
            when (result) {
                is TransactionHistoryResult.Success -> {
                    updateSearchResults(result.transactions)
                }
                is TransactionHistoryResult.Error -> {
                    showError("Search failed: ${result.errorMessage}")
                }
            }
        }
    }
}
```

### Wallet Operations

```kotlin
class WalletActivity : AppCompatActivity() {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    private fun loadWalletBalance() {
        sfeSDK.wallet().getWalletBalance(getCurrentUserId()) { result ->
            when (result) {
                is WalletBalanceResult.Success -> {
                    updateBalanceUI(result.balance, result.currency)
                }
                is WalletBalanceResult.Error -> {
                    showError("Failed to load balance: ${result.message}")
                }
            }
        }
    }
    
    private fun addMoneyToWallet(amount: Double) {
        sfeSDK.wallet().addMoney(
            userId = getCurrentUserId(),
            amount = amount,
            source = "BANK_TRANSFER"
        ) { result ->
            when (result) {
                is WalletTransactionResult.Success -> {
                    showSuccess("Money added successfully")
                    refreshBalance()
                }
                is WalletTransactionResult.Error -> {
                    showError("Failed to add money: ${result.message}")
                }
            }
        }
    }
}
```

### Security Integration

```kotlin
class SecurityManager {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    fun performSecurityChecks(): Boolean {
        val securityResult = sfeSDK.security().performSecurityChecks()
        
        if (!securityResult.passed) {
            handleSecurityIssues(securityResult.issues)
            return false
        }
        
        return true
    }
    
    private fun handleSecurityIssues(issues: List<SecurityIssue>) {
        issues.forEach { issue ->
            when (issue) {
                SecurityIssue.DEVICE_ROOTED -> {
                    showWarning("Device is rooted. This may pose security risks.")
                }
                SecurityIssue.DEBUGGING_ENABLED -> {
                    showWarning("USB debugging is enabled. Please disable for security.")
                }
                SecurityIssue.APP_TAMPERED -> {
                    blockApp("App integrity check failed. Please reinstall from official store.")
                }
                else -> {
                    logSecurityIssue(issue)
                }
            }
        }
    }
    
    fun bindDeviceToUser(userId: String) {
        val bindingResult = sfeSDK.security().bindDevice(userId)
        when (bindingResult) {
            is DeviceBindingResult.Success -> {
                saveBindingKey(bindingResult.bindingKey)
            }
            is DeviceBindingResult.Error -> {
                showError("Device binding failed: ${bindingResult.message}")
            }
        }
    }
}
```

### Fraud Detection Integration

```kotlin
class FraudMonitoringService {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    fun checkUserRiskProfile(userId: String) {
        sfeSDK.fraud().getUserRiskProfile(userId) { profile ->
            when (profile.overallRiskLevel) {
                FraudRiskLevel.HIGH -> {
                    enforceAdditionalSecurity(userId)
                    notifyRiskTeam(profile)
                }
                FraudRiskLevel.MEDIUM -> {
                    increaseMonitoring(userId)
                }
                FraudRiskLevel.LOW -> {
                    // Normal operations
                }
            }
        }
    }
    
    fun reportSuspiciousActivity(userId: String, activityType: SuspiciousActivityType, details: String) {
        sfeSDK.fraud().reportSuspiciousActivity(userId, activityType, details) { success ->
            if (success) {
                logSecurityEvent("Suspicious activity reported for user: $userId")
            }
        }
    }
}
```

## Error Handling Best Practices

### 1. Network Error Handling

```kotlin
private fun handlePaymentError(error: PaymentResult.Error) {
    when (error.errorCode) {
        "NETWORK_ERROR" -> {
            if (error.isRetryable) {
                showRetryDialog("Network issue. Would you like to retry?")
            } else {
                showError("Please check your internet connection")
            }
        }
        "FRAUD_DETECTED" -> {
            showFraudAlert("Transaction blocked for security reasons")
            redirectToSupport()
        }
        "INSUFFICIENT_BALANCE" -> {
            showError("Insufficient balance. Please add money to your wallet.")
            redirectToWallet()
        }
        else -> {
            showGenericError(error.errorMessage)
        }
    }
}
```

### 2. Security Error Handling

```kotlin
private fun handleSecurityError(errorCode: String) {
    when (errorCode) {
        "SECURITY_CHECK_FAILED" -> {
            blockTransactions()
            showSecurityWarning()
        }
        "DEVICE_BINDING_FAILED" -> {
            initiateDeviceBinding()
        }
        "ADDITIONAL_AUTH_REQUIRED" -> {
            promptAdditionalAuthentication()
        }
    }
}
```

## Testing and Development

### 1. Enable Mock Mode

```kotlin
// For development/testing
val devConfig = SFEConfig.Builder()
    .setApiKey("test-key")
    .setEnvironment(SFEEnvironment.SANDBOX)
    .enableMockPayments(true)
    .setLogLevel(LogLevel.DEBUG)
    .build()
```

### 2. Test Transaction Scenarios

```kotlin
class PaymentTestHelper {
    
    fun testSuccessfulPayment() {
        // Mock payments will return success for amounts < 50000
        testPayment(1000.0) // Should succeed
    }
    
    fun testFailedPayment() {
        // Test error scenarios
        testPayment(-100.0) // Should fail with INVALID_AMOUNT
    }
    
    fun testHighRiskPayment() {
        // Large amounts trigger fraud detection
        testPayment(75000.0) // May be blocked or require additional auth
    }
}
```

## Performance Optimization

### 1. Module Initialization

```kotlin
// Modules are lazy-loaded, but you can pre-initialize critical ones
class AppInitializer {
    fun preloadCriticalModules() {
        val sdk = SFEClientSDK.getInstance()
        
        // Pre-load security module for faster security checks
        sdk.security()
        
        // Pre-load auth module if user is logged in
        if (isUserLoggedIn()) {
            sdk.auth()
        }
    }
}
```

### 2. Memory Management

```kotlin
class PaymentActivity : AppCompatActivity() {
    
    override fun onDestroy() {
        super.onDestroy()
        // SDK handles cleanup automatically
        // No manual cleanup required
    }
}
```

## Production Deployment

### 1. ProGuard Configuration

Add to your `proguard-rules.pro`:

```proguard
# SFE Client SDK
-keep class com.gradientgeeks.csfe.** { *; }
-keep interface com.gradientgeeks.csfe.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Networking
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
```

### 2. Release Configuration

```kotlin
// Production configuration
val prodConfig = SFEConfig.Builder()
    .setApiKey(BuildConfig.SFE_PROD_API_KEY)
    .setEnvironment(SFEEnvironment.PRODUCTION)
    .enableBiometrics(true)
    .enableDeviceBinding(true)
    .setFraudDetectionLevel(FraudDetectionLevel.HIGH)
    .setEncryptionLevel(EncryptionLevel.AES_256)
    .setLogLevel(LogLevel.ERROR)
    .enableMockPayments(false)
    .build()
```

## Troubleshooting

### Common Issues

1. **Biometric Authentication Not Working**
   - Check if device supports biometrics
   - Verify biometric enrollment
   - Handle BiometricResult.NotAvailable case

2. **Payment Failures**
   - Check network connectivity
   - Verify API key configuration
   - Review fraud detection settings

3. **Security Check Failures**
   - Review device security state
   - Check for root/debugging
   - Verify app signature

### Debug Logging

```kotlin
// Enable debug logging for troubleshooting
val debugConfig = SFEConfig.Builder()
    .setLogLevel(LogLevel.DEBUG)
    .setDebugMode(true)
    .build()
```

## Support and Documentation

- **API Reference**: Complete Javadoc documentation included
- **Sample Code**: Check the `/samples` directory
- **GitHub Issues**: Report bugs and feature requests
- **Integration Support**: Contact development team

This integration guide provides comprehensive coverage for implementing the SFE Client SDK in your Android application. Follow these patterns for robust, secure financial transaction processing.