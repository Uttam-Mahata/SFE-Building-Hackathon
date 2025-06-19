package com.sfe.paymentbackend.service

import com.sfe.paymentbackend.persistence.entity.WalletEntity
import com.sfe.paymentbackend.persistence.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val userService: UserService // Inject UserService to check if user exists
) {

    @Transactional
    fun debitAmount(userId: String, amount: Double) {
        val wallet = walletRepository.findByUserId(userId)
            ?: throw IllegalStateException("Wallet not found for user $userId to debit.")

        if (wallet.balance >= amount) {
            wallet.balance -= amount
            wallet.updatedAt = Instant.now()
            walletRepository.save(wallet)
            println("WalletService: Debited $amount from user $userId. New balance: ${wallet.balance}")
        } else {
            println("WalletService: Insufficient funds for user $userId to debit $amount.")
            throw IllegalStateException("Insufficient funds for user $userId to debit $amount.")
        }
    }

    @Transactional
    fun creditAmount(userId: String, amount: Double) {
        // Ensure user exists before creating/crediting a wallet
        userService.findUserById(userId) ?: throw IllegalArgumentException("User with ID $userId not found. Cannot credit wallet.")

        var wallet = walletRepository.findByUserId(userId)
        if (wallet == null) {
            wallet = WalletEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                balance = amount,
                currency = "INR", // Default currency
                status = "ACTIVE", // Default status
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            println("WalletService: Created new wallet for user $userId with initial credit of $amount.")
        } else {
            wallet.balance += amount
            wallet.updatedAt = Instant.now()
            println("WalletService: Credited $amount to user $userId. New balance: ${wallet.balance}")
        }
        walletRepository.save(wallet)
    }

    fun getWallet(userId: String): WalletEntity? {
        return walletRepository.findByUserId(userId)
    }

    // Optional: Method to create a wallet explicitly if needed, e.g., upon user registration
    @Transactional
    fun createWalletForUser(userId: String, initialBalance: Double = 0.0, currency: String = "INR"): WalletEntity {
        if (walletRepository.findByUserId(userId) != null) {
            throw IllegalStateException("Wallet already exists for user $userId.")
        }
        // Ensure user exists
        userService.findUserById(userId) ?: throw IllegalArgumentException("User with ID $userId not found. Cannot create wallet.")

        val newWallet = WalletEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            balance = initialBalance,
            currency = currency,
            status = "ACTIVE",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        return walletRepository.save(newWallet)
    }
}
