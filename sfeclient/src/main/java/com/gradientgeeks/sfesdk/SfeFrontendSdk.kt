package com.gradientgeeks.sfesdk

import android.content.Context
import com.gradientgeeks.sfesdk.managers.AttestationManager
import com.gradientgeeks.sfesdk.managers.DeviceBindingManager
import com.gradientgeeks.sfesdk.managers.PayloadManager
import com.gradientgeeks.sfesdk.managers.RaspManager
import com.gradientgeeks.sfesdk.managers.impl.AttestationManagerImpl
import com.gradientgeeks.sfesdk.managers.impl.DeviceBindingManagerImpl
import com.gradientgeeks.sfesdk.managers.impl.PayloadManagerImpl
import com.gradientgeeks.sfesdk.managers.impl.RaspManagerImpl

/**
 * Main entry point for the SFE Frontend SDK
 * 
 * This is a headless Android library that provides robust client-side security intelligence
 * without any UI components.
 */
object SfeFrontendSdk {
    
    /**
     * Configuration for the SDK
     */
    data class SdkConfig(
        val legitimateSignatureHash: String,
        val enableDebugLogging: Boolean = false
    )
    
    private var isInitialized = false
    private var sdkConfig: SdkConfig? = null
    private var applicationContext: Context? = null
    
    // Managers - lazy initialized
    val deviceBindingManager: DeviceBindingManager by lazy { DeviceBindingManagerImpl() }
    val raspManager: RaspManager by lazy { RaspManagerImpl() }
    val attestationManager: AttestationManager by lazy { AttestationManagerImpl() }
    val payloadManager: PayloadManager by lazy { PayloadManagerImpl() }
    
    /**
     * Initialize the SDK with configuration
     * 
     * @param context Application context
     * @param config SDK configuration
     */
    @JvmStatic
    fun initialize(context: Context, config: SdkConfig) {
        applicationContext = context.applicationContext
        sdkConfig = config
        isInitialized = true
        
        if (config.enableDebugLogging) {
            android.util.Log.d("SfeFrontendSdk", "SDK initialized with config: $config")
        }
    }
    
    /**
     * Check if SDK is initialized
     */
    @JvmStatic
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Get the SDK configuration
     */
    internal fun getConfig(): SdkConfig? = sdkConfig
    
    /**
     * Get the application context
     */
    @JvmStatic
    fun getApplicationContext(): Context? = applicationContext
} 