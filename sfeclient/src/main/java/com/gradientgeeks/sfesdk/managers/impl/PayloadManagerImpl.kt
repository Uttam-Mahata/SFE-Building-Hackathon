package com.gradientgeeks.sfesdk.managers.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.gson.Gson
import com.gradientgeeks.sfesdk.SfeFrontendSdk
import com.gradientgeeks.sfesdk.exceptions.SfeException
import com.gradientgeeks.sfesdk.exceptions.SfeInitializationException
import com.gradientgeeks.sfesdk.managers.PayloadManager
import com.gradientgeeks.sfesdk.models.BindingInfo
import com.gradientgeeks.sfesdk.models.DeviceInfo
import com.gradientgeeks.sfesdk.models.SecurePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PayloadManagerImpl : PayloadManager {
    
    companion object {
        private const val SDK_VERSION = "1.2.0-prototype"
        private val gson = Gson()
    }
    
    override suspend fun constructSecurePayload(context: Context, attestationToken: String?): Result<String> {
        return withContext(Dispatchers.Default) {
            try {
                if (!SfeFrontendSdk.isInitialized()) {
                    return@withContext Result.failure(SfeInitializationException("SDK not initialized"))
                }
                
                val payload = SecurePayload(
                    appVersion = getAppVersion(context),
                    sdkVersion = SDK_VERSION,
                    timestamp = getCurrentTimestamp(),
                    deviceInfo = buildDeviceInfo(context),
                    bindingInfo = buildBindingInfo(context),
                    attestationToken = attestationToken
                )
                
                val jsonPayload = gson.toJson(payload)
                Result.success(jsonPayload)
            } catch (e: Exception) {
                Result.failure(SfeException("Failed to construct secure payload", e))
            }
        }
    }
    
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }
    }
    
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }
    
    private suspend fun buildDeviceInfo(context: Context): DeviceInfo {
        val raspManager = SfeFrontendSdk.raspManager
        
        return DeviceInfo(
            osVersion = Build.VERSION.RELEASE,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            isRooted = raspManager.isDeviceRooted(),
            isDebuggerAttached = raspManager.isDebuggerAttached(),
            isAppTampered = raspManager.isAppTampered(context)
        )
    }
    
    private suspend fun buildBindingInfo(context: Context): BindingInfo {
        val deviceBindingManager = SfeFrontendSdk.deviceBindingManager
        
        val deviceBindingToken = deviceBindingManager.getDeviceBindingToken(context)
            .getOrThrow()
        
        return BindingInfo(
            simPresent = deviceBindingManager.isSimPresent(context),
            networkOperator = deviceBindingManager.getNetworkOperator(context),
            deviceBindingToken = deviceBindingToken
        )
    }
} 