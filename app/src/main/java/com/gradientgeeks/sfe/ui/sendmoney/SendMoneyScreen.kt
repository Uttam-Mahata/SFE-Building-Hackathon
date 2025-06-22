package com.gradientgeeks.sfe.ui.sendmoney

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gradientgeeks.csfe.SFEClientSDK
import com.gradientgeeks.csfe.auth.BiometricResult
import com.gradientgeeks.csfe.payment.PaymentMode
import com.gradientgeeks.csfe.payment.PaymentRequest
import com.gradientgeeks.csfe.payment.PaymentResult
import kotlinx.coroutines.launch

/**
 * Screen for sending money to another user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    onBackClick: () -> Unit,
    onTransactionComplete: () -> Unit
) {
    val context = LocalContext.current
    val sfeSDK = SFEClientSDK.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var recipientVPA by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // States for the confirmation dialog
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var currentRequest by remember { mutableStateOf<PaymentRequest?>(null) }
    
    // State for the success dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    var transactionId by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Money") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Transfer Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = recipientVPA,
                    onValueChange = { recipientVPA = it },
                    label = { Text("UPI ID / Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { /* Open QR scanner */ }) {
                            Icon(Icons.Default.QrCode, contentDescription = "Scan QR")
                        }
                    }
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        // Validate inputs
                        when {
                            recipientVPA.isEmpty() -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter a UPI ID or phone number")
                                }
                            }
                            amount.isEmpty() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0 -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter a valid amount")
                                }
                            }
                            else -> {
                                // Create payment request
                                val paymentRequest = PaymentRequest.Builder()
                                    .setAmount(amount.toDouble())
                                    .setRecipientVPA(recipientVPA)
                                    .setDescription(note)
                                    .setPaymentMode(PaymentMode.UPI)
                                    .build()
                                
                                currentRequest = paymentRequest
                                showConfirmationDialog = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Proceed")
                }
            }
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Payment confirmation dialog
        if (showConfirmationDialog && currentRequest != null) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmationDialog = false
                },
                title = { Text("Confirm Payment") },
                text = {
                    Column {
                        Text("You are about to send:")
                        Text(
                            text = "₹ ${currentRequest!!.amount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("To: ${currentRequest!!.recipientVPA}")
                        if (!currentRequest!!.description.isNullOrEmpty()) {
                            Text("Note: ${currentRequest!!.description}")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmationDialog = false
                            isLoading = true
                            if (context is ComponentActivity) {
                                val activity = context
                                sfeSDK.auth().authenticateWithBiometrics(
                                    activity = activity,
                                    title = "Confirm Payment",
                                    subtitle = "Pay ₹${currentRequest!!.amount} to ${currentRequest!!.recipientVPA}",
                                    description = "Use your fingerprint to confirm this payment"
                                ) { authResult ->
                                    when (authResult) {
                                        is BiometricResult.Success -> {
                                            // Authentication succeeded, proceed with payment
                                            sfeSDK.payments().initiatePayment(currentRequest!!, authResult.token) { paymentResult ->
                                                isLoading = false
                                                when (paymentResult) {
                                                    is PaymentResult.Success -> {
                                                        transactionId = paymentResult.transactionId
                                                        showSuccessDialog = true
                                                    }
                                                    is PaymentResult.Error -> {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Payment failed: ${paymentResult.errorMessage}")
                                                        }
                                                    }
                                                    is PaymentResult.Pending -> {
                                                        isLoading = false
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Payment is being processed. ID: ${paymentResult.transactionId}")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        is BiometricResult.Error -> {
                                            isLoading = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Authentication failed: ${authResult.errorMessage}")
                                            }
                                        }
                                        is BiometricResult.Cancelled -> {
                                            isLoading = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Authentication cancelled")
                                            }
                                        }
                                    }
                                }
                            } else {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: Could not access Activity")
                                }
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showConfirmationDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Success dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onTransactionComplete()
                },
                title = { Text("Payment Successful") },
                text = {
                    Column {
                        Text("Your payment has been processed successfully!")
                        Text("Transaction ID: $transactionId")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onTransactionComplete()
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
