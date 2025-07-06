package com.gradientgeeks.csfe.fraud

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.gradientgeeks.csfe.config.FraudDetectionLevel
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.models.DeviceInfo
import com.gradientgeeks.csfe.utils.Logger
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Handles fraud detection and risk assessment for transactions.
 */
class FraudDetectionModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "FraudDetectionModule"
    
    // Transaction history for pattern analysis
    private val transactionHistory = ConcurrentHashMap<String, MutableList<TransactionRiskData>>()
    
    // Device behavior patterns
    private val deviceBehavior = ConcurrentHashMap<String, DeviceBehaviorData>()
    
    // Velocity limits based on detection level
    private val velocityLimits = mapOf(
        FraudDetectionLevel.LOW to VelocityLimits(
            maxTransactionsPerHour = 50,
            maxAmountPerHour = 50000.0,
            maxAmountPerDay = 200000.0
        ),
        FraudDetectionLevel.MEDIUM to VelocityLimits(
            maxTransactionsPerHour = 30,
            maxAmountPerHour = 25000.0,
            maxAmountPerDay = 100000.0
        ),
        FraudDetectionLevel.HIGH to VelocityLimits(
            maxTransactionsPerHour = 20,
            maxAmountPerHour = 15000.0,
            maxAmountPerDay = 50000.0
        )
    )
    
    /**
     * Analyze transaction for fraud risk.
     */
    fun analyzeTransaction(
        userId: String,
        amount: Double,
        recipientId: String,
        deviceInfo: DeviceInfo,
        location: Location? = null,
        callback: (FraudAnalysisResult) -> Unit
    ) {
        Logger.d(TAG, "Analyzing transaction for fraud risk: $amount to $recipientId")
        
        try {
            val riskFactors = mutableListOf<RiskFactor>()
            var riskScore = 0.0
            
            // Check velocity limits
            val velocityRisk = checkVelocityLimits(userId, amount)
            if (velocityRisk.isRisky) {
                riskFactors.add(RiskFactor.VELOCITY_EXCEEDED)
                riskScore += velocityRisk.riskScore
            }
            
            // Check device behavior
            val deviceRisk = analyzeDeviceBehavior(userId, deviceInfo)
            if (deviceRisk.isRisky) {
                riskFactors.add(RiskFactor.UNUSUAL_DEVICE_BEHAVIOR)
                riskScore += deviceRisk.riskScore
            }
            
            // Check location anomalies
            location?.let { loc ->
                val locationRisk = analyzeLocationRisk(userId, loc)
                if (locationRisk.isRisky) {
                    riskFactors.add(RiskFactor.LOCATION_ANOMALY)
                    riskScore += locationRisk.riskScore
                }
            }
            
            // Check transaction patterns
            val patternRisk = analyzeTransactionPatterns(userId, amount, recipientId)
            if (patternRisk.isRisky) {
                riskFactors.add(RiskFactor.UNUSUAL_TRANSACTION_PATTERN)
                riskScore += patternRisk.riskScore
            }
            
            // Check for suspicious amounts
            val amountRisk = analyzeAmountRisk(userId, amount)
            if (amountRisk.isRisky) {
                riskFactors.add(RiskFactor.SUSPICIOUS_AMOUNT)
                riskScore += amountRisk.riskScore
            }
            
            // Check time-based patterns
            val timeRisk = analyzeTimePatterns(userId)
            if (timeRisk.isRisky) {
                riskFactors.add(RiskFactor.UNUSUAL_TIME_PATTERN)
                riskScore += timeRisk.riskScore
            }
            
            // Determine overall risk level
            val riskLevel = determineRiskLevel(riskScore, riskFactors.size)
            
            // Store transaction data for future analysis
            storeTransactionData(userId, amount, recipientId, riskScore, riskLevel)
            
            Logger.d(TAG, "Fraud analysis completed. Risk score: $riskScore, Level: $riskLevel")
            
            callback(FraudAnalysisResult(
                riskScore = riskScore,
                riskLevel = riskLevel,
                riskFactors = riskFactors,
                isBlocked = shouldBlockTransaction(riskLevel, riskScore),
                requiresAdditionalAuth = requiresAdditionalAuth(riskLevel, riskScore),
                recommendation = generateRecommendation(riskLevel, riskFactors)
            ))
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error analyzing transaction: ${e.message}")
            callback(FraudAnalysisResult(
                riskScore = 0.0,
                riskLevel = FraudRiskLevel.LOW,
                riskFactors = emptyList(),
                isBlocked = false,
                requiresAdditionalAuth = false,
                recommendation = "Analysis failed, proceed with caution"
            ))
        }
    }
    
    /**
     * Check if a user or device is blacklisted.
     */
    fun checkBlacklist(
        userId: String,
        deviceId: String,
        callback: (BlacklistResult) -> Unit
    ) {
        Logger.d(TAG, "Checking blacklist for user: $userId, device: $deviceId")
        
        // In a real implementation, this would check against a backend blacklist
        // For demo purposes, simulate blacklist check
        val isUserBlacklisted = userId.contains("blacklisted") || userId.contains("blocked")
        val isDeviceBlacklisted = deviceId.contains("compromised") || deviceId.contains("stolen")
        
        if (isUserBlacklisted || isDeviceBlacklisted) {
            Logger.w(TAG, "Blacklist hit detected")
            callback(BlacklistResult.Blocked(
                reason = when {
                    isUserBlacklisted -> "User is blacklisted"
                    isDeviceBlacklisted -> "Device is blacklisted"
                    else -> "Unknown blacklist reason"
                }
            ))
        } else {
            Logger.d(TAG, "No blacklist hits found")
            callback(BlacklistResult.Clear)
        }
    }
    
    /**
     * Report suspicious activity.
     */
    fun reportSuspiciousActivity(
        userId: String,
        activityType: SuspiciousActivityType,
        details: String,
        callback: (Boolean) -> Unit
    ) {
        Logger.d(TAG, "Reporting suspicious activity: $activityType for user: $userId")
        
        try {
            val report = SuspiciousActivityReport(
                userId = userId,
                activityType = activityType,
                details = details,
                timestamp = Date(),
                deviceInfo = getCurrentDeviceInfo()
            )
            
            // In a real implementation, this would send to backend fraud team
            // For demo, just log the report
            Logger.w(TAG, "Suspicious activity reported: $report")
            
            callback(true)
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error reporting suspicious activity: ${e.message}")
            callback(false)
        }
    }
    
    /**
     * Get user's fraud risk profile.
     */
    fun getUserRiskProfile(
        userId: String,
        callback: (UserRiskProfile) -> Unit
    ) {
        Logger.d(TAG, "Getting risk profile for user: $userId")
        
        try {
            val userTransactions = transactionHistory[userId] ?: emptyList()
            val deviceBehaviorData = deviceBehavior[userId]
            
            val totalTransactions = userTransactions.size
            val avgRiskScore = if (totalTransactions > 0) {
                userTransactions.map { it.riskScore }.average()
            } else {
                0.0
            }
            
            val highRiskTransactions = userTransactions.count { it.riskLevel == FraudRiskLevel.HIGH }
            val recentSuspiciousActivity = userTransactions.filter { 
                it.timestamp.after(Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)) // Last 7 days
            }.count { it.riskLevel == FraudRiskLevel.HIGH }
            
            val overallRiskLevel = when {
                avgRiskScore > 7.0 || recentSuspiciousActivity > 3 -> FraudRiskLevel.HIGH
                avgRiskScore > 4.0 || recentSuspiciousActivity > 1 -> FraudRiskLevel.MEDIUM
                else -> FraudRiskLevel.LOW
            }
            
            val profile = UserRiskProfile(
                userId = userId,
                overallRiskLevel = overallRiskLevel,
                averageRiskScore = avgRiskScore,
                totalTransactions = totalTransactions,
                highRiskTransactions = highRiskTransactions,
                recentSuspiciousActivity = recentSuspiciousActivity,
                lastUpdated = Date()
            )
            
            Logger.d(TAG, "Risk profile generated: $overallRiskLevel (score: $avgRiskScore)")
            callback(profile)
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting risk profile: ${e.message}")
            callback(UserRiskProfile(
                userId = userId,
                overallRiskLevel = FraudRiskLevel.LOW,
                averageRiskScore = 0.0,
                totalTransactions = 0,
                highRiskTransactions = 0,
                recentSuspiciousActivity = 0,
                lastUpdated = Date()
            ))
        }
    }
    
    private fun checkVelocityLimits(userId: String, amount: Double): RiskCheckResult {
        val limits = velocityLimits[config.fraudDetectionLevel] ?: velocityLimits[FraudDetectionLevel.MEDIUM]!!
        val userTransactions = transactionHistory[userId] ?: emptyList()
        
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        
        val recentTransactions = userTransactions.filter { it.timestamp.time > oneHourAgo }
        val dailyTransactions = userTransactions.filter { it.timestamp.time > oneDayAgo }
        
        val hourlyCount = recentTransactions.size
        val hourlyAmount = recentTransactions.sumOf { it.amount }
        val dailyAmount = dailyTransactions.sumOf { it.amount }
        
        val isRisky = hourlyCount >= limits.maxTransactionsPerHour ||
                     hourlyAmount >= limits.maxAmountPerHour ||
                     dailyAmount >= limits.maxAmountPerDay
        
        val riskScore = when {
            hourlyCount >= limits.maxTransactionsPerHour -> 3.0
            hourlyAmount >= limits.maxAmountPerHour -> 2.5
            dailyAmount >= limits.maxAmountPerDay -> 2.0
            else -> 0.0
        }
        
        return RiskCheckResult(isRisky, riskScore)
    }
    
    private fun analyzeDeviceBehavior(userId: String, deviceInfo: DeviceInfo): RiskCheckResult {
        val behaviorData = deviceBehavior[userId]
        
        if (behaviorData == null) {
            // First time seeing this device for this user
            deviceBehavior[userId] = DeviceBehaviorData(
                deviceId = deviceInfo.deviceId,
                firstSeen = Date(),
                lastSeen = Date(),
                transactionCount = 0,
                isRooted = deviceInfo.isRooted
            )
            return RiskCheckResult(false, 0.0)
        }
        
        // Check for device changes
        var riskScore = 0.0
        var isRisky = false
        
        if (behaviorData.deviceId != deviceInfo.deviceId) {
            // Different device
            riskScore += 2.0
            isRisky = true
        }
        
        if (deviceInfo.isRooted && !behaviorData.isRooted) {
            // Device became rooted
            riskScore += 3.0
            isRisky = true
        }
        
        // Update behavior data
        behaviorData.lastSeen = Date()
        behaviorData.transactionCount++
        
        return RiskCheckResult(isRisky, riskScore)
    }
    
    private fun analyzeLocationRisk(userId: String, location: Location): RiskCheckResult {
        // In a real implementation, this would compare with user's typical locations
        // For demo, simulate location risk analysis
        
        val suspiciousCountries = listOf("XX", "YY", "ZZ") // Placeholder country codes
        val isHighRiskLocation = false // Would be determined by IP/GPS location
        
        return RiskCheckResult(isHighRiskLocation, if (isHighRiskLocation) 2.5 else 0.0)
    }
    
    private fun analyzeTransactionPatterns(userId: String, amount: Double, recipientId: String): RiskCheckResult {
        val userTransactions = transactionHistory[userId] ?: emptyList()
        
        if (userTransactions.isEmpty()) {
            return RiskCheckResult(false, 0.0)
        }
        
        val avgAmount = userTransactions.map { it.amount }.average()
        val maxAmount = userTransactions.maxOfOrNull { it.amount } ?: 0.0
        
        var riskScore = 0.0
        var isRisky = false
        
        // Check for unusual amount patterns
        if (amount > avgAmount * 5) {
            riskScore += 1.5
            isRisky = true
        }
        
        if (amount > maxAmount * 2) {
            riskScore += 2.0
            isRisky = true
        }
        
        // Check for repeated transactions to same recipient
        val recentToSameRecipient = userTransactions.filter { 
            it.recipientId == recipientId && 
            it.timestamp.time > System.currentTimeMillis() - (60 * 60 * 1000) // Last hour
        }
        
        if (recentToSameRecipient.size > 3) {
            riskScore += 1.5
            isRisky = true
        }
        
        return RiskCheckResult(isRisky, riskScore)
    }
    
    private fun analyzeAmountRisk(userId: String, amount: Double): RiskCheckResult {
        val suspiciousAmounts = listOf(
            9999.0, 19999.0, 49999.0, 99999.0, 199999.0 // Common amounts to avoid reporting thresholds
        )
        
        val isRisky = suspiciousAmounts.any { abs(amount - it) < 1.0 }
        val riskScore = if (isRisky) 1.0 else 0.0
        
        return RiskCheckResult(isRisky, riskScore)
    }
    
    private fun analyzeTimePatterns(userId: String): RiskCheckResult {
        val userTransactions = transactionHistory[userId] ?: emptyList()
        
        if (userTransactions.isEmpty()) {
            return RiskCheckResult(false, 0.0)
        }
        
        val currentHour = Date().hours
        val isUnusualHour = currentHour < 6 || currentHour > 23 // Very early morning or late night
        
        if (isUnusualHour) {
            val normalHourTransactions = userTransactions.filter { txn ->
                val hour = txn.timestamp.hours
                hour in 6..23
            }
            
            if (normalHourTransactions.size > userTransactions.size * 0.8) {
                // User typically transacts during normal hours
                return RiskCheckResult(true, 1.0)
            }
        }
        
        return RiskCheckResult(false, 0.0)
    }
    
    private fun determineRiskLevel(riskScore: Double, riskFactorCount: Int): FraudRiskLevel {
        return when {
            riskScore >= 8.0 || riskFactorCount >= 4 -> FraudRiskLevel.HIGH
            riskScore >= 4.0 || riskFactorCount >= 2 -> FraudRiskLevel.MEDIUM
            else -> FraudRiskLevel.LOW
        }
    }
    
    private fun shouldBlockTransaction(riskLevel: FraudRiskLevel, riskScore: Double): Boolean {
        return when (config.fraudDetectionLevel) {
            FraudDetectionLevel.HIGH -> riskLevel == FraudRiskLevel.HIGH
            FraudDetectionLevel.MEDIUM -> riskLevel == FraudRiskLevel.HIGH && riskScore >= 9.0
            FraudDetectionLevel.LOW -> riskScore >= 10.0
        }
    }
    
    private fun requiresAdditionalAuth(riskLevel: FraudRiskLevel, riskScore: Double): Boolean {
        return when (config.fraudDetectionLevel) {
            FraudDetectionLevel.HIGH -> riskLevel >= FraudRiskLevel.MEDIUM
            FraudDetectionLevel.MEDIUM -> riskLevel == FraudRiskLevel.HIGH
            FraudDetectionLevel.LOW -> riskScore >= 8.0
        }
    }
    
    private fun generateRecommendation(riskLevel: FraudRiskLevel, riskFactors: List<RiskFactor>): String {
        return when (riskLevel) {
            FraudRiskLevel.HIGH -> "High risk detected. Consider blocking transaction or requiring additional verification."
            FraudRiskLevel.MEDIUM -> "Medium risk detected. Recommend additional authentication steps."
            FraudRiskLevel.LOW -> "Low risk transaction. Safe to proceed."
        }
    }
    
    private fun storeTransactionData(userId: String, amount: Double, recipientId: String, riskScore: Double, riskLevel: FraudRiskLevel) {
        val transactionData = TransactionRiskData(
            amount = amount,
            recipientId = recipientId,
            timestamp = Date(),
            riskScore = riskScore,
            riskLevel = riskLevel
        )
        
        val userTransactions = transactionHistory.getOrPut(userId) { mutableListOf() }
        userTransactions.add(transactionData)
        
        // Keep only last 100 transactions per user
        if (userTransactions.size > 100) {
            userTransactions.removeAt(0)
        }
    }
    
    private fun getCurrentDeviceInfo(): DeviceInfo {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        
        return DeviceInfo(
            deviceId = androidId ?: "unknown",
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            osVersion = Build.VERSION.RELEASE,
            appVersion = "1.0.0",
            isRooted = false, // Simplified for demo
            hasSecurityPatches = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        )
    }
}

/**
 * Fraud analysis result
 */
data class FraudAnalysisResult(
    val riskScore: Double,
    val riskLevel: FraudRiskLevel,
    val riskFactors: List<RiskFactor>,
    val isBlocked: Boolean,
    val requiresAdditionalAuth: Boolean,
    val recommendation: String
)

/**
 * Fraud risk levels
 */
enum class FraudRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Risk factors that can be detected
 */
enum class RiskFactor {
    VELOCITY_EXCEEDED,
    UNUSUAL_DEVICE_BEHAVIOR,
    LOCATION_ANOMALY,
    UNUSUAL_TRANSACTION_PATTERN,
    SUSPICIOUS_AMOUNT,
    UNUSUAL_TIME_PATTERN
}

/**
 * Blacklist check result
 */
sealed class BlacklistResult {
    object Clear : BlacklistResult()
    data class Blocked(val reason: String) : BlacklistResult()
}

/**
 * Suspicious activity types
 */
enum class SuspiciousActivityType {
    MULTIPLE_FAILED_ATTEMPTS,
    RAPID_TRANSACTIONS,
    UNUSUAL_LOCATION,
    COMPROMISED_DEVICE,
    SOCIAL_ENGINEERING_ATTEMPT
}

/**
 * User risk profile
 */
data class UserRiskProfile(
    val userId: String,
    val overallRiskLevel: FraudRiskLevel,
    val averageRiskScore: Double,
    val totalTransactions: Int,
    val highRiskTransactions: Int,
    val recentSuspiciousActivity: Int,
    val lastUpdated: Date
)

/**
 * Transaction risk data for analysis
 */
data class TransactionRiskData(
    val amount: Double,
    val recipientId: String,
    val timestamp: Date,
    val riskScore: Double,
    val riskLevel: FraudRiskLevel
)

/**
 * Device behavior data
 */
data class DeviceBehaviorData(
    val deviceId: String,
    val firstSeen: Date,
    var lastSeen: Date,
    var transactionCount: Int,
    val isRooted: Boolean
)

/**
 * Velocity limits for fraud detection
 */
data class VelocityLimits(
    val maxTransactionsPerHour: Int,
    val maxAmountPerHour: Double,
    val maxAmountPerDay: Double
)

/**
 * Risk check result
 */
data class RiskCheckResult(
    val isRisky: Boolean,
    val riskScore: Double
)

/**
 * Suspicious activity report
 */
data class SuspiciousActivityReport(
    val userId: String,
    val activityType: SuspiciousActivityType,
    val details: String,
    val timestamp: Date,
    val deviceInfo: DeviceInfo
)
