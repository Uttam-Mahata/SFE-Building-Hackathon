package com.gradientgeeks.csfe.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.gradientgeeks.csfe.utils.Logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Advanced root detection with multiple detection methods
 */
class RootDetector(private val context: Context) {
    private val TAG = "RootDetector"
    
    /**
     * Comprehensive root detection using multiple techniques
     */
    fun isRooted(): Boolean {
        Logger.d(TAG, "Starting comprehensive root detection")
        
        val detectionResults = mutableListOf<Pair<String, Boolean>>()
        
        // Method 1: Check for common root binaries
        detectionResults.add("SU_BINARIES" to checkSuBinaries())
        
        // Method 2: Check for root management apps
        detectionResults.add("ROOT_APPS" to checkRootApps())
        
        // Method 3: Check build tags
        detectionResults.add("BUILD_TAGS" to checkBuildTags())
        
        // Method 4: Check writable system directories
        detectionResults.add("WRITABLE_DIRS" to checkWritableSystemDirs())
        
        // Method 5: Check for root cloaking apps
        detectionResults.add("CLOAK_APPS" to checkRootCloakingApps())
        
        // Method 6: Check dangerous properties
        detectionResults.add("DANGEROUS_PROPS" to checkDangerousProperties())
        
        // Method 7: Check native root detection
        detectionResults.add("NATIVE_CHECK" to checkRootNative())
        
        // Method 8: Check for Magisk
        detectionResults.add("MAGISK" to checkMagisk())
        
        // Log results for debugging
        detectionResults.forEach { (method, result) ->
            Logger.d(TAG, "Root detection method $method: $result")
        }
        
        // Return true if any method detected root
        return detectionResults.any { it.second }
    }
    
    /**
     * Check for common SU binary locations
     */
    private fun checkSuBinaries(): Boolean {
        val suPaths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su", 
            "/system/sbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/system/bin/.ext/.su",
            "/system/bin/failsafe/su",
            "/system/sd/xbin/su",
            "/system/usr/we-need-root/su",
            "/cache/su",
            "/data/su",
            "/dev/su"
        )
        
        return suPaths.any { path ->
            try {
                val file = File(path)
                file.exists() && file.canExecute()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Check for installed root management applications
     */
    private fun checkRootApps(): Boolean {
        val rootApps = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite", 
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.koushikdutta.rommanager",
            "com.koushikdutta.rommanager.license",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.ramdroid.appquarantine",
            "com.ramdroid.appquarantinepro",
            "com.topjohnwu.magisk"
        )
        
        val packageManager = context.packageManager
        return rootApps.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    
    /**
     * Check build tags for test-keys (indicates custom ROM)
     */
    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
    
    /**
     * Check if system directories are writable (shouldn't be on non-rooted devices)
     */
    private fun checkWritableSystemDirs(): Boolean {
        val systemDirs = arrayOf(
            "/system",
            "/system/bin",
            "/system/sbin", 
            "/system/xbin",
            "/vendor/bin",
            "/sbin",
            "/etc"
        )
        
        return systemDirs.any { dir ->
            try {
                File(dir).canWrite()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Check for root cloaking applications
     */
    private fun checkRootCloakingApps(): Boolean {
        val cloakingApps = arrayOf(
            "com.devadvance.rootcloak",
            "com.devadvance.rootcloakplus",
            "de.robv.android.xposed.installer",
            "com.saurik.substrate",
            "com.zachspong.temprootremovejb",
            "com.amphoras.hidemyroot",
            "com.amphoras.hidemyrootadfree",
            "com.formyhm.hiderootPremium",
            "com.formyhm.hideroot"
        )
        
        val packageManager = context.packageManager
        return cloakingApps.any { packageName ->
            try {
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    
    /**
     * Check for dangerous system properties
     */
    private fun checkDangerousProperties(): Boolean {
        val dangerousProps = mapOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0"
        )
        
        return dangerousProps.any { (prop, dangerousValue) ->
            try {
                val value = getSystemProperty(prop)
                value == dangerousValue
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Native root detection using shell commands
     */
    private fun checkRootNative(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            process.waitFor()
            !result.isNullOrEmpty()
        } catch (e: Exception) {
            // If we can't execute the command, assume not rooted
            false
        }
    }
    
    /**
     * Check for Magisk (systemless root)
     */
    private fun checkMagisk(): Boolean {
        val magiskPaths = arrayOf(
            "/sbin/.magisk",
            "/sbin/.core/mirror",
            "/sbin/.core/img",
            "/sbin/.core/db-0/magisk.db"
        )
        
        return magiskPaths.any { path ->
            try {
                File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Get system property value
     */
    private fun getSystemProperty(property: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $property")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            process.waitFor()
            result?.trim()
        } catch (e: Exception) {
            null
        }
    }
}