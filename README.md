# Secure Financial Environment (SFE) SDK Integration Guide

## Overview

The Secure Financial Environment (SFE) SDK provides a standardized security framework for payment applications in India, designed to work across diverse payment providers including banks, fintech companies, and third-party payment processors. This document outlines how different payment providers can integrate the SFE SDK with their existing UI and backend systems to meet RBI/NPCI regulatory requirements.

## Integration Architecture

The SFE SDK is designed to integrate seamlessly with various payment app architectures while maintaining consistent security standards:

```
┌───────────────────────────────────────────────────────────────────┐
│                                                                   │
│                     Payment App (Any Provider)                    │
│                                                                   │
│  ┌───────────────┐      ┌───────────────────────────────┐         │
│  │               │      │                               │         │
│  │   Provider's  │      │      SFE Secure Container     │         │
│  │      UI       │──────│                               │         │
│  │   Components  │      │    (Standardized Security)    │         │
│  │               │      │                               │         │
│  └───────────────┘      └───────────────────────────────┘         │
│          │                            │                           │
│          │                            │                           │
│  ┌───────────────┐      ┌───────────────────────────────┐         │
│  │  Provider's   │      │                               │         │
│  │   Business    │──────│    SFE Security Layer APIs    │         │
│  │    Logic      │      │                               │         │
│  └───────────────┘      └───────────────────────────────┘         │
│                                       │                           │
└───────────────────────────────────────│───────────────────────────┘
                                        │
                                        ▼
          ┌─────────────────────────────────────────────────┐
          │                                                 │
          │            Provider's Backend Systems           │
          │                                                 │
          │  ┌─────────────────┐    ┌───────────────────┐   │
          │  │  Provider API   │    │  SFE Verification │   │
          │  │    Endpoints    │────│      Module       │   │
          │  └─────────────────┘    └───────────────────┘   │
          │                                │               │
          └────────────────────────────────│───────────────┘
                                           │
                                           ▼
                            ┌───────────────────────────┐
                            │                           │
                            │    RBI/NPCI Regulatory    │
                            │         Systems           │
                            │                           │
                            └───────────────────────────┘
```

## Provider-Specific Integration Guidelines

### 1. Bank Payment Applications

Banks typically have robust security infrastructures and complex backend systems. Integration focuses on complementing existing security measures.

#### UI Integration for Banks

```java
// Example: Bank App Activity with SFE Integration
public class BankPaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_payment);
        
        // Initialize SFE with bank-specific configuration
        SFEConfiguration config = new SFEConfiguration.Builder()
            .setAppId("bank-app-registered-id")
            .setProviderType(SFEProviderType.BANK)
            .setCustomSecurityLevel(SFESecurityLevel.HIGH)
            .setAllowedAPIs(BankSpecificAPIs.getAllowedEndpoints())
            .build();
            
        SecureFinancialEnvironment.initialize(this, config, initResult -> {
            if (initResult.isSuccess()) {
                enableSecureTransactions();
            } else {
                // Handle initialization failure according to bank policy
                showSecurityAlert(initResult.getErrorReason());
            }
        });
    }
    
    private void processPayment() {
        // Get payment data from bank UI
        BankPaymentData paymentData = collectPaymentDataFromUI();
        
        // Process through SFE secure channel
        SecureFinancialEnvironment.getSecureCommunication()
            .sendBankTransaction(
                bankConfig.getSecureEndpoint(),
                paymentData,
                bankConfig.getTransactionTimeout(),
                result -> {
                    if (result.isSuccess()) {
                        updateBankAccountDisplayAndRecords(result.getTransactionData());
                    } else {
                        handleBankTransactionFailure(result.getErrorCode());
                    }
                }
            );
    }
}
```

#### Bank Backend Integration

```java
// Example: Bank API Controller with SFE Verification

@RestController
@RequestMapping("/api/bank/transactions")
public class BankTransactionController {

    @Autowired
    private SFEBankVerificationService sfeVerification;
    
    @Autowired
    private BankCoreSystemService bankCoreSystem;
    
    @Autowired
    private RBIReportingService rbiReporting;
    
    @PostMapping("/process")
    public ResponseEntity<?> processTransaction(@RequestBody String encryptedPayload,
                                               HttpServletRequest request) {
        
        // 1. Verify SFE security signatures and attestation
        BankSecurityVerificationResult securityResult = 
            sfeVerification.verifyTransaction(request, encryptedPayload);
            
        if (!securityResult.isValid()) {
            // Log security failure and report to monitoring systems
            bankCoreSystem.logSecurityFailure(securityResult);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Security verification failed"));
        }
        
        // 2. Decode transaction with bank's keys
        BankTransactionRequest transaction = 
            bankCoreSystem.decodeVerifiedTransaction(securityResult.getVerifiedPayload());
        
        // 3. Process through bank's core banking system
        BankTransactionResult result = bankCoreSystem.processTransaction(transaction);
        
        // 4. Report security metrics to RBI
        BankRegulatoryReport report = new BankRegulatoryReport(
            transaction, securityResult.getSecurityMetrics());
        rbiReporting.submitReport(report);
        
        // 5. Return secure response
        return ResponseEntity.ok(
            sfeVerification.createSecureResponse(result)
        );
    }
}
```

### 2. Fintech Payment Applications

Fintech apps typically have modern architectures but may need more comprehensive security controls to meet regulatory requirements.

#### UI Integration for Fintech Apps

```javascript
// React Native example for fintech apps
import React, { useEffect, useState } from 'react';
import { View, Text, Button, Alert } from 'react-native';
import { SecureFinancialEnvironment } from '@npci/sfe-react-native';

function FintechPaymentScreen({ navigation }) {
  const [sfeReady, setSfeReady] = useState(false);
  
  useEffect(() => {
    // Initialize SFE with fintech-specific configuration
    const initSFE = async () => {
      try {
        await SecureFinancialEnvironment.initialize({
          appId: 'fintech-registered-app-id',
          providerType: 'FINTECH',
          customBranding: {
            primaryColor: '#1E88E5',
            securityIndicatorStyle: 'modern'
          }
        });
        setSfeReady(true);
      } catch (error) {
        Alert.alert(
          'Security Setup Failed',
          'Unable to establish secure environment. Please update the app and try again.'
        );
      }
    };
    
    initSFE();
  }, []);
  
  const handlePayment = async () => {
    if (!sfeReady) {
      Alert.alert('Security Environment Not Ready', 'Please wait while we secure your transaction.');
      return;
    }
    
    // Collect payment data from fintech UI
    const paymentDetails = collectPaymentDetails();
    
    try {
      // Process through SFE secure channel
      const result = await SecureFinancialEnvironment.sendTransaction({
        url: 'https://fintech-api.example.com/payments/process',
        data: paymentDetails,
        options: {
          // Fintech-specific options
          retryPolicy: 'aggressive',
          analyticsEnabled: true
        }
      });
      
      // Handle success in the fintech app UI
      navigation.navigate('PaymentSuccess', { transactionId: result.id });
      
    } catch (error) {
      // Handle security or transaction failures
      handleFintechTransactionError(error);
    }
  };
  
  return (
    <View style={styles.container}>
      {/* Fintech UI components */}
      <Text style={styles.securityIndicator}>
        {sfeReady ? '🔒 Secure Transaction Ready' : 'Setting up security...'}
      </Text>
      <Button 
        title="Make Payment" 
        onPress={handlePayment}
        disabled={!sfeReady} 
      />
    </View>
  );
}
```

#### Fintech Backend Integration

```javascript
// Node.js/Express example for fintech backend
const express = require('express');
const { SFEServerSecurity } = require('@npci/sfe-server-verification');
const { FintechTransactionProcessor } = require('./fintech-core');
const { RegulatoryReporting } = require('./regulatory-service');

const router = express.Router();
const sfeVerifier = new SFEServerSecurity({
  fintechId: 'registered-fintech-id',
  verificationKeys: process.env.SFE_VERIFICATION_KEYS
});

// Fintech payment processing endpoint
router.post('/payments/process', async (req, res) => {
  try {
    // 1. Verify SFE security signatures and attestation
    const securityResult = await sfeVerifier.verifyRequest(req);
    
    if (!securityResult.isValid) {
      // Log security issue and return appropriate response
      console.error('Security verification failed:', securityResult.reason);
      return res.status(403).json({ 
        error: 'Security verification failed',
        code: 'SECURITY_CHECK_FAILED'
      });
    }
    
    // 2. Extract verified payment data
    const paymentData = securityResult.verifiedData;
    
    // 3. Process through fintech payment processor
    const fintechProcessor = new FintechTransactionProcessor();
    const paymentResult = await fintechProcessor.processPayment(paymentData);
    
    // 4. Report security metrics to regulatory systems
    const regulatoryReporting = new RegulatoryReporting();
    await regulatoryReporting.reportTransaction({
      transactionId: paymentResult.id,
      amount: paymentData.amount,
      timestamp: new Date(),
      securityMetrics: securityResult.securityMetrics,
      fintechMetadata: {
        providerId: 'FINTECH-123',
        channelType: 'MOBILE_APP'
      }
    });
    
    // 5. Return secure response
    const secureResponse = await sfeVerifier.createSecureResponse(paymentResult);
    return res.json(secureResponse);
    
  } catch (error) {
    console.error('Payment processing error:', error);
    return res.status(500).json({ 
      error: 'Payment processing failed',
      code: 'PROCESSING_ERROR'
    });
  }
});

module.exports = router;
```
