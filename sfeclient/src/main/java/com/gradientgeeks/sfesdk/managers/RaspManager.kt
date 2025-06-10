package com.gradientgeeks.sfesdk.managers

import android.content.Context
import com.gradientgeeks.sfesdk.models.SecurityAssessment
import com.gradientgeeks.sfesdk.models.SecurityThreat

/**
 * Enhanced Runtime Application Self-Protection (RASP) Manager
 * 
 * Provides comprehensive security checks, threat detection, and behavioral analysis
 * for production-ready financial applications
 */
interface RaspManager {
    
    /**
     * Check if device is rooted
     * Detection methods:
     * - Checks for su binaries and root management apps
     * - Verifies system integrity (test-keys, build properties)
     * - Detects Magisk, SuperSU, and other rooting tools
     */
    fun isDeviceRooted(): Boolean
    
    /**
     * Check if app has been tampered with
     * Compares current app signature with legitimate hash
     */
    fun isAppTampered(context: Context): Boolean
    
    /**
     * Check if debugger is attached
     * Monitors Debug.isDebuggerConnected() and TracerPid
     */
    fun isDebuggerAttached(): Boolean
    
    /**
     * Perform comprehensive security check of the device
     * Returns detailed device security information
     */
    suspend fun performComprehensiveSecurityCheck(context: Context): SecurityAssessment
    
    /**
     * Detect suspicious applications
     * Scans for malware, hacking tools, and security bypass apps
     */
    suspend fun detectSuspiciousApps(context: Context): Result<List<String>>
    
    /**
     * Check device integrity
     * Validates bootloader status, system partition integrity
     */
    suspend fun checkDeviceIntegrity(context: Context): Result<Map<String, String>>
    
    /**
     * Monitor for runtime threats
     * Detects hooking frameworks, runtime manipulation
     */
    suspend fun monitorRuntimeThreats(context: Context): Result<List<SecurityThreat>>
    
    /**
     * Validate app environment
     * Checks for emulators, virtual environments, analysis tools
     */
    suspend fun validateAppEnvironment(context: Context): Result<Boolean>
    
    /**
     * Behavioral analysis (if enabled)
     * Monitors user interaction patterns for anomalies
     */
    suspend fun performBehavioralAnalysis(context: Context): Result<Map<String, Any>>
    
    /**
     * Real-time threat monitoring
     * Continuous monitoring for security events
     */
    fun startRealTimeMonitoring(context: Context): Result<Unit>
    
    /**
     * Stop real-time monitoring
     */
    fun stopRealTimeMonitoring(): Result<Unit>
    
    /**
     * Check network security
     * Validates secure connections, detects network attacks
     */
    suspend fun checkNetworkSecurity(context: Context): Result<Map<String, Boolean>>
    
    /**
     * Screen recording and screenshot detection
     */
    fun isScreenCaptureActive(context: Context): Boolean
    
    /**
     * Accessibility service abuse detection
     */
    suspend fun detectAccessibilityAbuse(context: Context): Result<List<String>>
    
    /**
     * Anti-fraud checks specific to financial apps
     */
    suspend fun performAntifraudChecks(context: Context): Result<Map<String, Any>>
} 