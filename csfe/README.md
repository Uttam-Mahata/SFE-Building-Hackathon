# SFE Client SDK - Secure Financial Environment - IIEST UCO Bank Hackathon

> **🚀 Hackathon Project**: This is a prototype developed for IIEST-UCO Bank Hackathon. The Secure Financial Environment (SFE) is a secure transaction layer for mobile financial applications.

## Overview

This client SDK provides a headless, secure financial transaction layer for Android applications, ensuring regulatory compliance and enterprise-grade security for payment apps.

## Features

- 🔐 **End-to-end Encryption** - AES-256 encryption for transactions
- 🛡️ **Biometric Authentication** - Fingerprint, Face ID, PIN support
- 🏦 **Regulatory Compliant** - Built with compliance in mind
- 📱 **Kotlin Support** - Native Android development
- 🛡️ **Security Features** - Device binding, fraud alerts, encryption

## Installation

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
    implementation 'com.github.Uttam-Mahata:SFE-Building-Hackathon:v1.0.0'
}
```

## Basic Usage

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

sfeSDK.payments().initiatePayment(paymentRequest) { result ->
    when (result) {
        is PaymentResult.Success -> {
            println("Payment successful: ${result.transactionId}")
        }
        is PaymentResult.Error -> {
            println("Payment failed: ${result.errorMessage}")
        }
        is PaymentResult.Pending -> {
            println("Payment pending verification")
        }
    }
}
```

## License

This project is licensed under the MIT License.

## Hackathon Project Notice

**⚠️ This is a prototype developed for hackathon demonstration. For production use, please ensure proper security audits and compliance validation.**
