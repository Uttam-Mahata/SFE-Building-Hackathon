package com.gradientgeeks.csfe.payment

import java.util.Date

/**
 * Represents a payment request.
 */
data class PaymentRequest private constructor(
    val amount: Double,
    val recipientVPA: String?,
    val recipientMobile: String?,
    val description: String?,
    val transactionNote: String?,
    val paymentMode: PaymentMode
) {
    /**
     * Builder for creating PaymentRequest instances.
     */
    class Builder {
        private var amount: Double = 0.0
        private var recipientVPA: String? = null
        private var recipientMobile: String? = null
        private var description: String? = null
        private var transactionNote: String? = null
        private var paymentMode: PaymentMode = PaymentMode.UPI
        
        /**
         * Set the payment amount.
         */
        fun setAmount(amount: Double): Builder {
            this.amount = amount
            return this
        }
        
        /**
         * Set the recipient's VPA (Virtual Payment Address).
         */
        fun setRecipientVPA(vpa: String): Builder {
            this.recipientVPA = vpa
            return this
        }
        
        /**
         * Set the recipient's mobile number.
         */
        fun setRecipientMobile(mobile: String): Builder {
            this.recipientMobile = mobile
            return this
        }
        
        /**
         * Set the payment description (visible to recipient).
         */
        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }
        
        /**
         * Set a private note for the transaction (not visible to recipient).
         */
        fun setTransactionNote(note: String): Builder {
            this.transactionNote = note
            return this
        }
        
        /**
         * Set the payment mode.
         */
        fun setPaymentMode(mode: PaymentMode): Builder {
            this.paymentMode = mode
            return this
        }
        
        /**
         * Build the PaymentRequest instance.
         */
        fun build(): PaymentRequest {
            if (amount <= 0) {
                throw IllegalArgumentException("Amount must be greater than zero")
            }
            
            if (recipientVPA == null && recipientMobile == null) {
                throw IllegalArgumentException("Either VPA or mobile number must be specified")
            }
            
            return PaymentRequest(
                amount = amount,
                recipientVPA = recipientVPA,
                recipientMobile = recipientMobile,
                description = description,
                transactionNote = transactionNote,
                paymentMode = paymentMode
            )
        }
    }
}

/**
 * Payment modes supported by the SDK.
 */
enum class PaymentMode {
    /**
     * UPI payment.
     */
    UPI,
    
    /**
     * Wallet transfer.
     */
    WALLET,
    
    /**
     * Bank transfer.
     */
    BANK_TRANSFER,
    
    /**
     * QR code payment.
     */
    QR_CODE
}

/**
 * Results of payment operations.
 */
sealed class PaymentResult {
    /**
     * Successful payment.
     */
    data class Success(
        val transactionId: String,
        val amount: Double,
        val timestamp: Date,
        val recipient: String
    ) : PaymentResult()
    
    /**
     * Failed payment.
     */
    data class Error(
        val errorCode: String,
        val errorMessage: String,
        val isRetryable: Boolean
    ) : PaymentResult()
    
    /**
     * Payment is being processed.
     */
    data class Pending(
        val transactionId: String,
        val estimatedCompletionTime: Date?
    ) : PaymentResult()
}
