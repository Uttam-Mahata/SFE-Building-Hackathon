package com.gradientgeeks.csfe.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.config.SFEEnvironment
import com.gradientgeeks.csfe.config.EncryptionLevel
import com.gradientgeeks.csfe.config.FraudDetectionLevel
import com.gradientgeeks.csfe.config.LogLevel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for SecurityModule and related security components
 */
@RunWith(RobolectricTestRunner::class)
class SecurityModuleTest {
    
    private lateinit var context: Context
    private lateinit var config: SFEConfig
    private lateinit var securityModule: SecurityModule
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        config = SFEConfig(
            apiKey = "test-api-key",
            environment = SFEEnvironment.SANDBOX,
            apiBaseUrl = "https://test.api.com",
            enableBiometrics = true,
            enableDeviceBinding = true,
            debugMode = true,
            connectionTimeout = 30,
            readTimeout = 30,
            fraudDetectionLevel = FraudDetectionLevel.MEDIUM,
            encryptionLevel = EncryptionLevel.AES_256,
            logLevel = LogLevel.DEBUG,
            enableMockPayments = true
        )
        securityModule = SecurityModule(context, config)
    }
    
    @Test
    fun testSecurityCheckExecution() {
        // Test that security checks can be executed without crashing
        val result = securityModule.performSecurityChecks()
        
        assertNotNull("Security check result should not be null", result)
        assertNotNull("Detection results should not be null", result.detectionResults)
        assertNotNull("Threats list should not be null", result.threats)
    }
    
    @Test
    fun testEncryptionDecryption() {
        val testData = "sensitive_data_123"
        
        // Test encryption
        val encrypted = securityModule.encryptData(testData)
        assertNotNull("Encrypted data should not be null", encrypted)
        assertNotEquals("Encrypted data should be different from original", testData, encrypted)
        
        // Test decryption
        val decrypted = securityModule.decryptData(encrypted)
        assertEquals("Decrypted data should match original", testData, decrypted)
    }
    
    @Test
    fun testDeviceBindingStatus() {
        var callbackExecuted = false
        var bindingStatus: DeviceBindingStatus? = null
        
        securityModule.getDeviceBindingStatus { status ->
            callbackExecuted = true
            bindingStatus = status
        }
        
        assertTrue("Callback should be executed", callbackExecuted)
        assertNotNull("Binding status should not be null", bindingStatus)
    }
    
    @Test
    fun testDeviceBinding() {
        var callbackExecuted = false
        var bindingResult: DeviceBindingResult? = null
        
        securityModule.bindDevice("test-user-123") { result ->
            callbackExecuted = true
            bindingResult = result
        }
        
        assertTrue("Callback should be executed", callbackExecuted)
        assertNotNull("Binding result should not be null", bindingResult)
    }
    
    @Test
    fun testLegacySecurityMethods() {
        // Test that legacy methods still work
        val isEmulator = securityModule.isEmulator()
        val isRooted = securityModule.isRooted()
        val isDebuggerAttached = securityModule.isDebuggerAttached()
        
        // These should not throw exceptions
        assertNotNull("Emulator check should return a value", isEmulator)
        assertNotNull("Root check should return a value", isRooted)
        assertNotNull("Debugger check should return a value", isDebuggerAttached)
    }
}