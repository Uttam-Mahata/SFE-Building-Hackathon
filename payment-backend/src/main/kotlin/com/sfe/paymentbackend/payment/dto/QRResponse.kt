package com.sfe.paymentbackend.payment.dto

import com.sfe.sdk.backend.QRData // SDK's QRData

// Wrapper for SDK's QRData
data class QRResponse(
    val qrDetails: QRData
)
