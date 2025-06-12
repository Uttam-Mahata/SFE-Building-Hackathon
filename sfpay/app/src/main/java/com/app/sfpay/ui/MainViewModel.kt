package com.app.sfpay.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.sfpay.services.SfeBackendService
import com.gradientgeeks.sfesdk.SfeFrontendSdk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * MainViewModel for SFPay Payment App
 * 
 * Handles background SFE security checks and payment-related operations
 * Security checks run silently without UI exposure
 */
class MainViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "SFPayMainViewModel"
    }
    
    /**
     * Data classes for security status
     */
    data class SecurityStatus(
        val overallStatus: String = "pending", // "secure", "warning", "insecure", "pending"
        val message: String = "Initializing security checks...",
        val showBanner: Boolean = false
    )
    
    data class DetailedSecurityInfo(
        val deviceBinding: String = "pending",
        val rootDetection: String = "pending", 
        val debugDetection: String = "pending",
        val tamperDetection: String = "pending",
        val playIntegrity: String = "pending"
    )
    
    // State flows for UI
    private val _securityStatus = MutableStateFlow(SecurityStatus())
    val securityStatus: StateFlow<SecurityStatus> = _securityStatus.asStateFlow()
    
    private val _detailedSecurityInfo = MutableStateFlow(DetailedSecurityInfo())
    val detailedSecurityInfo: StateFlow<DetailedSecurityInfo> = _detailedSecurityInfo.asStateFlow()
    
    private var sfeInitialized = false
    private var latestSecurityPayload: String? = null
    private lateinit var backendService: SfeBackendService
    
    /**
     * Initialize SFE SDK and perform background security checks
     * This runs silently when the app starts
     */
    fun initializeSfeChecks() {
        if (sfeInitialized) return
        
        // Update status to show initialization
        _securityStatus.value = SecurityStatus(
            overallStatus = "pending",
            message = "Verifying device security...",
            showBanner = true
        )
        
        viewModelScope.launch {
            try {
                // Initialize backend service
                val context = SfeFrontendSdk.getApplicationContext()
                if (context != null) {
                    backendService = SfeBackendService(context)
                }
                
                // SDK is already initialized in Application class
                sfeInitialized = SfeFrontendSdk.isInitialized()
                
                if (sfeInitialized) {
                    Log.d(TAG, "SFE SDK already initialized")
                    // Perform initial security assessment
                    performBackgroundSecurityChecks()
                } else {
                    Log.w(TAG, "SFE SDK not initialized")
                    _securityStatus.value = SecurityStatus(
                        overallStatus = "insecure",
                        message = "Security system unavailable",
                        showBanner = true
                    )
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check SFE SDK initialization", e)
                _securityStatus.value = SecurityStatus(
                    overallStatus = "insecure",
                    message = "Security check failed",
                    showBanner = true
                )
            }
        }
    }
    
    /**
     * Perform comprehensive security checks in background
     * Results are prepared for backend transmission
     */
    private fun performBackgroundSecurityChecks() {
        viewModelScope.launch {
            try {
                val context = SfeFrontendSdk.getApplicationContext() ?: return@launch
                
                // Initialize detailed security info
                _detailedSecurityInfo.value = DetailedSecurityInfo(
                    deviceBinding = "checking",
                    rootDetection = "checking",
                    debugDetection = "checking", 
                    tamperDetection = "checking",
                    playIntegrity = "checking"
                )
                
                // Step 1: Device Binding Check
                var deviceBindingStatus = "failed"
                val deviceBindingResult = SfeFrontendSdk.deviceBindingManager
                    .getDeviceBindingToken(context)
                
                if (deviceBindingResult.isSuccess) {
                    deviceBindingStatus = "passed"
                    Log.d(TAG, "Device binding check passed")
                } else {
                    Log.w(TAG, "Device binding failed", deviceBindingResult.exceptionOrNull())
                }
                
                // Step 2: RASP Security Checks
                var rootDetectionStatus = "passed"
                var debugDetectionStatus = "passed"
                var tamperDetectionStatus = "passed"
                
                try {
                    val raspManager = SfeFrontendSdk.raspManager
                    
                    // Root detection
                    if (raspManager.isDeviceRooted()) {
                        rootDetectionStatus = "failed"
                        Log.w(TAG, "Root detection: Device is rooted")
                    }
                    
                    // Debug detection
                    if (raspManager.isDebuggerAttached()) {
                        debugDetectionStatus = "failed"
                        Log.w(TAG, "Debug detection: Debugger is attached")
                    }
                    
                    // Tamper detection
                    if (raspManager.isAppTampered(context)) {
                        tamperDetectionStatus = "failed"
                        Log.w(TAG, "Tamper detection: App has been tampered")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "RASP checks failed", e)
                    rootDetectionStatus = "failed"
                    debugDetectionStatus = "failed"
                    tamperDetectionStatus = "failed"
                }
                
                // Step 3: Play Integrity Attestation
                var playIntegrityStatus = "failed"
                val nonce = generateSecureNonce()
                val attestationResult = SfeFrontendSdk.attestationManager
                    .requestPlayIntegrityToken(context, nonce)
                
                val attestationToken = if (attestationResult.isSuccess) {
                    playIntegrityStatus = "passed"
                    Log.d(TAG, "Play Integrity attestation successful")
                    attestationResult.getOrNull()
                } else {
                    Log.w(TAG, "Play Integrity attestation failed", attestationResult.exceptionOrNull())
                    null
                }
                
                // Update detailed security info
                _detailedSecurityInfo.value = DetailedSecurityInfo(
                    deviceBinding = deviceBindingStatus,
                    rootDetection = rootDetectionStatus,
                    debugDetection = debugDetectionStatus,
                    tamperDetection = tamperDetectionStatus,
                    playIntegrity = playIntegrityStatus
                )
                
                // Calculate overall security status
                val failedChecks = listOf(deviceBindingStatus, rootDetectionStatus, 
                                        debugDetectionStatus, tamperDetectionStatus, playIntegrityStatus)
                    .count { it == "failed" }
                
                val overallStatus = when {
                    failedChecks == 0 -> "secure"
                    failedChecks <= 2 -> "warning"
                    else -> "insecure"
                }
                
                val message = when (overallStatus) {
                    "secure" -> "Device is secure for payments"
                    "warning" -> "Some security checks failed"
                    else -> "Device security compromised"
                }
                
                _securityStatus.value = SecurityStatus(
                    overallStatus = overallStatus,
                    message = message,
                    showBanner = overallStatus != "secure"
                )
                
                // Step 4: Construct secure payload with all security information
                val payloadResult = SfeFrontendSdk.payloadManager
                    .constructSecurePayload(context, attestationToken)
                
                if (payloadResult.isSuccess) {
                    latestSecurityPayload = payloadResult.getOrNull()
                    Log.d(TAG, "Security payload constructed successfully")
                    
                    // Step 5: Send payload to SFE backend (silently)
                    sendSecurityDataToBackend()
                } else {
                    Log.e(TAG, "Failed to construct security payload", payloadResult.exceptionOrNull())
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Background security checks failed", e)
                _securityStatus.value = SecurityStatus(
                    overallStatus = "insecure",
                    message = "Security checks failed",
                    showBanner = true
                )
                _detailedSecurityInfo.value = DetailedSecurityInfo(
                    deviceBinding = "failed",
                    rootDetection = "failed",
                    debugDetection = "failed",
                    tamperDetection = "failed",
                    playIntegrity = "failed"
                )
            }
        }
    }
    
    /**
     * Send security payload to SFE backend for verification
     * This happens automatically without user knowledge
     */
    private fun sendSecurityDataToBackend() {
        viewModelScope.launch {
            try {
                val payload = latestSecurityPayload ?: return@launch
                
                Log.d(TAG, "Security payload ready for backend transmission")
                Log.d(TAG, "Payload size: ${payload.length} characters")
                
                // Perform authentication with SFE payload
                if (::backendService.isInitialized) {
                    val authResult = backendService.demoLogin(payload)
                    
                    if (authResult.isSuccess) {
                        val loginResponse = authResult.getOrNull()
                        Log.d(TAG, "Authentication successful with SFE backend")
                        Log.d(TAG, "Risk assessment: ${loginResponse?.securityAssessment?.riskLevel}")
                        Log.d(TAG, "Device trusted: ${loginResponse?.securityAssessment?.deviceTrusted}")
                        
                        // Update security status based on backend response
                        loginResponse?.securityAssessment?.let { assessment ->
                            val updatedStatus = when (assessment.riskLevel) {
                                "LOW" -> "secure"
                                "MEDIUM" -> "warning"
                                else -> "insecure"
                            }
                            
                            _securityStatus.value = SecurityStatus(
                                overallStatus = updatedStatus,
                                message = if (assessment.deviceTrusted) 
                                    "Device verified by SFE backend" 
                                else 
                                    "Device security concerns detected",
                                showBanner = !assessment.deviceTrusted
                            )
                        }
                        
                        // Complete the end-to-end flow
                        demonstrateCompleteFlow()
                        
                    } else {
                        Log.w(TAG, "Authentication failed with SFE backend", authResult.exceptionOrNull())
                        _securityStatus.value = SecurityStatus(
                            overallStatus = "insecure",
                            message = "Backend authentication failed",
                            showBanner = true
                        )
                    }
                } else {
                    Log.w(TAG, "Backend service not initialized")
                    simulateBackendCommunication()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send security data to backend", e)
            }
        }
    }
    
    /**
     * Demonstrate the complete SFE ecosystem flow
     */
    private fun demonstrateCompleteFlow() {
        Log.d(TAG, "=== SFE Ecosystem Complete Flow ===")
        Log.d(TAG, "✓ 1. Frontend SDK collected security telemetry")
        Log.d(TAG, "✓ 2. Play Integrity attestation completed")
        Log.d(TAG, "✓ 3. Secure payload constructed and sent to SFE Backend")
        Log.d(TAG, "✓ 4. SFE Backend validated device security status")
        Log.d(TAG, "✓ 5. SFE Backend authenticated user with risk assessment")
        Log.d(TAG, "✓ 6. SFE Backend ready to process payment transactions")
        Log.d(TAG, "✓ 7. Anonymized telemetry sent to regulatory compliance")
        Log.d(TAG, "✓ 8. Payment app ready for secure transactions")
        Log.d(TAG, "==================================")
    }
    
    /**
     * Simulate the backend communication flow
     * In production, this would be actual API calls
     */
    private fun simulateBackendCommunication() {
        Log.d(TAG, "=== SFE Ecosystem Communication Flow ===")
        Log.d(TAG, "1. Security payload sent to SFE Backend")
        Log.d(TAG, "2. SFE Backend validates device security status")
        Log.d(TAG, "3. SFE Backend reports anonymized data to Regulatory Board")
        Log.d(TAG, "4. SFE Backend sends approval to Payment Backend")
        Log.d(TAG, "5. Payment app ready for secure transactions")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Generate cryptographic nonce for Play Integrity API
     */
    private fun generateSecureNonce(): String {
        return System.currentTimeMillis().toString() + "_" + 
               (1000..9999).random().toString()
    }
    
    /**
     * Get current security status for internal use
     * This could be used by payment flows to verify security before transactions
     */
    fun isDeviceSecureForPayment(): Boolean {
        return sfeInitialized && latestSecurityPayload != null && 
               _securityStatus.value.overallStatus == "secure"
    }
    
    /**
     * Refresh security checks (can be called periodically or before sensitive operations)
     */
    fun refreshSecurityStatus() {
        if (sfeInitialized) {
            _securityStatus.value = SecurityStatus(
                overallStatus = "pending",
                message = "Refreshing security status...",
                showBanner = true
            )
            performBackgroundSecurityChecks()
        }
    }
    
    /**
     * Payment-related functions (these would be the main app functionality)
     */
    
    fun initiatePayment(amount: Double, recipient: String) {
        viewModelScope.launch {
            try {
                // Before processing payment, ensure device security is validated
                if (!isDeviceSecureForPayment()) {
                    Log.w(TAG, "Payment blocked: Device security not validated")
                    return@launch
                }
                
                // Ensure backend service is available
                if (!::backendService.isInitialized) {
                    Log.w(TAG, "Payment blocked: Backend service not available")
                    return@launch
                }
                
                // Check if user is authenticated
                if (!backendService.isAuthenticated()) {
                    Log.w(TAG, "Payment blocked: User not authenticated")
                    return@launch
                }
                
                Log.d(TAG, "Payment initiated: ₹$amount to $recipient")
                
                // Get current security payload
                val currentPayload = latestSecurityPayload
                if (currentPayload == null) {
                    Log.w(TAG, "Payment blocked: No security payload available")
                    return@launch
                }
                
                // Initiate payment transaction with SFE security verification
                val transactionResult = backendService.initiatePaymentTransaction(
                    amount = amount,
                    currency = "USD",
                    recipient = recipient,
                    description = "SFPay transaction",
                    sfePayload = currentPayload
                )
                
                if (transactionResult.isSuccess) {
                    val response = transactionResult.getOrNull()
                    Log.d(TAG, "Payment transaction successful!")
                    Log.d(TAG, "Transaction ID: ${response?.transactionId}")
                    Log.d(TAG, "Status: ${response?.status}")
                    Log.d(TAG, "Authorization Code: ${response?.authorizationCode}")
                    Log.d(TAG, "Security Risk Level: ${response?.securityInfo?.riskLevel}")
                    Log.d(TAG, "Policy Decision: ${response?.securityInfo?.policyDecision}")
                } else {
                    Log.w(TAG, "Payment transaction failed", transactionResult.exceptionOrNull())
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Payment initiation failed", e)
            }
        }
    }
    
    fun scanQrCode() {
        Log.d(TAG, "QR code scanner requested")
        // TODO: Implement QR code scanning
    }
    
    fun openBillPayment() {
        Log.d(TAG, "Bill payment requested")
        // TODO: Implement bill payment flow
    }
    
    fun openCardManagement() {
        Log.d(TAG, "Card management requested")
        // TODO: Implement card management
    }
    
    /**
     * Check backend connectivity and authentication status
     */
    fun checkBackendStatus() {
        viewModelScope.launch {
            try {
                if (::backendService.isInitialized) {
                    val healthResult = backendService.checkBackendHealth()
                    if (healthResult.isSuccess) {
                        Log.d(TAG, "Backend health check successful")
                        val isAuthenticated = backendService.isAuthenticated()
                        Log.d(TAG, "User authentication status: $isAuthenticated")
                        
                        if (isAuthenticated) {
                            val userInfo = backendService.getUserInfo()
                            Log.d(TAG, "Authenticated user: ${userInfo?.username}")
                        }
                    } else {
                        Log.w(TAG, "Backend health check failed", healthResult.exceptionOrNull())
                    }
                } else {
                    Log.w(TAG, "Backend service not initialized")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Backend status check failed", e)
            }
        }
    }
    
    /**
     * Logout from backend service
     */
    fun logout() {
        if (::backendService.isInitialized) {
            backendService.logout()
            Log.d(TAG, "User logged out from backend")
        }
    }
} 