package com.gradientgeeks.sfe

import android.app.Application
import com.gradientgeeks.csfe.SFEClientSDK
import com.gradientgeeks.csfe.config.LogLevel
import com.gradientgeeks.csfe.config.SFEConfig
import com.gradientgeeks.csfe.config.SFEEnvironment

/**
 * Main Application class for the Sample Payment App.
 * Initializes the SFE Client SDK and other global components.
 */
class PaymentApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SFE Client SDK
        SFEClientSDK.initialize(
            context = this,
            config = SFEConfig.Builder()
                .setApiKey("sfe-hackathon-api-key-123456")
                .setApiBaseUrl("https://api.sfe-hackathon.com/")
                .setEnvironment(if (BuildConfig.DEBUG) SFEEnvironment.SANDBOX else SFEEnvironment.PRODUCTION)
                .enableBiometrics(true)
                .enableDeviceBinding(true)
                .setLogLevel(if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR)
                .enableMockPayments(true) // For hackathon demo
                .build()
        )
    }
}
