package com.sfe.paymentbackend.wallet

import com.sfe.paymentbackend.auth.dto.UserPrincipal // Using existing UserPrincipal for dummy user
import com.sfe.paymentbackend.wallet.dto.*
import com.sfe.sdk.backend.SFEBackendSDK
import com.sfe.sdk.backend.AddMoneyRequest as SDKAddMoneyRequest // Alias for SDK's AddMoneyRequest
import com.sfe.sdk.backend.TransactionFilter
import com.sfe.sdk.backend.TransactionHistoryResponse as SDKTransactionHistoryResponse
import com.sfe.sdk.backend.EncryptedBalance as SDKEncryptedBalance
import com.sfe.sdk.backend.PaymentResponse as SDKPaymentResponse
import com.sfe.sdk.backend.exceptions.SFEValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("/api/wallet")
class WalletController(
    private val sfeBackendSDK: SFEBackendSDK
) {

    // Helper to create a dummy UserPrincipal until proper auth is in place
    private fun getDummyUserPrincipal(): UserPrincipal =
        UserPrincipal("test-user-id-123", "testuser@example.com", listOf("ROLE_USER"))

    @GetMapping("/balance")
    fun getWalletBalance(): ResponseEntity<*> {
        val currentUser = getDummyUserPrincipal()
        try {
            // Assuming walletId is implicitly handled by SDK or not needed for default wallet
            val sdkEncryptedBalance: SDKEncryptedBalance = sfeBackendSDK.wallet().getEncryptedBalance(currentUser.userId)
            val response = WalletBalanceResponse.fromSDKEncryptedBalance(sdkEncryptedBalance)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Error retrieving wallet balance: ${e.message}"))
        }
    }

    @PostMapping("/add-money")
    fun addMoney(@RequestBody request: AddMoneyRequest): ResponseEntity<*> {
        val currentUser = getDummyUserPrincipal()
        try {
            val sdkAddMoneyRequest = SDKAddMoneyRequest(
                userId = currentUser.userId,
                amount = request.amount,
                currency = request.currency,
                paymentMethodId = request.paymentMethodId
            )

            // The SDK's WalletOperations has addMoney(request: AddMoneyRequest): PaymentResponse
            val sdkPaymentResponse: SDKPaymentResponse = sfeBackendSDK.wallet().addMoney(sdkAddMoneyRequest)

            // The SAMPLE-BACKEND-README mentions walletService.creditAmount after success.
            // This would be a separate service call if implemented.
            // e.g., if (sdkPaymentResponse.status == com.sfe.sdk.backend.PaymentStatus.SUCCESS) {
            //     walletService.creditAmount(currentUser.userId, request.amount);
            // }

            val response = AddMoneyResponse.fromSDKPaymentResponse(sdkPaymentResponse)
            return ResponseEntity.ok(response)

        } catch (e: SFEValidationException) {
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Add money validation failed: ${e.message}"))
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "An unexpected error occurred while adding money: ${e.message}"))
        }
    }

    @GetMapping("/transactions")
    fun getWalletTransactions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam startDate: String?, // Expecting ISO date string e.g., 2023-01-01T00:00:00Z
        @RequestParam endDate: String?
    ): ResponseEntity<*> {
        val currentUser = getDummyUserPrincipal()
        try {
            val filterBuilder = TransactionFilter.Builder()
                .userId(currentUser.userId)
                // .pageNumber(page) // SDK's TransactionFilter.Builder doesn't have page/size
                // .pageSize(size)   // These would need to be added to SDK or handled post-fetch

            try {
                startDate?.let { filterBuilder.fromDate(Instant.parse(it).toEpochMilli()) }
                endDate?.let { filterBuilder.toDate(Instant.parse(it).toEpochMilli()) }
            } catch (e: DateTimeParseException) {
                return ResponseEntity.badRequest()
                    .body(mapOf("message" to "Invalid date format. Please use ISO 8601 format (e.g., YYYY-MM-DDTHH:mm:ssZ)."))
            }

            val sdkFilter = filterBuilder.build()

            // Using TransactionOperations.listTransactions as ReportingOperations isn't available
            // and WalletOperations.getWalletTransactionHistory doesn't take a filter.
            // This implies transactions listed might not be wallet-specific unless filtered by other means
            // or if userId in TransactionFilter is sufficient for the SDK's mock logic.
            val sdkTransactionHistory: SDKTransactionHistoryResponse = sfeBackendSDK.transactions().listTransactions(sdkFilter)

            // The SDK's TransactionHistoryResponse contains List<TransactionStatus>.
            // If pagination was intended, it needs to be implemented in the SDK or done in-memory here (less ideal).
            // For now, returning the potentially unpaginated (or SDK-default paginated) list.

            return ResponseEntity.ok(WalletTransactionsResponse(history = sdkTransactionHistory))

        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Error retrieving wallet transactions: ${e.message}"))
        }
    }
}
