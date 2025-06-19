package com.sfe.sdk.backend.ops

import com.sfe.sdk.backend.QRData // Assuming these classes exist
import com.sfe.sdk.backend.QRGenerationRequest

interface QROperations {
    /**
     * Generates QR data for a payment.
     * @param request The request details for QR code generation.
     * @return QRData containing the QR string and potentially other details.
     */
    fun generatePaymentQR(request: QRGenerationRequest): QRData {
        throw NotImplementedError("generatePaymentQR is not yet implemented.")
    }

    /**
     * Parses QR code data to extract payment information.
     * @param qrString The string representation of the QR code.
     * @return A map or a dedicated data class containing the parsed information.
     */
    fun parseQRData(qrString: String): Map<String, String> { // Or a specific data class
        throw NotImplementedError("parseQRData is not yet implemented.")
    }
}
