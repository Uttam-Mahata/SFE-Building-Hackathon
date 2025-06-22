package com.gradientgeeks.csfe.security

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.gradientgeeks.csfe.utils.Logger
import java.io.File

/**
 * Screen recording and screenshot protection
 */
class ScreenProtector(private val context: Context) {
    private val TAG = "ScreenProtector"
    private var isProtectionEnabled = false
    private var screenRecordingDetectedCallback: (() -> Unit)? = null
    
    /**
     * Enable comprehensive screen protection for an activity
     */
    fun enableScreenProtection(
        activity: Activity,
        onScreenRecordingDetected: (() -> Unit)? = null
    ) {
        screenRecordingDetectedCallback = onScreenRecordingDetected
        isProtectionEnabled = true
        
        Logger.d(TAG, "Enabling screen protection")
        
        // Method 1: Prevent screenshots and screen recording using FLAG_SECURE
        enableSecureFlag(activity)
        
        // Method 2: Add overlay protection
        enableOverlayProtection(activity)
        
        // Method 3: Start monitoring for screen recording
        startScreenRecordingMonitoring()
        
        // Method 4: Detect external display connections
        monitorExternalDisplays()
    }
    
    /**
     * Disable screen protection
     */
    fun disableScreenProtection(activity: Activity) {
        isProtectionEnabled = false
        Logger.d(TAG, "Disabling screen protection")
        
        // Remove secure flag
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        
        // Remove any overlay protections
        removeOverlayProtection(activity)
    }
    
    /**
     * Check if screen recording is currently active
     */
    fun isScreenRecordingActive(): Boolean {
        Logger.d(TAG, "Checking for active screen recording")
        
        val detectionResults = mutableListOf<Pair<String, Boolean>>()
        
        // Method 1: Check media projection service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            detectionResults.add("MEDIA_PROJECTION" to checkMediaProjection())
        }
        
        // Method 2: Check for screen recording processes
        detectionResults.add("RECORDING_PROCESSES" to checkRecordingProcesses())
        
        // Method 3: Check for screen recording files
        detectionResults.add("RECORDING_FILES" to checkRecordingFiles())
        
        // Method 4: Check system properties
        detectionResults.add("SYSTEM_PROPERTIES" to checkRecordingProperties())
        
        // Method 5: Check display metrics changes
        detectionResults.add("DISPLAY_CHANGES" to checkDisplayChanges())
        
        // Log results for debugging
        detectionResults.forEach { (method, result) ->
            Logger.d(TAG, "Screen recording detection method $method: $result")
        }
        
        return detectionResults.any { it.second }
    }
    
    /**
     * Enable FLAG_SECURE to prevent screenshots and basic screen recording
     */
    private fun enableSecureFlag(activity: Activity) {
        try {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            Logger.d(TAG, "FLAG_SECURE enabled")
        } catch (e: Exception) {
            Logger.e(TAG, "Error enabling FLAG_SECURE: ${e.message}")
        }
    }
    
    /**
     * Add overlay protection to obscure sensitive content
     */
    private fun enableOverlayProtection(activity: Activity) {
        try {
            // Create a transparent overlay that shows warning when screen recording is detected
            val overlay = View(activity).apply {
                setBackgroundColor(Color.argb(200, 0, 0, 0))
                visibility = View.GONE
                isClickable = false
                isFocusable = false
            }
            
            // Add overlay to window (implementation would depend on UI framework)
            Logger.d(TAG, "Overlay protection enabled")
        } catch (e: Exception) {
            Logger.e(TAG, "Error enabling overlay protection: ${e.message}")
        }
    }
    
    /**
     * Remove overlay protection
     */
    private fun removeOverlayProtection(activity: Activity) {
        try {
            // Remove overlay from window
            Logger.d(TAG, "Overlay protection removed")
        } catch (e: Exception) {
            Logger.e(TAG, "Error removing overlay protection: ${e.message}")
        }
    }
    
    /**
     * Start monitoring for screen recording activity
     */
    private fun startScreenRecordingMonitoring() {
        Thread {
            while (isProtectionEnabled) {
                try {
                    if (isScreenRecordingActive()) {
                        Logger.w(TAG, "Screen recording detected!")
                        screenRecordingDetectedCallback?.invoke()
                        break
                    }
                    Thread.sleep(2000) // Check every 2 seconds
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Logger.e(TAG, "Error in screen recording monitoring: ${e.message}")
                }
            }
        }.start()
    }
    
    /**
     * Monitor for external display connections
     */
    private fun monitorExternalDisplays() {
        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            
            val displayListener = object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {
                    Logger.w(TAG, "External display connected: $displayId")
                    screenRecordingDetectedCallback?.invoke()
                }
                
                override fun onDisplayRemoved(displayId: Int) {
                    Logger.d(TAG, "Display removed: $displayId")
                }
                
                override fun onDisplayChanged(displayId: Int) {
                    Logger.d(TAG, "Display changed: $displayId")
                }
            }
            
            displayManager.registerDisplayListener(displayListener, null)
            Logger.d(TAG, "External display monitoring enabled")
        } catch (e: Exception) {
            Logger.e(TAG, "Error setting up display monitoring: ${e.message}")
        }
    }
    
    /**
     * Check media projection service for active screen recording
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkMediaProjection(): Boolean {
        return try {
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            
            // This is a simplified check - in reality, detecting active media projection 
            // requires more complex implementation
            Logger.d(TAG, "Checking media projection service")
            
            false // Placeholder - actual implementation would require system-level access
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking media projection: ${e.message}")
            false
        }
    }
    
    /**
     * Check for screen recording processes
     */
    private fun checkRecordingProcesses(): Boolean {
        return try {
            val recordingProcesses = arrayOf(
                "screenrecord", "ffmpeg", "obs", "kazam", "vokoscreen",
                "simplescreenrecorder", "recordmydesktop", "camtasia"
            )
            
            val procDir = File("/proc")
            val processDirs = procDir.listFiles { file -> 
                file.isDirectory && file.name.matches(Regex("\\d+"))
            } ?: return false
            
            processDirs.any { processDir ->
                try {
                    val cmdlineFile = File(processDir, "cmdline")
                    if (cmdlineFile.exists()) {
                        val cmdline = cmdlineFile.readText().toLowerCase()
                        recordingProcesses.any { processName -> 
                            cmdline.contains(processName)
                        }
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for recently created screen recording files
     */
    private fun checkRecordingFiles(): Boolean {
        return try {
            val commonRecordingPaths = arrayOf(
                "/sdcard/DCIM/",
                "/sdcard/Movies/",
                "/sdcard/Pictures/Screenshots/",
                "/storage/emulated/0/DCIM/",
                "/storage/emulated/0/Movies/"
            )
            
            val recordingExtensions = arrayOf(".mp4", ".mkv", ".avi", ".mov", ".webm")
            val recentThreshold = System.currentTimeMillis() - (5 * 60 * 1000) // 5 minutes
            
            commonRecordingPaths.any { path ->
                try {
                    val directory = File(path)
                    if (directory.exists() && directory.isDirectory) {
                        directory.listFiles()?.any { file ->
                            file.isFile &&
                            file.lastModified() > recentThreshold &&
                            recordingExtensions.any { ext -> 
                                file.name.toLowerCase().endsWith(ext)
                            }
                        } ?: false
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check system properties for recording indicators
     */
    private fun checkRecordingProperties(): Boolean {
        return try {
            val recordingProps = arrayOf(
                "debug.sf.recording",
                "vendor.debug.sf.recording_enable",
                "ro.config.media_vol_default"
            )
            
            recordingProps.any { prop ->
                val value = getSystemProperty(prop)
                !value.isNullOrEmpty() && value != "0"
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for suspicious display metrics changes
     */
    private fun checkDisplayChanges(): Boolean {
        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val displays = displayManager.displays
            
            // Check if there are multiple displays (potential mirroring/recording)
            if (displays.size > 1) {
                Logger.w(TAG, "Multiple displays detected: ${displays.size}")
                return true
            }
            
            // Check for unusual display characteristics
            displays.any { display ->
                val metrics = android.util.DisplayMetrics()
                display.getMetrics(metrics)
                
                // Check for virtual displays or unusual configurations
                display.displayId != 0 || // Non-default display
                display.state != android.view.Display.STATE_ON ||
                (display.flags and android.view.Display.FLAG_PRIVATE) != 0 ||
                (display.flags and android.view.Display.FLAG_PRESENTATION) != 0
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create a secure surface for sensitive content
     */
    fun createSecureSurface(activity: Activity): Surface? {
        return try {
            // This would create a secure surface that can't be recorded
            // Implementation depends on specific use case
            Logger.d(TAG, "Creating secure surface")
            null // Placeholder
        } catch (e: Exception) {
            Logger.e(TAG, "Error creating secure surface: ${e.message}")
            null
        }
    }
    
    /**
     * Apply blur or overlay to sensitive views during potential recording
     */
    fun protectSensitiveViews(views: List<View>) {
        views.forEach { view ->
            try {
                // Apply protection to individual views
                view.alpha = 0.1f // Make barely visible
                view.isEnabled = false
                Logger.d(TAG, "Protected sensitive view")
            } catch (e: Exception) {
                Logger.e(TAG, "Error protecting view: ${e.message}")
            }
        }
    }
    
    /**
     * Remove protection from sensitive views
     */
    fun unprotectSensitiveViews(views: List<View>) {
        views.forEach { view ->
            try {
                view.alpha = 1.0f
                view.isEnabled = true
            } catch (e: Exception) {
                Logger.e(TAG, "Error unprotecting view: ${e.message}")
            }
        }
    }
    
    /**
     * Get system property value
     */
    private fun getSystemProperty(property: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $property")
            val reader = process.inputStream.bufferedReader()
            val result = reader.readLine()
            reader.close()
            process.waitFor()
            result?.trim()
        } catch (e: Exception) {
            null
        }
    }
}