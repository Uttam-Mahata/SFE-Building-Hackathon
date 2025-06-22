package com.gradientgeeks.csfe.security

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.gradientgeeks.csfe.utils.Logger
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Advanced emulator detection using multiple detection techniques
 */
class EmulatorDetector(private val context: Context) {
    private val TAG = "EmulatorDetector"
    
    /**
     * Comprehensive emulator detection using multiple methods
     */
    fun isEmulator(): Boolean {
        Logger.d(TAG, "Starting comprehensive emulator detection")
        
        val detectionResults = mutableListOf<Pair<String, Boolean>>()
        
        // Method 1: Check build characteristics
        detectionResults.add("BUILD_CHARACTERISTICS" to checkBuildCharacteristics())
        
        // Method 2: Check hardware characteristics
        detectionResults.add("HARDWARE_CHARACTERISTICS" to checkHardwareCharacteristics())
        
        // Method 3: Check telephony features
        detectionResults.add("TELEPHONY_FEATURES" to checkTelephonyFeatures())
        
        // Method 4: Check sensor availability
        detectionResults.add("SENSOR_AVAILABILITY" to checkSensorAvailability())
        
        // Method 5: Check emulator-specific files
        detectionResults.add("EMULATOR_FILES" to checkEmulatorFiles())
        
        // Method 6: Check network characteristics
        detectionResults.add("NETWORK_CHARACTERISTICS" to checkNetworkCharacteristics())
        
        // Method 7: Check CPU characteristics
        detectionResults.add("CPU_CHARACTERISTICS" to checkCpuCharacteristics())
        
        // Method 8: Check specific emulator signatures
        detectionResults.add("EMULATOR_SIGNATURES" to checkEmulatorSignatures())
        
        // Method 9: Check device ID patterns
        detectionResults.add("DEVICE_ID_PATTERNS" to checkDeviceIdPatterns())
        
        // Log results for debugging
        detectionResults.forEach { (method, result) ->
            Logger.d(TAG, "Emulator detection method $method: $result")
        }
        
        // Return true if multiple methods detect emulator (reduce false positives)
        val detectionCount = detectionResults.count { it.second }
        Logger.d(TAG, "Emulator detection methods triggered: $detectionCount/${detectionResults.size}")
        
        return detectionCount >= 3 // Require at least 3 positive detections
    }
    
    /**
     * Check build characteristics typical of emulators
     */
    private fun checkBuildCharacteristics(): Boolean {
        val suspiciousValues = listOf(
            Build.FINGERPRINT.contains("generic"),
            Build.FINGERPRINT.contains("unknown"),
            Build.MODEL.contains("google_sdk"),
            Build.MODEL.contains("Emulator"),
            Build.MODEL.contains("Android SDK"),
            Build.MANUFACTURER.contains("Genymotion"),
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"),
            Build.PRODUCT.contains("sdk"),
            Build.PRODUCT.contains("vbox"),
            Build.PRODUCT.contains("emulator"),
            Build.PRODUCT.contains("simulator")
        )
        
        return suspiciousValues.any { it }
    }
    
    /**
     * Check hardware characteristics
     */
    private fun checkHardwareCharacteristics(): Boolean {
        val suspiciousHardware = listOf(
            Build.HARDWARE.contains("goldfish"),
            Build.HARDWARE.contains("vbox"),
            Build.HARDWARE.contains("nox"),
            Build.HARDWARE.contains("ttVM_hdragon"),
            Build.BOARD.toLowerCase(Locale.ROOT).contains("nox"),
            Build.BOOTLOADER.toLowerCase(Locale.ROOT).contains("nox")
        )
        
        return suspiciousHardware.any { it }
    }
    
    /**
     * Check telephony features
     */
    private fun checkTelephonyFeatures(): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        
        return telephonyManager?.run {
            val networkOperatorName = networkOperatorName?.toLowerCase(Locale.ROOT) ?: ""
            val simOperatorName = simOperatorName?.toLowerCase(Locale.ROOT) ?: ""
            
            // Emulators often have specific operator names or no SIM
            networkOperatorName.contains("android") ||
            simOperatorName.contains("android") ||
            networkOperatorName.isEmpty() ||
            simOperatorName.isEmpty() ||
            deviceId == null ||
            deviceId == "000000000000000" // Common emulator IMEI
        } ?: false
    }
    
    /**
     * Check sensor availability (emulators often lack many sensors)
     */
    private fun checkSensorAvailability(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        
        return sensorManager?.run {
            val criticalSensors = listOf(
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_MAGNETIC_FIELD,
                Sensor.TYPE_LIGHT,
                Sensor.TYPE_PROXIMITY
            )
            
            val availableSensors = criticalSensors.count { sensorType ->
                getDefaultSensor(sensorType) != null
            }
            
            // If less than 2 critical sensors are available, likely an emulator
            availableSensors < 2
        } ?: true
    }
    
    /**
     * Check for emulator-specific files and directories
     */
    private fun checkEmulatorFiles(): Boolean {
        val emulatorFiles = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd"
        )
        
        return emulatorFiles.any { path ->
            try {
                File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Check network characteristics
     */
    private fun checkNetworkCharacteristics(): Boolean {
        return try {
            // Check for common emulator IP ranges
            val process = Runtime.getRuntime().exec("netcfg")
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()
            process.waitFor()
            
            output.contains("10.0.2.") || // Default Android emulator network
            output.contains("192.168.56.") || // VirtualBox network
            output.contains("192.168.57.") // Genymotion network
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check CPU characteristics
     */
    private fun checkCpuCharacteristics(): Boolean {
        return try {
            val cpuInfo = File("/proc/cpuinfo")
            if (cpuInfo.exists()) {
                val content = FileInputStream(cpuInfo).bufferedReader().readText().toLowerCase(Locale.ROOT)
                content.contains("intel") && content.contains("translation") ||
                content.contains("qemu") ||
                content.contains("virtual")
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for specific emulator signatures
     */
    private fun checkEmulatorSignatures(): Boolean {
        // Check system properties for emulator signatures
        val emulatorProps = mapOf(
            "ro.kernel.qemu" to "1",
            "ro.kernel.android.qemud" to null, // Just existence
            "ro.hardware" to "goldfish",
            "ro.hardware" to "vbox86",
            "ro.product.device" to "generic",
            "ro.product.model" to "sdk"
        )
        
        return emulatorProps.any { (prop, expectedValue) ->
            try {
                val value = getSystemProperty(prop)
                if (expectedValue == null) {
                    !value.isNullOrEmpty()
                } else {
                    value == expectedValue
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Check device ID patterns common in emulators  
     */
    private fun checkDeviceIdPatterns(): Boolean {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        
        val suspiciousIds = arrayOf(
            "9774d56d682e549c", // Default Android emulator
            "000000000000000",  // Common default
            "123456789abcdef",  // Generic pattern
            null,               // Missing ID
            ""                  // Empty ID
        )
        
        return suspiciousIds.contains(androidId)
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