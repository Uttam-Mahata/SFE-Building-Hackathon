package com.gradientgeeks.sfesdk.managers.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.gradientgeeks.sfesdk.SfeFrontendSdk
import com.gradientgeeks.sfesdk.managers.RaspManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class RaspManagerImpl : RaspManager {
    
    companion object {
        private val ROOT_INDICATORS = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        private val ROOT_APPS = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot"
        )
    }
    
    override fun isDeviceRooted(): Boolean {
        return checkRootBinaries() || checkRootApps() || checkTestKeys()
    }
    
    override fun isAppTampered(context: Context): Boolean {
        val config = SfeFrontendSdk.getConfig()
        if (config?.legitimateSignatureHash == null) {
            return false // Cannot verify without legitimate hash
        }
        
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName, 
                PackageManager.GET_SIGNATURES
            )
            
            val currentSignature = packageInfo.signatures?.get(0)?.toCharsString() ?: return true
            val currentHash = hashSignature(currentSignature)
            
            currentHash != config.legitimateSignatureHash
        } catch (e: Exception) {
            true // Assume tampered if cannot verify
        }
    }
    
    override fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || checkTracerPid()
    }
    
    private fun checkRootBinaries(): Boolean {
        return ROOT_INDICATORS.any { path ->
            File(path).exists()
        }
    }
    
    private fun checkRootApps(): Boolean {
        // This would require checking for root management apps
        // For prototype, simplified check
        return false
    }
    
    private fun checkTestKeys(): Boolean {
        return Build.TAGS != null && Build.TAGS.contains("test-keys")
    }
    
    private fun checkTracerPid(): Boolean {
        return try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                BufferedReader(FileReader(statusFile)).use { reader ->
                    reader.lineSequence().any { line ->
                        line.startsWith("TracerPid:") && !line.contains("TracerPid:\t0")
                    }
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hashSignature(signature: String): String {
        // Simplified hash - in production would use proper cryptographic hash
        return signature.hashCode().toString()
    }
} 