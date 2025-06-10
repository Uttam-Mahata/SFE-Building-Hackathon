package com.gradientgeeks.sfesdk.managers.impl

import android.content.Context
import android.telephony.TelephonyManager
import com.gradientgeeks.sfesdk.exceptions.SfeSecurityException
import com.gradientgeeks.sfesdk.managers.DeviceBindingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class DeviceBindingManagerImpl : DeviceBindingManager {
    
    companion object {
        private const val PREFS_NAME = "sfe_device_binding"
        private const val KEY_DEVICE_TOKEN = "device_binding_token"
    }
    
    override fun isSimPresent(context: Context): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.simState == TelephonyManager.SIM_STATE_READY
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getNetworkOperator(context: Context): String? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val operatorName = telephonyManager?.networkOperatorName
            if (operatorName?.isNotEmpty() == true) operatorName else null
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getDeviceBindingToken(context: Context): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                
                // Check if token already exists
                val existingToken = prefs.getString(KEY_DEVICE_TOKEN, null)
                if (existingToken != null) {
                    return@withContext Result.success(existingToken)
                }
                
                // Generate new token
                val newToken = generateDeviceToken()
                
                // Store token
                prefs.edit()
                    .putString(KEY_DEVICE_TOKEN, newToken)
                    .apply()
                
                Result.success(newToken)
            } catch (e: Exception) {
                Result.failure(SfeSecurityException("Failed to generate device binding token", e))
            }
        }
    }
    
    private fun generateDeviceToken(): String {
        // For prototype: Use UUID
        // In production: Would use Android Keystore for cryptographic token
        return UUID.randomUUID().toString()
    }
} 