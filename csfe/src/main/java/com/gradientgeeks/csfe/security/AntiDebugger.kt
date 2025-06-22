package com.gradientgeeks.csfe.security

import android.content.Context
import android.os.Debug
import com.gradientgeeks.csfe.utils.Logger
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.concurrent.thread

/**
 * Anti-debugging techniques to detect and prevent debugging attempts
 */
class AntiDebugger(private val context: Context) {
    private val TAG = "AntiDebugger"
    private var isMonitoring = false
    private var debuggerDetectedCallback: (() -> Unit)? = null
    
    /**
     * Start continuous monitoring for debugging attempts
     */
    fun startMonitoring(onDebuggerDetected: (() -> Unit)? = null) {
        if (isMonitoring) {
            Logger.w(TAG, "Monitoring already active")
            return
        }
        
        debuggerDetectedCallback = onDebuggerDetected
        isMonitoring = true
        
        Logger.d(TAG, "Starting anti-debugging monitoring")
        
        // Start background thread for continuous monitoring
        thread(name = "AntiDebugMonitor") {
            monitoringLoop()
        }
        
        // Perform initial detection
        performDetection()
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        isMonitoring = false
        Logger.d(TAG, "Stopped anti-debugging monitoring")
    }
    
    /**
     * Perform comprehensive debugging detection
     */
    fun isDebuggerDetected(): Boolean {
        Logger.d(TAG, "Starting comprehensive debugger detection")
        
        val detectionResults = mutableListOf<Pair<String, Boolean>>()
        
        // Method 1: Check if debugger is connected
        detectionResults.add("DEBUGGER_CONNECTED" to checkDebuggerConnected())
        
        // Method 2: Check tracer PID in /proc/self/status
        detectionResults.add("TRACER_PID" to checkTracerPid())
        
        // Method 3: Check debug flags
        detectionResults.add("DEBUG_FLAGS" to checkDebugFlags())
        
        // Method 4: Check for debugging tools processes
        detectionResults.add("DEBUG_PROCESSES" to checkDebugProcesses())
        
        // Method 5: Check timing attacks (debugger slows execution)
        detectionResults.add("TIMING_ATTACK" to checkTimingAttack())
        
        // Method 6: Check for JDWP (Java Debug Wire Protocol)
        detectionResults.add("JDWP_CHECK" to checkJdwp())
        
        // Method 7: Check debug environment variables
        detectionResults.add("DEBUG_ENV_VARS" to checkDebugEnvironmentVariables())
        
        // Log results for debugging
        detectionResults.forEach { (method, result) ->
            Logger.d(TAG, "Debug detection method $method: $result")
        }
        
        // Return true if any method detected debugging
        return detectionResults.any { it.second }
    }
    
    /**
     * Monitoring loop that runs in background thread
     */
    private fun monitoringLoop() {
        while (isMonitoring) {
            try {
                if (performDetection()) {
                    Logger.w(TAG, "Debugger detected during monitoring!")
                    debuggerDetectedCallback?.invoke()
                    break // Stop monitoring after detection
                }
                
                // Sleep for a short interval before next check
                Thread.sleep(1000) // Check every second
            } catch (e: InterruptedException) {
                Logger.d(TAG, "Monitoring interrupted")
                break
            } catch (e: Exception) {
                Logger.e(TAG, "Error in monitoring loop: ${e.message}")
            }
        }
    }
    
    /**
     * Perform detection and return result
     */
    private fun performDetection(): Boolean {
        return isDebuggerDetected()
    }
    
    /**
     * Check if debugger is connected using Android Debug API
     */
    private fun checkDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected()
    }
    
    /**
     * Check tracer PID in /proc/self/status
     */
    private fun checkTracerPid(): Boolean {
        return try {
            val statusFile = File("/proc/self/status")
            if (!statusFile.exists()) return false
            
            BufferedReader(FileReader(statusFile)).use { reader ->
                reader.lineSequence().forEach { line ->
                    if (line.startsWith("TracerPid:")) {
                        val tracerPid = line.substringAfter("TracerPid:").trim().toIntOrNull()
                        return tracerPid != null && tracerPid != 0
                    }
                }
            }
            false
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking tracer PID: ${e.message}")
            false
        }
    }
    
    /**
     * Check various debug flags and properties
     */
    private fun checkDebugFlags(): Boolean {
        return try {
            // Check if debugging is enabled in build
            val isDebuggable = context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
            
            // Check debug system properties
            val debugProps = listOf(
                getSystemProperty("ro.debuggable"),
                getSystemProperty("ro.secure")
            )
            
            isDebuggable || 
            debugProps[0] == "1" || // ro.debuggable = 1
            debugProps[1] == "0"    // ro.secure = 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for known debugging tool processes
     */
    private fun checkDebugProcesses(): Boolean {
        return try {
            // Read /proc/*/cmdline to find debugging processes
            val procDir = File("/proc")
            val processDirs = procDir.listFiles { file -> 
                file.isDirectory && file.name.matches(Regex("\\d+"))
            } ?: return false
            
            val debugTools = listOf(
                "gdb", "lldb", "strace", "ltrace", "gdbserver", 
                "frida", "objection", "r2", "radare2"
            )
            
            processDirs.any { processDir ->
                try {
                    val cmdlineFile = File(processDir, "cmdline")
                    if (cmdlineFile.exists()) {
                        val cmdline = cmdlineFile.readText().toLowerCase()
                        debugTools.any { tool -> cmdline.contains(tool) }
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
     * Timing attack to detect if execution is being slowed by debugger
     */
    private fun checkTimingAttack(): Boolean {
        return try {
            val iterations = 1000000
            val startTime = System.nanoTime()
            
            // Perform some computation
            var result = 0
            for (i in 0 until iterations) {
                result += i * 2
            }
            
            val endTime = System.nanoTime()
            val executionTime = endTime - startTime
            
            // If execution took too long, might be debugger interference
            // Adjust threshold based on expected performance
            val threshold = 50_000_000L // 50ms in nanoseconds
            
            Logger.d(TAG, "Timing check: ${executionTime}ns (threshold: ${threshold}ns)")
            executionTime > threshold
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for JDWP (Java Debug Wire Protocol)
     */
    private fun checkJdwp(): Boolean {
        return try {
            // Check if JDWP is enabled by looking at system properties
            val jdwpProps = listOf(
                getSystemProperty("debug.jdwp.enabled"),
                getSystemProperty("debug.jdwp.port"),
                getSystemProperty("ro.debuggable")
            )
            
            jdwpProps[0] == "1" || // JDWP enabled
            !jdwpProps[1].isNullOrEmpty() || // JDWP port set
            jdwpProps[2] == "1" // Debuggable build
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check debug-related environment variables
     */
    private fun checkDebugEnvironmentVariables(): Boolean {
        return try {
            val debugEnvVars = mapOf(
                "JAVA_TOOL_OPTIONS" to null,
                "_JAVA_OPTIONS" to null,
                "ANDROID_DEBUG" to "1"
            )
            
            debugEnvVars.any { (envVar, expectedValue) ->
                val value = System.getenv(envVar)
                if (expectedValue == null) {
                    !value.isNullOrEmpty()
                } else {
                    value == expectedValue
                }
            }
        } catch (e: Exception) {
            false
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
    
    /**
     * Anti-debugging technique: Detect and crash on ptrace attach
     */
    fun enablePtraceProtection() {
        try {
            // This is a native technique that would require JNI
            // For demo purposes, we'll simulate the concept
            Logger.d(TAG, "Ptrace protection enabled (simulated)")
            
            thread(name = "PtraceProtection") {
                while (isMonitoring) {
                    if (checkTracerPid()) {
                        Logger.w(TAG, "Ptrace attach detected!")
                        debuggerDetectedCallback?.invoke()
                        break
                    }
                    Thread.sleep(500)
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error enabling ptrace protection: ${e.message}")
        }
    }
}