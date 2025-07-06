package com.sfe.backend.services

import com.sfe.backend.models.*
import com.sfe.backend.sdk.SFEConfiguration
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class AuthService(private val config: SFEConfiguration) {
    
    private val otpStore = ConcurrentHashMap<String, OTPRecord>()
    private val authTokens = ConcurrentHashMap<String, AuthToken>()
    
    fun generateOTP(phoneNumber: String): String {
        val otp = (100000..999999).random().toString()
        val otpRecord = OTPRecord(
            phoneNumber = phoneNumber,
            otp = otp,
            generatedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(300) // 5 minutes
        )
        otpStore[phoneNumber] = otpRecord
        return otp
    }
    
    fun verifyOTP(phoneNumber: String, otp: String): Boolean {
        val otpRecord = otpStore[phoneNumber] ?: return false
        
        if (otpRecord.expiresAt.isBefore(Instant.now())) {
            otpStore.remove(phoneNumber)
            return false
        }
        
        if (otpRecord.otp == otp) {
            otpStore.remove(phoneNumber)
            return true
        }
        
        return false
    }
    
    fun requireStepUpAuth(userId: String, stepUpType: StepUpType): AuthService {
        // Trigger step-up authentication
        when (stepUpType) {
            StepUpType.OTP -> {
                // Send OTP to user
            }
            StepUpType.BIOMETRIC -> {
                // Request biometric verification
            }
            else -> {
                // Handle other step-up types
            }
        }
        return this
    }
    
    fun generateAuthToken(userId: String): String {
        val token = "AUTH_${System.currentTimeMillis()}_${Random.nextInt(10000)}"
        val authToken = AuthToken(
            token = token,
            userId = userId,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(3600) // 1 hour
        )
        authTokens[token] = authToken
        return token
    }
    
    fun validateAuthToken(token: String): Boolean {
        val authToken = authTokens[token] ?: return false
        return authToken.expiresAt.isAfter(Instant.now())
    }
}

data class OTPRecord(
    val phoneNumber: String,
    val otp: String,
    val generatedAt: Instant,
    val expiresAt: Instant
)

data class AuthToken(
    val token: String,
    val userId: String,
    val createdAt: Instant,
    val expiresAt: Instant
)