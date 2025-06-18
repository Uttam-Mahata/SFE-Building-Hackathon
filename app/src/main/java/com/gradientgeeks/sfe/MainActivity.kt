package com.gradientgeeks.sfe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.gradientgeeks.csfe.SFEClientSDK
import com.gradientgeeks.sfe.navigation.AppNavigation
import com.gradientgeeks.sfe.ui.theme.SFETheme

/**
 * Main activity for the Sample Payment App.
 */
class MainActivity : ComponentActivity() {
    
    // Get the SFE SDK instance
    private val sfeSDK = SFEClientSDK.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SFETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PaymentApp()
                }
            }
        }
    }
}

/**
 * Main composable for the Payment App.
 */
@Composable
fun PaymentApp() {
    val navController = rememberNavController()
    
    AppNavigation(
        navController = navController,
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun PaymentAppPreview() {
    SFETheme {
        PaymentApp()
    }
}