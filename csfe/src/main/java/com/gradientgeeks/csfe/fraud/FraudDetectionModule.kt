package com.gradientgeeks.csfe.fraud

import android.content.Context
import android.provider.Settings
import com.gradientgeeks.csfe.config.FraudDetectionLevel
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.payment.PaymentRequest
import com.gradientgeeks.csfe.utils.Logger
import java.util.Date

/**
 * Handles fraud detection and risk analysis for transactions.
 */
class FraudDetectionModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "FraudDetectionModule"
    private val fraudConfig = FraudDetectionConfig()
    
    /**
     * Analyze a transaction for potential fraud.
     * 
     * @param request The payment request to analyze
     * @param callback Callback with the fraud detection result
     */
    fun analyzeTransaction(
        request: PaymentRequest,
        callback: (FraudDetectionResult) -> Unit
    ) {
        Logger.d(TAG, "Analyzing transaction risk for amount: ${request.amount}")
        
        try {
            val analysisRequest = createAnalysisRequest(request)
            val riskScore = calculateRiskScore(analysisRequest)
            val riskLevel = determineRiskLevel(riskScore)
            
            when {
                riskScore >= fraudConfig.criticalRiskThreshold -> {
                    callback(
                        FraudDetectionResult.Blocked(
                            riskScore = riskScore,
                            riskLevel = riskLevel,
                            reason = "Critical risk threshold exceeded",
                            blockReason = BlockReason.HIGH_RISK_SCORE
                        )
                    )
                }
                riskScore >= fraudConfig.riskScoreThreshold -> {
                    callback(
                        FraudDetectionResult.RequiresVerification(
                            riskScore = riskScore,
                            riskLevel = riskLevel,
                            verificationType = VerificationType.BIOMETRIC
                        )
                    )
                }
                else -> {
                    callback(
                        FraudDetectionResult.Allowed(
                            riskScore = riskScore,
                            riskLevel = riskLevel,
                            analysisDetails = "Transaction cleared all risk checks"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error during fraud analysis: ${e.message}")
            callback(
                FraudDetectionResult.Error(
                    errorMessage = "Fraud analysis failed: ${e.message}",
                    errorCode = "ANALYSIS_ERROR"
                )
            )
        }
    }
    
    /**
     * Perform velocity check for transaction limits.
     */
    fun performVelocityCheck(
        userId: String,
        amount: Double,
        timeWindow: Long = 24 * 60 * 60 * 1000 // 24 hours
    ): VelocityCheckResult {
        // In a real implementation, this would check against stored transaction history
        // For demo purposes, simulate velocity check
        val currentTime = System.currentTimeMillis()
        val windowStart = currentTime - timeWindow
        
        // Simulate transaction count and amount for demo
        val transactionCount = (Math.random() * 10).toInt()
        val totalAmount = Math.random() * 100000
        
        val isViolated = when {
            transactionCount >= fraudConfig.maxTransactionCount -> true
            totalAmount + amount > fraudConfig.maxDailyTransactionAmount -> true
            amount > fraudConfig.maxTransactionAmount -> true
            else -> false
        }
        
        val limitType = when {
            transactionCount >= fraudConfig.maxTransactionCount -> VelocityLimitType.TRANSACTION_COUNT
            totalAmount + amount > fraudConfig.maxDailyTransactionAmount -> VelocityLimitType.DAILY_LIMIT
            amount > fraudConfig.maxTransactionAmount -> VelocityLimitType.TRANSACTION_AMOUNT
            else -> VelocityLimitType.HOURLY_LIMIT
        }
        
        return VelocityCheckResult(
            transactionCount = transactionCount,
            totalAmount = totalAmount,
            timeWindow = timeWindow,
            isViolated = isViolated,
            limitType = limitType
        )
    }
    
    /**
     * Check if a device is suspicious.
     */
    fun checkDeviceSecurity(deviceInfo: DeviceInfo): Boolean {
        var suspiciousScore = 0.0
        
        // Check for rooted device
        if (deviceInfo.isRooted) {
            suspiciousScore += 0.4
        }
        
        // Check for emulator
        if (deviceInfo.isEmulator) {
            suspiciousScore += 0.5
        }
        
        // Check for unusual device characteristics
        if (deviceInfo.simCountry == null) {
            suspiciousScore += 0.1
        }
        
        return suspiciousScore >= 0.3
    }
    
    /**
     * Create fraud analysis request from payment request.
     */
    private fun createAnalysisRequest(request: PaymentRequest): FraudAnalysisRequest {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        
        return FraudAnalysisRequest(
            transactionId = "temp_${System.currentTimeMillis()}",
            amount = request.amount,
            recipientId = request.recipientVPA ?: request.recipientMobile ?: "unknown",
            userId = "current_user", // Would come from authentication
            deviceId = deviceId,
            timestamp = Date(),
            transactionType = when {
                request.recipientVPA != null -> TransactionType.UPI
                request.recipientMobile != null -> TransactionType.WALLET
                else -> TransactionType.BANK_TRANSFER
            },
            deviceInfo = getDeviceInfo()
        )
    }
    
    /**
     * Calculate risk score based on various factors.
     */
    private fun calculateRiskScore(request: FraudAnalysisRequest): Double {
        var riskScore = 0.0
        
        // Amount-based risk
        riskScore += when {
            request.amount > 100000 -> 0.4
            request.amount > 50000 -> 0.3
            request.amount > 10000 -> 0.2
            else -> 0.1
        }
        
        // Device-based risk
        request.deviceInfo?.let { deviceInfo ->
            if (checkDeviceSecurity(deviceInfo)) {
                riskScore += 0.3
            }
        }
        
        // Time-based risk (night transactions are slightly riskier)
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hour < 6 || hour > 22) {
            riskScore += 0.1
        }
        
        // Velocity risk
        val velocityCheck = performVelocityCheck(request.userId, request.amount)
        if (velocityCheck.isViolated) {
            riskScore += 0.2
        }
        
        // Apply fraud detection level modifier
        when (config.fraudDetectionLevel) {
            FraudDetectionLevel.HIGH -> riskScore *= 1.2
            FraudDetectionLevel.LOW -> riskScore *= 0.8
            else -> {} // No change for medium
        }
        
        return minOf(riskScore, 1.0) // Cap at 1.0
    }
    
    /**
     * Determine risk level based on risk score.
     */
    private fun determineRiskLevel(riskScore: Double): RiskLevel {
        return when {
            riskScore >= 0.8 -> RiskLevel.CRITICAL
            riskScore >= 0.6 -> RiskLevel.HIGH
            riskScore >= 0.3 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    /**
     * Get device information for fraud analysis.
     */
    private fun getDeviceInfo(): DeviceInfo {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        
        return DeviceInfo(
            deviceId = deviceId,
            deviceModel = android.os.Build.MODEL,
            osVersion = android.os.Build.VERSION.RELEASE,
            appVersion = "1.0.0", // Would come from BuildConfig
            isRooted = false, // Would implement proper root detection
            isEmulator = false, // Would implement proper emulator detection
            screenResolution = "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}",
            simCountry = null // Would get from TelephonyManager
        )
    }
}
