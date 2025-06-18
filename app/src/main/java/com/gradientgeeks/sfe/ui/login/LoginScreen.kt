package com.gradientgeeks.sfe.ui.login

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gradientgeeks.csfe.SFEClientSDK
import com.gradientgeeks.csfe.auth.LoginData
import com.gradientgeeks.csfe.auth.LoginResult
import kotlinx.coroutines.launch

/**
 * Login screen for user authentication.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val sfeSDK = SFEClientSDK.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Demo OTP for hackathon purposes - in a real app, this would be sent via SMS
    val demoOtp = "123456"
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Login form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SFE Payment App",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Demo OTP: $demoOtp",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        // Validate inputs
                        when {
                            phoneNumber.isEmpty() -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter a phone number")
                                }
                            }
                            otp.isEmpty() -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter the OTP")
                                }
                            }
                            else -> {
                                isLoading = true
                                
                                // Call SFE SDK to login
                                val loginData = LoginData(
                                    phoneNumber = phoneNumber,
                                    otp = otp
                                )
                                
                                sfeSDK.auth().login(loginData) { result ->
                                    isLoading = false
                                    
                                    when (result) {
                                        is LoginResult.Success -> {
                                            // Login successful, navigate to dashboard
                                            onLoginSuccess()
                                        }
                                        is LoginResult.Error -> {
                                            // Show error message
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Login failed: ${result.message}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Login")
                }
            }
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
