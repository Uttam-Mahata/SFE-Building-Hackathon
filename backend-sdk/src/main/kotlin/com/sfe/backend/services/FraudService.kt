package com.sfe.backend.services

import com.sfe.backend.models.*
import com.sfe.backend.sdk.SFEConfiguration
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

/**
 * Fraud detection service for risk assessment and prevention
 */
@Service
class FraudService(private val config: SFEConfiguration) {
    
    private val userTransactionHistory = ConcurrentHashMap<String, MutableList<TransactionRecord>>()
    private val deviceHistory = ConcurrentHashMap<String, MutableList<DeviceActivity>>()
    private val blacklistedUsers = ConcurrentHashMap<String, BlacklistEntry>()
    private val velocityLimits = ConcurrentHashMap<String, VelocityLimitConfig>()
    
    init {
        // Initialize default velocity limits
        setupDefaultVelocityLimits()
    }
    
    /**
     * Analyze transaction for fraud risk
     */
    fun analyzeTransaction(request: PaymentRequest): RiskAssessment {
        val riskFactors = mutableListOf<String>()
        var riskScore = 0.0
        
        // Check if user is blacklisted
        if (isUserBlacklisted(request.userId)) {
            return RiskAssessment(
                riskLevel = RiskLevel.CRITICAL,
                riskScore = 1.0,
                reason = "User is blacklisted",
                recommendedAction = "BLOCK_TRANSACTION",
                fraudIndicators = listOf("BLACKLISTED_USER")
            )
        }
        
        // Amount-based risk assessment
        riskScore += assessAmountRisk(request.amount, riskFactors)
        
        // Velocity-based risk assessment
        riskScore += assessVelocityRisk(request.userId, request.amount, riskFactors)
        
        // Device-based risk assessment
        request.deviceInfo?.let { deviceInfo ->
            riskScore += assessDeviceRisk(request.userId, deviceInfo, riskFactors)
        }
        
        // Time-based risk assessment
        riskScore += assessTimeRisk(request.createdAt, riskFactors)
        
        // Location-based risk assessment
        request.deviceInfo?.location?.let { location ->
            riskScore += assessLocationRisk(request.userId, location, riskFactors)
        }
        
        // Behavioral risk assessment
        riskScore += assessBehavioralRisk(request.userId, request, riskFactors)
        
        // Determine risk level
        val riskLevel = when {
            riskScore >= 0.8 -> RiskLevel.CRITICAL
            riskScore >= 0.6 -> RiskLevel.HIGH
            riskScore >= 0.4 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        
        val recommendedAction = when (riskLevel) {
            RiskLevel.CRITICAL -> "BLOCK_TRANSACTION"
            RiskLevel.HIGH -> "REQUIRE_ADDITIONAL_AUTH"
            RiskLevel.MEDIUM -> "MONITOR_TRANSACTION"
            RiskLevel.LOW -> "ALLOW_TRANSACTION"
        }
        
        return RiskAssessment(
            riskLevel = riskLevel,
            riskScore = riskScore,
            reason = generateRiskReason(riskFactors),
            recommendedAction = recommendedAction,
            fraudIndicators = riskFactors
        )
    }
    
    /**
     * Check velocity limits for user
     */
    fun checkVelocityLimits(userId: String): RiskAssessment {
        val userHistory = userTransactionHistory[userId] ?: emptyList()
        val now = Instant.now()
        
        // Check daily velocity
        val todayTransactions = userHistory.filter { 
            ChronoUnit.DAYS.between(it.createdAt, now) == 0L 
        }
        val todayAmount = todayTransactions.sumOf { it.amount }
        val dailyLimit = getDailyVelocityLimit(userId)
        
        if (todayAmount > dailyLimit) {
            return RiskAssessment(
                riskLevel = RiskLevel.HIGH,
                riskScore = 0.8,
                reason = "Daily transaction limit exceeded",
                recommendedAction = "BLOCK_TRANSACTION",
                fraudIndicators = listOf("DAILY_LIMIT_EXCEEDED")
            )
        }
        
        // Check hourly velocity
        val hourlyTransactions = userHistory.filter { 
            ChronoUnit.HOURS.between(it.createdAt, now) <= 1
        }
        
        if (hourlyTransactions.size > 10) {
            return RiskAssessment(
                riskLevel = RiskLevel.HIGH,
                riskScore = 0.7,
                reason = "Too many transactions in short time",
                recommendedAction = "REQUIRE_ADDITIONAL_AUTH",
                fraudIndicators = listOf("HIGH_FREQUENCY_TRANSACTIONS")
            )
        }
        
        return RiskAssessment(
            riskLevel = RiskLevel.LOW,
            riskScore = 0.1,
            reason = "Velocity checks passed",
            recommendedAction = "ALLOW_TRANSACTION",
            fraudIndicators = emptyList()
        )
    }
    
    /**
     * Validate device fingerprint
     */
    fun validateDeviceFingerprint(deviceId: String): RiskAssessment {
        val deviceActivity = deviceHistory[deviceId] ?: emptyList()
        
        // Check if device is new
        if (deviceActivity.isEmpty()) {
            return RiskAssessment(
                riskLevel = RiskLevel.MEDIUM,
                riskScore = 0.5,
                reason = "New device detected",
                recommendedAction = "REQUIRE_ADDITIONAL_AUTH",
                fraudIndicators = listOf("NEW_DEVICE")
            )
        }
        
        // Check for suspicious device behavior
        val recentActivity = deviceActivity.filter { 
            ChronoUnit.HOURS.between(it.timestamp, Instant.now()) <= 24 
        }
        
        val uniqueUsers = recentActivity.map { it.userId }.toSet()
        if (uniqueUsers.size > 5) {
            return RiskAssessment(
                riskLevel = RiskLevel.HIGH,
                riskScore = 0.9,
                reason = "Device used by multiple users",
                recommendedAction = "BLOCK_TRANSACTION",
                fraudIndicators = listOf("SHARED_DEVICE_SUSPICIOUS")
            )
        }
        
        return RiskAssessment(
            riskLevel = RiskLevel.LOW,
            riskScore = 0.1,
            reason = "Device fingerprint validated",
            recommendedAction = "ALLOW_TRANSACTION",
            fraudIndicators = emptyList()
        )
    }
    
    /**
     * Analyze user behavior patterns
     */
    fun analyzeUserBehavior(userId: String, request: PaymentRequest): RiskAssessment {
        val userHistory = userTransactionHistory[userId] ?: emptyList()
        val riskFactors = mutableListOf<String>()
        var riskScore = 0.0
        
        // Check transaction patterns
        val avgAmount = userHistory.map { it.amount.toDouble() }.average()
        if (request.amount.toDouble() > avgAmount * 10) {
            riskScore += 0.3
            riskFactors.add("AMOUNT_DEVIATION")
        }
        
        // Check time patterns
        val userTransactionHours = userHistory.map { it.createdAt.atZone(java.time.ZoneOffset.UTC).hour }
        val currentHour = request.createdAt.atZone(java.time.ZoneOffset.UTC).hour
        if (userTransactionHours.isNotEmpty() && currentHour !in userTransactionHours) {
            riskScore += 0.2
            riskFactors.add("UNUSUAL_TIME")
        }
        
        // Check recipient patterns
        val commonRecipients = userHistory.map { it.recipientDetails.name }.groupingBy { it }.eachCount()
        if (request.recipientDetails.name !in commonRecipients.keys) {
            riskScore += 0.1
            riskFactors.add("NEW_RECIPIENT")
        }
        
        val riskLevel = when {
            riskScore >= 0.6 -> RiskLevel.HIGH
            riskScore >= 0.3 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        
        return RiskAssessment(
            riskLevel = riskLevel,
            riskScore = riskScore,
            reason = "Behavioral analysis completed",
            recommendedAction = if (riskLevel == RiskLevel.HIGH) "REQUIRE_ADDITIONAL_AUTH" else "ALLOW_TRANSACTION",
            fraudIndicators = riskFactors
        )
    }
    
    /**
     * Add user to blacklist
     */
    fun blacklistUser(userId: String, reason: String) {
        blacklistedUsers[userId] = BlacklistEntry(
            userId = userId,
            reason = reason,
            timestamp = Instant.now()
        )
    }
    
    /**
     * Remove user from blacklist
     */
    fun removeFromBlacklist(userId: String) {
        blacklistedUsers.remove(userId)
    }
    
    /**
     * Record transaction for history tracking
     */
    fun recordTransaction(transaction: TransactionRecord) {
        userTransactionHistory.computeIfAbsent(transaction.userId) { mutableListOf() }
            .add(transaction)
        
        // Keep only last 1000 transactions per user
        userTransactionHistory[transaction.userId]?.let { history ->
            if (history.size > 1000) {
                history.removeAt(0)
            }
        }
    }
    
    /**
     * Record device activity
     */
    fun recordDeviceActivity(userId: String, deviceInfo: DeviceInfo) {
        val activity = DeviceActivity(
            userId = userId,
            deviceId = deviceInfo.deviceId,
            ipAddress = deviceInfo.ipAddress,
            location = deviceInfo.location,
            timestamp = Instant.now()
        )
        
        deviceHistory.computeIfAbsent(deviceInfo.deviceId) { mutableListOf() }
            .add(activity)
    }
    
    // Private helper methods
    
    private fun assessAmountRisk(amount: BigDecimal, riskFactors: MutableList<String>): Double {
        return when {
            amount >= BigDecimal("500000") -> {
                riskFactors.add("HIGH_AMOUNT")
                0.4
            }
            amount >= BigDecimal("100000") -> {
                riskFactors.add("MEDIUM_AMOUNT")
                0.2
            }
            else -> 0.0
        }
    }
    
    private fun assessVelocityRisk(userId: String, amount: BigDecimal, riskFactors: MutableList<String>): Double {
        val userHistory = userTransactionHistory[userId] ?: return 0.0
        val now = Instant.now()
        
        // Check transactions in last hour
        val recentTransactions = userHistory.filter { 
            ChronoUnit.HOURS.between(it.createdAt, now) <= 1 
        }
        
        val recentAmount = recentTransactions.sumOf { it.amount }
        return when {
            recentAmount + amount > BigDecimal("100000") -> {
                riskFactors.add("HIGH_VELOCITY")
                0.3
            }
            recentTransactions.size > 5 -> {
                riskFactors.add("FREQUENT_TRANSACTIONS")
                0.2
            }
            else -> 0.0
        }
    }
    
    private fun assessDeviceRisk(userId: String, deviceInfo: DeviceInfo, riskFactors: MutableList<String>): Double {
        val deviceActivity = deviceHistory[deviceInfo.deviceId] ?: return 0.1
        
        // Check if device is associated with multiple users
        val uniqueUsers = deviceActivity.map { it.userId }.toSet()
        return when {
            uniqueUsers.size > 3 -> {
                riskFactors.add("SHARED_DEVICE")
                0.3
            }
            deviceActivity.isEmpty() -> {
                riskFactors.add("NEW_DEVICE")
                0.2
            }
            else -> 0.0
        }
    }
    
    private fun assessTimeRisk(timestamp: Instant, riskFactors: MutableList<String>): Double {
        val hour = timestamp.atZone(java.time.ZoneOffset.UTC).hour
        
        // Higher risk for transactions during unusual hours (2 AM - 6 AM)
        return if (hour in 2..6) {
            riskFactors.add("UNUSUAL_TIME")
            0.2
        } else {
            0.0
        }
    }
    
    private fun assessLocationRisk(userId: String, location: Location, riskFactors: MutableList<String>): Double {
        // Simple location-based risk assessment
        // In a real implementation, this would use ML models and location history
        return 0.0
    }
    
    private fun assessBehavioralRisk(userId: String, request: PaymentRequest, riskFactors: MutableList<String>): Double {
        val userHistory = userTransactionHistory[userId] ?: return 0.0
        
        if (userHistory.isEmpty()) {
            riskFactors.add("NEW_USER")
            return 0.1
        }
        
        // Check for unusual patterns
        val avgAmount = userHistory.map { it.amount.toDouble() }.average()
        return if (request.amount.toDouble() > avgAmount * 5) {
            riskFactors.add("AMOUNT_ANOMALY")
            0.2
        } else {
            0.0
        }
    }
    
    private fun generateRiskReason(riskFactors: List<String>): String {
        return when {
            riskFactors.isEmpty() -> "No risk factors detected"
            riskFactors.size == 1 -> "Risk factor: ${riskFactors[0]}"
            else -> "Multiple risk factors: ${riskFactors.joinToString(", ")}"
        }
    }
    
    private fun isUserBlacklisted(userId: String): Boolean {
        return blacklistedUsers.containsKey(userId)
    }
    
    private fun getDailyVelocityLimit(userId: String): BigDecimal {
        return velocityLimits[userId]?.dailyLimit ?: BigDecimal("500000")
    }
    
    private fun setupDefaultVelocityLimits() {
        // Default velocity limits for different user types
        velocityLimits["default"] = VelocityLimitConfig(
            dailyLimit = BigDecimal("500000"),
            hourlyLimit = BigDecimal("100000"),
            transactionLimit = BigDecimal("50000")
        )
    }
}

/**
 * Device activity tracking
 */
data class DeviceActivity(
    val userId: String,
    val deviceId: String,
    val ipAddress: String,
    val location: Location?,
    val timestamp: Instant
)

/**
 * Blacklist entry
 */
data class BlacklistEntry(
    val userId: String,
    val reason: String,
    val timestamp: Instant
)

/**
 * Velocity limit configuration
 */
data class VelocityLimitConfig(
    val dailyLimit: BigDecimal,
    val hourlyLimit: BigDecimal,
    val transactionLimit: BigDecimal
)