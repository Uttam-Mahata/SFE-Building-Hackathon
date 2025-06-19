package com.sfe.paymentbackend.auth.dto

// A simple representation of an authenticated user.
// This could be expanded to include roles, permissions, etc.,
// and might implement Spring Security's UserDetails if using Spring Security.
data class UserPrincipal(
    val userId: String,
    val email: String,
    val authorities: List<String> = emptyList() // Example: "ROLE_USER"
)
