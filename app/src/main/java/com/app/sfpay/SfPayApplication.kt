package com.app.sfpay

import android.app.Application
import android.util.Log
import com.app.sfpay.BuildConfig
import com.gradientgeeks.sfesdk.SfeFrontendSdk

class SfPayApplication : Application() {
    
    companion object {
        private const val TAG = "SfPayApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "SFPay application starting...")
        
        try {
            // Initialize SFE SDK with application context
            val config = SfeFrontendSdk.SdkConfig(
                legitimateSignatureHash = BuildConfig.LEGITIMATE_SIGNATURE_HASH,
                enableDebugLogging = BuildConfig.DEBUG
            )
            
            SfeFrontendSdk.initialize(this, config)
            Log.d(TAG, "SFE SDK initialized during app startup")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SFE SDK during app startup", e)
        }
        
        Log.d(TAG, "SFPay application ready")
    }
} 