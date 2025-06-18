package com.gradientgeeks.csfe.qr

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.utils.Logger
import java.lang.ref.WeakReference
import java.util.EnumMap
import java.util.UUID

/**
 * Handles QR code generation and scanning for payments.
 */
class QRCodeModule(
    private val context: Context,
    private val config: SFEConfig
) {
    private val TAG = "QRCodeModule"
    private var scanCallbackRef: WeakReference<((QRScanResult) -> Unit)>? = null
    
    /**
     * Generates a payment QR code.
     * 
     * @param request The QR code generation request
     * @param callback Callback with the QR code generation result
     */
    fun generateQRCode(request: QRGenerationRequest, callback: (QRGenerationResult) -> Unit) {
        Logger.d(TAG, "Generating QR code for amount: ${request.amount}")
        
        try {
            // Generate QR code content (in a real app, this would be a standardized format)
            val uuid = UUID.randomUUID().toString()
            val expiryTime = request.expiryMinutes?.let {
                System.currentTimeMillis() + (it * 60 * 1000)
            } ?: (System.currentTimeMillis() + 15 * 60 * 1000) // Default 15 minutes
            
            val qrContent = buildString {
                append("upi://pay?")
                append("pa=demoapp@sfe&")
                append("pn=SFE Demo&")
                if (request.amount != null) {
                    append("am=${request.amount}&")
                }
                if (!request.description.isNullOrEmpty()) {
                    append("tn=${request.description}&")
                }
                append("tr=$uuid&")
                append("exp=$expiryTime&")
                append("cu=INR")
            }
            
            // Generate QR bitmap
            val qrBitmap = generateQRBitmap(qrContent, 512)
            
            if (qrBitmap != null) {
                // Return success with the bitmap
                callback(QRGenerationResult.Success(qrBitmap, qrContent))
            } else {
                callback(QRGenerationResult.Error("Failed to generate QR code"))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error generating QR code: ${e.message}")
            callback(QRGenerationResult.Error("Error generating QR code: ${e.message}"))
        }
    }
    
    /**
     * Starts the QR code scanner.
     * 
     * Note: In a real implementation, this would integrate with a camera-based
     * QR scanner library or intent. For the hackathon prototype, we're simulating
     * the scan result.
     * 
     * @param activity The activity to launch the scanner from
     * @param callback Callback with the scan result
     */
    fun scanQRCode(activity: Activity, callback: (QRScanResult) -> Unit) {
        Logger.d(TAG, "Starting QR code scanner")
        
        // Store the callback reference for when the scan completes
        scanCallbackRef = WeakReference(callback)
        
        // In a real implementation, this would launch a QR scanner
        // For now, simulate a successful scan after a short delay
        
        if (activity is AppCompatActivity) {
            // This is just a placeholder to demonstrate what would happen in a real app
            // In reality, you would integrate a QR scanner library and configure a proper result callback
            simulateQRScan(callback)
        } else {
            callback(QRScanResult.Error("Activity must be an AppCompatActivity"))
        }
    }
    
    /**
     * Helper method to generate a QR code bitmap.
     * 
     * @param content The content to encode in the QR code
     * @param size The size of the QR code in pixels
     * @return The QR code bitmap, or null if generation failed
     */
    @Throws(WriterException::class)
    private fun generateQRBitmap(content: String, size: Int): Bitmap? {
        try {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            hints[EncodeHintType.MARGIN] = 2
            
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
            
        } catch (e: Exception) {
            Logger.e(TAG, "Error generating QR bitmap: ${e.message}")
            return null
        }
    }
    
    /**
     * Simulates a QR code scan result for the hackathon demo.
     * In a real app, this would be replaced with actual camera scanning.
     */
    private fun simulateQRScan(callback: (QRScanResult) -> Unit) {
        // Simulate processing time
        android.os.Handler().postDelayed({
            // Create a mock payment data object
            val paymentData = QRPaymentData(
                recipientVPA = "demouser@sfe",
                amount = 100.0,
                description = "Demo Payment",
                merchantName = "SFE Demo Store",
                referenceId = "ref_${System.currentTimeMillis()}"
            )
            
            callback(QRScanResult.Success(paymentData))
        }, 1500)
    }
}
