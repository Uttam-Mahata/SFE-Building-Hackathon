package com.sfe.paymentbackend.service

import com.sfe.paymentbackend.auth.dto.UserPrincipal
import com.sfe.paymentbackend.auth.dto.UserRegistrationRequest
import com.sfe.paymentbackend.persistence.entity.UserEntity
import com.sfe.paymentbackend.persistence.repository.UserRepository
import com.sfe.sdk.backend.KYCResult
import com.sfe.sdk.backend.KYCStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun createUser(request: UserRegistrationRequest, kycResult: KYCResult): UserPrincipal {
        if (kycResult.status != KYCStatus.VERIFIED) {
            throw IllegalStateException("KYC not verified. Status: ${kycResult.status}")
        }

        if (userRepository.findByEmail(request.email.lowercase()) != null) {
            throw IllegalArgumentException("User with email ${request.email} already exists.")
        }

        println("KYC Result for ${request.email}: ${kycResult.kycId}, Status: ${kycResult.status}")

        val hashedPassword = passwordEncoder.encode(request.password)
        val userId = UUID.randomUUID().toString()

        val userEntity = UserEntity(
            id = userId,
            email = request.email.lowercase(),
            passwordHash = hashedPassword,
            fullName = "${request.firstName} ${request.lastName}",
            // phoneNumber = request.phoneNumber, // Assuming UserRegistrationRequest has phoneNumber
            kycStatus = KYCStatus.VERIFIED.name, // Store as string
            riskProfile = "LOW", // Default or from SDK
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        userRepository.save(userEntity)
        println("User created: ${userEntity.email}, ID: ${userEntity.id}")

        return mapToUserPrincipal(userEntity)
    }

    fun authenticateUser(email: String, pass: String): UserPrincipal? {
        val userEntity = userRepository.findByEmail(email.lowercase())
        if (userEntity != null && passwordEncoder.matches(pass, userEntity.passwordHash)) {
            println("Authenticated user: ${userEntity.email}")
            return mapToUserPrincipal(userEntity)
        }
        println("Authentication failed for user: $email")
        return null
    }

    fun findByEmail(email: String): UserPrincipal? {
        return userRepository.findByEmail(email.lowercase())?.let { mapToUserPrincipal(it) }
    }

    fun findUserById(userId: String): UserPrincipal? {
        return userRepository.findById(userId).orElse(null)?.let { mapToUserPrincipal(it) }
    }

    private fun mapToUserPrincipal(userEntity: UserEntity): UserPrincipal {
        return UserPrincipal(
            userId = userEntity.id,
            email = userEntity.email
            // In a real app, map roles/authorities from UserEntity if they exist
            // authorities = listOf("ROLE_USER")
        )
    }
}
