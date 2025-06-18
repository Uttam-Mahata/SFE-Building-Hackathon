package com.gradientgeeks.csfe.qr

import android.graphics.Bitmap

/**
 * Request parameters for generating a QR code.
 */
data class QRGenerationRequest private constructor(
    val amount: Double?,
    val description: String?,
    val expiryMinutes: Int?
) {
    /**
     * Builder for creating QRGenerationRequest instances.
     */
    class Builder {
        private var amount: Double? = null
        private var description: String? = null
        private var expiryMinutes: Int? = 15 // Default 15 minutes
        
        /**
         * Set the payment amount in the QR code.
         * If null, the user will enter the amount.
         */
        fun setAmount(amount: Double?): Builder {
            this.amount = amount
            return this
        }
        
        /**
         * Set the payment description in the QR code.
         */
        fun setDescription(description: String?): Builder {
            this.description = description
            return this
        }
        
        /**
         * Set how long the QR code will be valid (in minutes).
         */
        fun setExpiryMinutes(minutes: Int): Builder {
            this.expiryMinutes = minutes
            return this
        }
        
        /**
         * Build the QRGenerationRequest instance.
         */
        fun build(): QRGenerationRequest {
            return QRGenerationRequest(
                amount = amount,
                description = description,
                expiryMinutes = expiryMinutes
            )
        }
    }
}

/**
 * Results from QR code generation.
 */
sealed class QRGenerationResult {
    /**
     * QR code generation succeeded.
     */
    data class Success(
        val qrCodeBitmap: Bitmap,
        val qrCodeContent: String
    ) : QRGenerationResult()
    
    /**
     * QR code generation failed.
     */
    data class Error(
        val errorMessage: String
    ) : QRGenerationResult()
}

/**
 * Results from QR code scanning.
 */
sealed class QRScanResult {
    /**
     * QR scan succeeded with payment data.
     */
    data class Success(
        val paymentData: QRPaymentData
    ) : QRScanResult()
    
    /**
     * QR code scanned, but it's not a valid payment QR.
     */
    data class InvalidQR(
        val scannedContent: String
    ) : QRScanResult()
    
    /**
     * QR scanning failed with an error.
     */
    data class Error(
        val errorMessage: String
    ) : QRScanResult()
}

/**
 * Payment data parsed from a QR code.
 */
data class QRPaymentData(
    val recipientVPA: String,
    val amount: Double?,
    val description: String?,
    val merchantName: String?,
    val referenceId: String?
)
