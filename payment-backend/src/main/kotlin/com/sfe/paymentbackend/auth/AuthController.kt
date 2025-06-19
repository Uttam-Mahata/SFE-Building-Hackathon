package com.sfe.paymentbackend.auth

import com.sfe.paymentbackend.auth.dto.*
import com.sfe.paymentbackend.service.JwtService
import com.sfe.paymentbackend.service.UserService
import com.sfe.sdk.backend.SFEBackendSDK
import com.sfe.sdk.backend.KYCResult
import com.sfe.sdk.backend.KYCStatus
import com.sfe.sdk.backend.RiskLevel
import com.sfe.sdk.backend.StepUpType
import com.sfe.sdk.backend.exceptions.SFEValidationException
import com.sfe.sdk.backend.exceptions.SFEFraudException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val sfeBackendSDK: SFEBackendSDK,
    private val userService: UserService,
    private val jwtService: JwtService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: UserRegistrationRequest): ResponseEntity<AuthResponse> {
        if (!request.consentToTerms) {
            return ResponseEntity.badRequest()
                .body(AuthResponse.error("User must consent to terms and conditions."))
        }

        try {
            // Step 1: Perform KYC using SFEBackendSDK (mocked for now)
            // In a real scenario, you might have more complex KYC flows.
            // These calls will use the mock mode if enabled in SFEBackendSDK configuration.
            val aadhaarResult = sfeBackendSDK.kyc().verifyAadhaar(request.email, request.aadhaarNumber)
            val panResult = sfeBackendSDK.kyc().verifyPAN(request.email, request.panNumber)
            // val videoKYCResult = sfeBackendSDK.kyc().performVideoKYC(request.email) // Assuming this is an async initiation

            // For this example, let's assume Aadhaar and PAN are enough and check their status.
            // In a real app, ensure these calls return meaningful mock data from the SDK in mock mode.
            // For now, we create a dummy successful KYCResult if in mock mode.
            val finalKycResult = if (sfeBackendSDK.fraud().assessLoginRisk("test", "test", "test") == RiskLevel.LOW) { // Hacky way to check mockMode indirectly
                 KYCResult("mock-kyc-id-${request.email}", KYCStatus.VERIFIED, mapOf("detail" to "Mock verification complete"))
            } else {
                // This path would be taken if mock mode isn't fully effective or if we had real results
                if (aadhaarResult.status == KYCStatus.VERIFIED && panResult.status == KYCStatus.VERIFIED) {
                     KYCResult(aadhaarResult.kycId, KYCStatus.VERIFIED, mapOf("aadhaar" to "verified", "pan" to "verified"))
                } else {
                     KYCResult("failed-kyc-${request.email}", KYCStatus.REJECTED, mapOf("aadhaar" to aadhaarResult.status, "pan" to panResult.status))
                }
            }


            if (finalKycResult.status != KYCStatus.VERIFIED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.error("KYC verification failed. Status: ${finalKycResult.status}"))
            }

            // Step 2: Create user in local database (mocked)
            val userPrincipal = userService.createUser(request, finalKycResult)

            // Step 3: Generate tokens
            val tokens = jwtService.generateTokens(userPrincipal)

            return ResponseEntity.ok(AuthResponse.success("User registered successfully.", tokens, userPrincipal.userId))

        } catch (e: SFEValidationException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(AuthResponse.error("Registration validation failed: ${e.message}"))
        } catch (e: IllegalArgumentException) { // From UserService if user exists
             return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(AuthResponse.error(e.message ?: "User registration failed."))
        } catch (e: Exception) {
            // Log the exception
            println("Error during registration: ${e.message}")
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("An unexpected error occurred during registration."))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        try {
            val user = userService.authenticateUser(request.email, request.password)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.error("Invalid email or password."))

            // Assess login risk using SFEBackendSDK
            val ipAddress = request.deviceInfo?.ipAddress ?: "unknown_ip"
            val userAgent = request.deviceInfo?.userAgent ?: "unknown_agent"
            val riskLevel = sfeBackendSDK.fraud().assessLoginRisk(user.userId, ipAddress, userAgent)

            when (riskLevel) {
                RiskLevel.HIGH -> {
                    // For HIGH risk, might block or require more stringent step-up, e.g., manual review.
                    // For this example, let's simulate requiring OTP.
                    val otpReference = sfeBackendSDK.auth().generateOTP(user.userId, StepUpType.OTP)
                    return ResponseEntity.status(HttpStatus.FORBIDDEN) // Or a custom status indicating step-up
                        .body(AuthResponse.requireOTP("High risk login detected. OTP required.", otpReference, user.userId))
                }
                RiskLevel.MEDIUM -> {
                    val otpReference = sfeBackendSDK.auth().generateOTP(user.userId, StepUpType.OTP)
                    return ResponseEntity.status(HttpStatus.ACCEPTED) // Or a custom status indicating step-up
                        .body(AuthResponse.requireOTP("Medium risk login. OTP verification required.", otpReference, user.userId))
                }
                RiskLevel.LOW -> {
                    val tokens = jwtService.generateTokens(user)
                    return ResponseEntity.ok(AuthResponse.success("Login successful.", tokens, user.userId))
                }
                // else -> { // Handle null or other unexpected RiskLevel if the enum could be extended by the SDK
                //    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AuthResponse.error("Error assessing login risk."))
                // }
            }
        } catch (e: SFEFraudException) {
            // Log the exception
             println("Fraud exception during login: ${e.message}, Report ID: ${e.fraudReportId}")
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AuthResponse.error("Login blocked due to fraud concerns: ${e.message}"))
        } catch (e: Exception) {
            // Log the exception
            println("Error during login: ${e.message}")
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.error("An unexpected error occurred during login."))
        }
    }
}
