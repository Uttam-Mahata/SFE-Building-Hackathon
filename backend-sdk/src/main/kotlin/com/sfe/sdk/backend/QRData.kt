package com.sfe.sdk.backend

data class QRData(
    val qrString: String, // The actual QR code string data
    val imageFormat: String = "PNG", // e.g., PNG, SVG
    val imageBytes: ByteArray? = null // Optional: if the SDK also generates the image
) {
    // equals and hashCode for ByteArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QRData

        if (qrString != other.qrString) return false
        if (imageFormat != other.imageFormat) return false
        if (imageBytes != null) {
            if (other.imageBytes == null) return false
            if (!imageBytes.contentEquals(other.imageBytes)) return false
        } else if (other.imageBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = qrString.hashCode()
        result = 31 * result + imageFormat.hashCode()
        result = 31 * result + (imageBytes?.contentHashCode() ?: 0)
        return result
    }
}
