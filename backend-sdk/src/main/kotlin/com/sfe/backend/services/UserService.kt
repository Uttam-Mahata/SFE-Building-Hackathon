package com.sfe.backend.services

import com.sfe.backend.models.*
import com.sfe.backend.sdk.SFEConfiguration
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
class UserService(private val config: SFEConfiguration) {
    
    private val users = ConcurrentHashMap<String, User>()
    private val userLimits = ConcurrentHashMap<String, TransactionLimits>()
    
    fun register(user: User): UserService {
        users[user.id] = user
        return this
    }
    
    fun validateLimitUpdate(userId: String, limits: TransactionLimits): UserService {
        // Validate if limits comply with RBI regulations
        if (limits.dailyLimit.compareTo(java.math.BigDecimal("1000000")) > 0) {
            throw SFEValidationException("Daily limit exceeds RBI regulations")
        }
        return this
    }
    
    fun updateTransactionLimits(userId: String, limits: TransactionLimits): Boolean {
        userLimits[userId] = limits
        return true
    }
    
    fun notifyLimitChange(userId: String): UserService {
        // Send notification to user about limit changes
        return this
    }
    
    fun getUser(userId: String): User? {
        return users[userId]
    }
    
    fun updateUserStatus(userId: String, status: UserStatus): Boolean {
        users[userId]?.let { user ->
            users[userId] = user.copy(status = status, updatedAt = Instant.now())
            return true
        }
        return false
    }
}