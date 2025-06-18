package com.gradientgeeks.sfe.ui.qrscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gradientgeeks.csfe.SFEClientSDK
import com.gradientgeeks.sfe.ui.theme.SFETheme

/**
 * Activity for scanning QR codes for payments.
 * In a real implementation, this would use CameraX to scan QR codes.
 * For the hackathon prototype, we're using the SDK's simulated QR scanner.
 */
class QRScannerActivity : ComponentActivity() {
    
    private val sfeSDK = SFEClientSDK.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SFETheme {
                QRScannerScreen(
                    onBackClick = { finish() }
                )
            }
        }
        
        // Start QR scanning when activity opens
        startQRScanner()
    }
    
    private fun startQRScanner() {
        sfeSDK.qr().scanQRCode(this) { result ->
            when (result) {
                is com.gradientgeeks.csfe.qr.QRScanResult.Success -> {
                    // Pass data back to calling activity
                    val intent = intent.apply {
                        putExtra("recipient_vpa", result.paymentData.recipientVPA)
                        putExtra("amount", result.paymentData.amount ?: 0.0)
                        putExtra("description", result.paymentData.description ?: "")
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is com.gradientgeeks.csfe.qr.QRScanResult.InvalidQR -> {
                    // Show error
                    showError("Invalid QR code. Please scan a valid payment QR.")
                }
                is com.gradientgeeks.csfe.qr.QRScanResult.Error -> {
                    // Show error
                    showError("Error scanning QR code: ${result.errorMessage}")
                }
            }
        }
    }
    
    private fun showError(message: String) {
        // In a real app, show a proper error message to the user
        // For now, just close the activity
        setResult(RESULT_CANCELED)
        finish()
    }
}

/**
 * QR Scanner screen with camera preview.
 * In a real implementation, this would show a camera preview with QR scanner overlay.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // In a real app, this would show a camera preview
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Scanning QR Code...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
