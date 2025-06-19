package com.sfe.paymentbackend.payment

import com.sfe.paymentbackend.auth.dto.UserPrincipal // Using existing UserPrincipal for dummy user
import com.sfe.paymentbackend.payment.dto.*
import com.sfe.sdk.backend.SFEBackendSDK
import com.sfe.sdk.backend.PaymentRequest as SDKPaymentRequest // Alias to avoid name clash
import com.sfe.sdk.backend.QRGenerationRequest as SDKQRGenerationRequest // Alias
import com.sfe.sdk.backend.PaymentProcessingResult
import com.sfe.sdk.backend.TransactionStatus
import com.sfe.sdk.backend.QRData
import com.sfe.sdk.backend.RiskLevel
import com.sfe.sdk.backend.exceptions.SFEValidationException
import com.sfe.sdk.backend.exceptions.SFEFraudException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val sfeBackendSDK: SFEBackendSDK
    // In a real app, you might inject other services like WalletService, NotificationService
) {

    // Helper to create a dummy UserPrincipal until proper auth is in place
    private fun getDummyUserPrincipal(): UserPrincipal =
        UserPrincipal("test-user-id-123", "testuser@example.com", listOf("ROLE_USER"))

    @PostMapping("/initiate")
    fun initiatePayment(@RequestBody request: InitiatePaymentRequest): ResponseEntity<*> {
        val currentUser = getDummyUserPrincipal() // Get dummy principal

        try {
            val sdkPaymentRequest = SDKPaymentRequest(
                amount = request.amount,
                currency = request.currency,
                recipientId = request.recipientId,
                senderId = currentUser.userId, // Using userId from dummy principal
                // description = request.description // SDK's PaymentRequest doesn't have description directly
            )

            // 1. Validate Payment Request (throws SFEValidationException on failure)
            sfeBackendSDK.payments().validatePaymentRequest(sdkPaymentRequest)

            // 2. Check User Limits (mock implementation in SDK assumed to pass)
            val withinLimits = sfeBackendSDK.payments().checkUserLimits(currentUser.userId, sdkPaymentRequest.amount)
            if (!withinLimits) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("message" to "Transaction exceeds user limits."))
            }

            // 3. Verify Recipient (mock implementation in SDK assumed to pass)
            val recipientVerified = sfeBackendSDK.payments().verifyRecipient(sdkPaymentRequest.recipientId)
            if (!recipientVerified) {
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Recipient verification failed."))
            }

            // 4. Perform Fraud Analysis
            // The SDK's PaymentRequest might need more fields for effective fraud analysis.
            // For now, using what's available.
            val riskProfile = sfeBackendSDK.payments().performFraudAnalysis(sdkPaymentRequest, currentUser.userId)
            if (riskProfile == RiskLevel.HIGH || riskProfile == RiskLevel.MEDIUM) { // Assuming performFraudAnalysis returns RiskLevel
                 // Based on SAMPLE-BACKEND-README, processTransaction might internally handle fraud checks
                 // and return a response indicating blockage.
                 // Or, we might explicitly block here.
                 // For now, let's assume processUPITransaction handles it or we proceed cautiously.
                 println("Payment attempt with risk level: $riskProfile for user ${currentUser.userId}")
                 // Optionally, could return a specific response if risk is too high to proceed.
            }

            // 5. Process UPI Transaction
            val processingResult: PaymentProcessingResult = sfeBackendSDK.payments().processUPITransaction(sdkPaymentRequest)

            // 6. Generate Audit Log (simplified)
            sfeBackendSDK.payments().generateAuditLog(processingResult.transactionId, "PAYMENT_INITIATED_BY_${currentUser.userId}")


            // The SAMPLE-BACKEND-README mentions walletService.debitAmount and notificationService.sendPaymentInitiated.
            // These would be called here if they were implemented and injected.
            // For example:
            // if (processingResult.status == com.sfe.sdk.backend.PaymentStatus.SUCCESS) {
            //     walletService.debitAmount(currentUser.userId, request.amount)
            //     notificationService.sendPaymentInitiated(currentUser.userId, processingResult.transactionId, request.amount)
            // }

            return ResponseEntity.ok(processingResult)

        } catch (e: SFEValidationException) {
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Payment validation failed: ${e.message}"))
        } catch (e: SFEFraudException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("message" to "Payment blocked due to fraud concerns: ${e.message}", "fraudReportId" to e.fraudReportId))
        } catch (e: Exception) {
            // Log the exception (e.g., using a logger)
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "An unexpected error occurred during payment initiation."))
        }
    }

    @GetMapping("/{transactionId}/status")
    fun getTransactionStatus(@PathVariable transactionId: String): ResponseEntity<*> {
        // val currentUser = getDummyUserPrincipal() // userId might be used by SDK for auth/scoping

        try {
            // The SDK method getTransactionStatus in TransactionOperations takes only transactionId.
            // If scoping by user is needed, the SDK interface would need to be different or
            // this backend would implement that logic on top.
            // For now, using the existing SDK method.
            val transactionStatus: TransactionStatus? = sfeBackendSDK.transactions().getStatus(transactionId)
            // The other SDK method is getTransactionStatus(transactionId) which returns PaymentStatus enum
            // Let's stick to getStatus which returns the TransactionStatus data class.

            return if (transactionStatus != null) {
                ResponseEntity.ok(PaymentStatusResponse(transactionDetails = transactionStatus))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("message" to "Transaction not found."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Error retrieving transaction status."))
        }
    }

    @PostMapping("/qr/generate")
    fun generateQR(@RequestBody request: GenerateQRRequest): ResponseEntity<*> {
        val currentUser = getDummyUserPrincipal()

        try {
            // Mapping GenerateQRRequest (API DTO) to SDKQRGenerationRequest (SDK DTO)
            val sdkQrRequestBuilder = SDKQRGenerationRequest.Builder()
                .amount(request.amount)
                .currency(request.currency)
                .merchantId(request.merchantId)
                .transactionReference(request.transactionReference)
                .purpose(request.purpose)
                // The SDK's QRGenerationRequest.Builder doesn't have setExpiryTime or enableDynamicPricing directly.
                // These would need to be added to the SDK's builder or handled differently if they are custom features.
                // For now, we build with available fields.

            val sdkQrRequest = sdkQrRequestBuilder.build()

            // The SDK's generatePaymentQR method in QROperations also doesn't explicitly take userId,
            // but it's good practice to assume context might be needed, or SDK handles it via API key.
            // The method signature from skeleton was: generatePaymentQR(request: QRGenerationRequest): QRData
            val qrData: QRData = sfeBackendSDK.qr().generatePaymentQR(sdkQrRequest)

            return ResponseEntity.ok(QRResponse(qrDetails = qrData))

        } catch (e: SFEValidationException) {
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "QR generation validation failed: ${e.message}"))
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "An unexpected error occurred during QR generation."))
        }
    }
}
