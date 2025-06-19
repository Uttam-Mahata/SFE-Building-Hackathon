package com.sfe.paymentbackend.auth.dto

data class Tokens(
    val accessToken: String,
    val refreshToken: String? = null, // Optional, depending on auth strategy
    val expiresIn: Long // Typically in seconds
)
