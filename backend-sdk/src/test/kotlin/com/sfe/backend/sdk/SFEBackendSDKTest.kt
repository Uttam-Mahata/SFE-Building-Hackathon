package com.sfe.backend.sdk

import com.sfe.backend.models.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class SFEBackendSDKTest {
    
    @Test
    fun `test SDK builder creates instance successfully`() {
        val sdk = SFEBackendSDK.Builder()
            .setApiKey("test-key")
            .setEnvironment(SFEEnvironment.TEST)
            .enableMockMode(true)
            .build()
        
        assertNotNull(sdk)
        assertEquals("test-key", sdk.getConfiguration().apiKey)
        assertEquals(SFEEnvironment.TEST, sdk.getConfiguration().environment)
        assertTrue(sdk.getConfiguration().mockModeEnabled)
    }
    
    @Test
    fun `test SDK builder throws exception for missing API key`() {
        assertThrows<SFEConfigurationException> {
            SFEBackendSDK.Builder()
                .setEnvironment(SFEEnvironment.TEST)
                .build()
        }
    }
    
    @Test
    fun `test payment validation with invalid amount`() {
        val sdk = SFEBackendSDK.Builder()
            .setApiKey("test-key")
            .setEnvironment(SFEEnvironment.TEST)
            .build()
        
        val invalidRequest = PaymentRequest(
            userId = "test-user",
            amount = BigDecimal.ZERO,
            transactionType = TransactionType.UPI,
            recipientDetails = RecipientDetails(name = "Test User")
        )
        
        assertThrows<SFEValidationException> {
            sdk.payments().validateRequest(invalidRequest)
        }
    }
    
    @Test
    fun `test fraud service blacklist functionality`() {
        val sdk = SFEBackendSDK.Builder()
            .setApiKey("test-key")
            .setEnvironment(SFEEnvironment.TEST)
            .build()
        
        val userId = "test-user"
        val reason = "Suspicious activity"
        
        sdk.fraud().blacklistUser(userId, reason)
        
        val request = PaymentRequest(
            userId = userId,
            amount = BigDecimal("100.00"),
            transactionType = TransactionType.UPI,
            recipientDetails = RecipientDetails(name = "Test User")
        )
        
        val riskAssessment = sdk.fraud().analyzeTransaction(request)
        assertEquals(RiskLevel.CRITICAL, riskAssessment.riskLevel)
        assertEquals(1.0, riskAssessment.riskScore)
    }
    
    @Test
    fun `test KYC verification with valid data`() {
        val sdk = SFEBackendSDK.Builder()
            .setApiKey("test-key")
            .setEnvironment(SFEEnvironment.TEST)
            .build()
        
        val result = sdk.kyc()
            .verifyAadhaar("123456789012")
            .verifyPAN("ABCDE1234F")
            .verifyBankAccount(BankAccountDetails("12345678", "SBIN0000123", "State Bank of India"))
            .performVideoKYC(VideoKYCData("video_url", 0.9, true, true))
        
        assertTrue(result.isVerified)
        assertEquals(RiskLevel.LOW, result.riskProfile.riskLevel)
    }
    
    @Test
    fun `test authentication service OTP generation`() {
        val sdk = SFEBackendSDK.Builder()
            .setApiKey("test-key")
            .setEnvironment(SFEEnvironment.TEST)
            .build()
        
        val phoneNumber = "+91 9876543210"
        val otp = sdk.auth().generateOTP(phoneNumber)
        
        assertNotNull(otp)
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })
        
        // Test OTP verification
        assertTrue(sdk.auth().verifyOTP(phoneNumber, otp))
    }
    
    @Test
    fun `test monitoring service metrics`() {
        val sdk = SFEBackendSDK.Builder()
            .setApiKey("test-key")
            .setEnvironment(SFEEnvironment.TEST)
            .build()
        
        val transaction = TransactionRecord(
            id = "test-txn-1",
            userId = "test-user",
            amount = BigDecimal("100.00"),
            currency = "INR",
            transactionType = TransactionType.UPI,
            status = PaymentStatus.COMPLETED,
            recipientDetails = RecipientDetails(name = "Test User"),
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        
        sdk.monitoring().recordTransactionMetric(transaction)
        
        val metrics = sdk.monitoring().getCurrentMetrics()
        assertEquals(1L, metrics.transactionCount)
        assertEquals(1L, metrics.successfulCount)
        assertEquals(0L, metrics.failedCount)
        assertEquals(1.0, metrics.successRate)
    }
}