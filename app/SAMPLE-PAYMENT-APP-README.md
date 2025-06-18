# SFE Sample Payment App - Demo Application - IIEST-UCO Bank Hackathon

[![](https://jitpack.io/v/Uttam-Mahata/SFE-Building-Hackathon.svg)](https://jitpack.io/#Uttam-Mahata/SFE-Building-Hackathon)

> **🚀 Hackathon Demo**: Sample payment application showcasing SFE Client SDK integration for secure financial transactions.

## Overview

This is a demonstration Android application that showcases how to integrate the SFE Client SDK for building secure, RBI/NPCI compliant payment applications. Perfect for developers learning fintech app development.

## ✨ Demo Features

- 💳 **UPI Payments** - Send money using UPI VPA or mobile number
- 📱 **QR Code Payments** - Scan or generate QR codes for payments
- 👆 **Biometric Auth** - Fingerprint and Face ID authentication
- 💰 **Wallet Management** - Digital wallet with balance tracking
- 📊 **Transaction History** - Complete payment history with filters
- 🔔 **Push Notifications** - Real-time payment notifications
- 🛡️ **Security Features** - Device binding, fraud alerts, encryption

## 🚀 Quick Start

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 21+ (Android 5.0+)
- Device with biometric capability (optional)

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/Uttam-Mahata/SFE-Building-Hackathon.git
cd SFE-Building-Hackathon/sample-payment-app
```

2. **Open in Android Studio:**
```bash
# Open Android Studio and import the sample-payment-app module
```

3. **Configure API Keys:**
```kotlin
// In local.properties
SFE_API_KEY=your-api-key-here
SFE_BASE_URL=https://api.sfe-hackathon.com/
```

4. **Build and Run:**
```bash
./gradlew assembleDebug
```

## 📱 App Walkthrough

### 1. Registration & KYC
```kotlin
// MainActivity.kt - User Registration Flow
class MainActivity : AppCompatActivity() {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    private fun startRegistration() {
        val registrationData = RegistrationData(
            phoneNumber = binding.etPhone.text.toString(),
            email = binding.etEmail.text.toString(),
            fullName = binding.etName.text.toString()
        )
        
        sfeSDK.auth().register(registrationData) { result ->
            when (result) {
                is RegistrationResult.Success -> {
                    // Move to KYC verification
                    startKYCVerification(result.userId)
                }
                is RegistrationResult.Error -> {
                    showError("Registration failed: ${result.message}")
                }
            }
        }
    }
    
    private fun startKYCVerification(userId: String) {
        val intent = Intent(this, KYCActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
}
```

### 2. Dashboard & Wallet
```kotlin
// DashboardActivity.kt - Main App Interface
class DashboardActivity : AppCompatActivity() {
    
    private fun loadWalletBalance() {
        sfeSDK.wallet().getBalance { result ->
            when (result) {
                is WalletResult.Success -> {
                    binding.tvBalance.text = "₹${result.balance}"
                    updateUI(result.walletData)
                }
                is WalletResult.Error -> {
                    showError("Failed to load balance")
                }
            }
        }
    }
    
    private fun setupQuickActions() {
        binding.btnSendMoney.setOnClickListener {
            startActivity(Intent(this, SendMoneyActivity::class.java))
        }
        
        binding.btnScanPay.setOnClickListener {
            startQRScanner()
        }
        
        binding.btnRequestMoney.setOnClickListener {
            startActivity(Intent(this, RequestMoneyActivity::class.java))
        }
    }
}
```

### 3. UPI Payment Flow
```kotlin
// SendMoneyActivity.kt - UPI Payment Implementation
class SendMoneyActivity : AppCompatActivity() {
    
    private fun initiatePayment() {
        val paymentRequest = PaymentRequest.Builder()
            .setRecipientVPA(binding.etVPA.text.toString())
            .setAmount(binding.etAmount.text.toString().toDouble())
            .setDescription(binding.etNote.text.toString())
            .setPaymentMode(PaymentMode.UPI)
            .build()
        
        // Show confirmation dialog with payment details
        showPaymentConfirmation(paymentRequest) { confirmed ->
            if (confirmed) {
                authenticateAndPay(paymentRequest)
            }
        }
    }
    
    private fun authenticateAndPay(request: PaymentRequest) {
        sfeSDK.auth().authenticateWithBiometrics(
            activity = this,
            title = "Confirm Payment",
            subtitle = "Pay ₹${request.amount} to ${request.recipientVPA}",
            description = "Use your fingerprint to confirm this payment"
        ) { authResult ->
            when (authResult) {
                is BiometricResult.Success -> {
                    processPayment(request, authResult.token)
                }
                is BiometricResult.Error -> {
                    showError("Authentication failed: ${authResult.errorMessage}")
                }
                is BiometricResult.Cancelled -> {
                    showMessage("Payment cancelled")
                }
            }
        }
    }
    
    private fun processPayment(request: PaymentRequest, authToken: String) {
        showProgressDialog("Processing payment...")
        
        sfeSDK.payments().initiatePayment(request, authToken) { result ->
            hideProgressDialog()
            
            when (result) {
                is PaymentResult.Success -> {
                    showSuccessScreen(result)
                    // Send success analytics
                    analyticsService.trackPaymentSuccess(result.transactionId)
                }
                is PaymentResult.Error -> {
                    showErrorScreen(result.errorMessage)
                    analyticsService.trackPaymentError(result.errorCode)
                }
                is PaymentResult.Pending -> {
                    showPendingScreen(result.transactionId)
                }
            }
        }
    }
}
```

### 4. QR Code Functionality
```kotlin
// QRScannerActivity.kt - QR Code Scanning
class QRScannerActivity : AppCompatActivity() {
    
    private fun startQRScanner() {
        sfeSDK.qr().startScanner(this) { result ->
            when (result) {
                is QRScanResult.Success -> {
                    val paymentData = result.paymentData
                    // Pre-populate payment form
                    showPaymentForm(paymentData)
                }
                is QRScanResult.InvalidQR -> {
                    showError("Invalid payment QR code")
                }
                is QRScanResult.Error -> {
                    showError("QR scan failed: ${result.errorMessage}")
                }
            }
        }
    }
    
    private fun showPaymentForm(data: QRPaymentData) {
        val intent = Intent(this, SendMoneyActivity::class.java).apply {
            putExtra("recipient", data.recipientVPA)
            putExtra("amount", data.amount)
            putExtra("description", data.description)
            putExtra("merchantName", data.merchantName)
        }
        startActivity(intent)
        finish()
    }
}

// QRGeneratorActivity.kt - Generate QR for receiving payments
class QRGeneratorActivity : AppCompatActivity() {
    
    private fun generateQRCode() {
        val qrRequest = QRGenerationRequest.Builder()
            .setAmount(binding.etAmount.text.toString().toDoubleOrNull())
            .setDescription(binding.etDescription.text.toString())
            .setExpiryMinutes(30)
            .build()
        
        sfeSDK.qr().generateQRCode(qrRequest) { result ->
            when (result) {
                is QRGenerationResult.Success -> {
                    binding.imageViewQR.setImageBitmap(result.qrCodeBitmap)
                    binding.tvQRData.text = "QR Code valid for 30 minutes"
                    startQRExpiryTimer()
                }
                is QRGenerationResult.Error -> {
                    showError("Failed to generate QR code")
                }
            }
        }
    }
}
```

### 5. Transaction History
```kotlin
// TransactionHistoryActivity.kt - Transaction Management
class TransactionHistoryActivity : AppCompatActivity() {
    
    private val transactionAdapter = TransactionAdapter()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)
        
        setupRecyclerView()
        loadTransactionHistory()
    }
    
    private fun loadTransactionHistory() {
        val filter = TransactionFilter.Builder()
            .setStartDate(getStartDate())
            .setEndDate(Date())
            .setTransactionTypes(getSelectedTypes())
            .setPageSize(20)
            .build()
        
        sfeSDK.transactions().getHistory(filter) { result ->
            when (result) {
                is TransactionHistoryResult.Success -> {
                    transactionAdapter.updateTransactions(result.transactions)
                    updateEmptyState(result.transactions.isEmpty())
                }
                is TransactionHistoryResult.Error -> {
                    showError("Failed to load transaction history")
                }
            }
        }
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        
        transactionAdapter.setOnTransactionClickListener { transaction ->
            showTransactionDetails(transaction)
        }
    }
}
```

### 6. Security Features Demo
```kotlin
// SecurityDemoActivity.kt - Showcase security features
class SecurityDemoActivity : AppCompatActivity() {
    
    private fun demonstrateSecurityFeatures() {
        // Device binding status
        sfeSDK.security().getDeviceBindingStatus { status ->
            binding.tvDeviceBinding.text = if (status.isBound) {
                "✅ Device is securely bound"
            } else {
                "⚠️ Device binding required"
            }
        }
        
        // Fraud detection demo
        binding.btnTriggerFraudCheck.setOnClickListener {
            demonstrateFraudDetection()
        }
        
        // Encryption demo
        binding.btnShowEncryption.setOnClickListener {
            demonstrateEncryption()
        }
    }
    
    private fun demonstrateFraudDetection() {
        // Create a suspicious transaction pattern
        val suspiciousPayment = PaymentRequest.Builder()
            .setAmount(100000.0) // Large amount
            .setRecipientVPA("suspicious@user")
            .setPaymentMode(PaymentMode.UPI)
            .build()
        
        sfeSDK.fraud().analyzeTransaction(suspiciousPayment) { result ->
            when (result.riskLevel) {
                RiskLevel.HIGH -> {
                    showAlert("🚨 High Risk Transaction Detected!", 
                             "Reason: ${result.reason}")
                }
                RiskLevel.MEDIUM -> {
                    showAlert("⚠️ Medium Risk - Additional verification required")
                }
                RiskLevel.LOW -> {
                    showAlert("✅ Transaction appears safe")
                }
            }
        }
    }
}
```

## 🔧 Configuration

```kotlin
// Application.kt - App Configuration
class PaymentApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SFE Client SDK
        SFEClientSDK.initialize(
            context = this,
            config = SFEConfig.Builder()
                .setApiKey(BuildConfig.SFE_API_KEY)
                .setBaseUrl(BuildConfig.SFE_BASE_URL)
                .setEnvironment(if (BuildConfig.DEBUG) SFEEnvironment.SANDBOX else SFEEnvironment.PRODUCTION)
                .enableBiometrics(true)
                .enableDeviceBinding(true)
                .setLogLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR)
                .build()
        )
        
        // Setup crash reporting
        if (!BuildConfig.DEBUG) {
            setupCrashlytics()
        }
    }
}
```

## 🎨 UI/UX Features

- **Material Design 3**: Modern, accessible interface
- **Dark Mode Support**: Automatic theme switching
- **Accessibility**: Screen reader support, high contrast
- **Offline Mode**: Queue transactions when offline
- **Biometric Integration**: Seamless fingerprint/face recognition

## 🧪 Testing

```kotlin
// ExampleInstrumentedTest.kt - Integration Tests
@RunWith(AndroidJUnit4::class)
class PaymentFlowInstrumentedTest {
    
    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java)
    
    @Test
    fun testCompletePaymentFlow() {
        // Test user registration
        onView(withId(R.id.etPhone)).perform(typeText("9876543210"))
        onView(withId(R.id.btnRegister)).perform(click())
        
        // Verify SDK integration
        onView(withText("Registration successful")).check(matches(isDisplayed()))
        
        // Test payment initiation
        onView(withId(R.id.btnSendMoney)).perform(click())
        onView(withId(R.id.etVPA)).perform(typeText("test@paytm"))
        onView(withId(R.id.etAmount)).perform(typeText("100"))
        onView(withId(R.id.btnPay)).perform(click())
        
        // Verify payment confirmation
        onView(withText("Payment initiated")).check(matches(isDisplayed()))
    }
}
```

## 📱 Screenshots

| Dashboard | Send Money | QR Scanner | Transaction History |
|-----------|------------|------------|-------------------|
| ![Dashboard](screenshots/dashboard.png) | ![Send Money](screenshots/send_money.png) | ![QR Scanner](screenshots/qr_scanner.png) | ![History](screenshots/history.png) |

## 🔗 Integration Examples

This sample app demonstrates:

- **5-minute SDK integration** - See how quickly you can add payments
- **Biometric authentication** - Secure user verification
- **QR code payments** - Scan-to-pay functionality  
- **Real-time notifications** - Payment status updates
- **Fraud detection** - Security alerts and blocking
- **Compliance features** - Automatic RBI/NPCI adherence

## 📞 Support

- **Documentation**: [Integration Guide](../docs/integration-guide.md)
- **Issues**: [GitHub Issues](https://github.com/Uttam-Mahata/SFE-Building-Hackathon/issues)
- **Email**: uttam.mahata@example.com

## 📄 License

MIT License - see [LICENSE](../LICENSE) file for details.

---

**⚠️ Demo Application**: This is a hackathon prototype for demonstration purposes. Not intended for production use without proper security audits.