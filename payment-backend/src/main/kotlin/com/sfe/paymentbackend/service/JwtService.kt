package com.sfe.paymentbackend.service

import com.sfe.paymentbackend.auth.dto.Tokens
import com.sfe.paymentbackend.auth.dto.UserPrincipal
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
class JwtService {

    // These are placeholders. In a real app, use a secure library like JJWT
    // and proper key management.
    private val mockSecretKey = "your-very-secure-and-long-secret-key-that-is-at-least-256-bits"
    private val accessTokenExpirationMs: Long = 3600000 // 1 hour
    private val refreshTokenExpirationMs: Long = 86400000 * 7 // 7 days

    fun generateTokens(user: UserPrincipal): Tokens {
        val now = System.currentTimeMillis()
        val accessTokenExpiry = Date(now + accessTokenExpirationMs)
        // val refreshTokenExpiry = Date(now + refreshTokenExpirationMs) // If using refresh tokens

        // Simulate JWT creation
        val accessToken = "mockJwt.${UUID.randomUUID()}.${user.userId}.${accessTokenExpiry.time}"
        val refreshToken = "mockRefreshJwt.${UUID.randomUUID()}.${user.userId}" // Optional

        println("Generated tokens for user: ${user.email}")

        return Tokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessTokenExpirationMs / 1000 // Send expiresIn in seconds
        )
    }

    // In a real app, you would have methods to validate tokens, extract claims, etc.
    // fun validateToken(token: String): Boolean { ... }
    // fun getEmailFromToken(token: String): String { ... }
}
