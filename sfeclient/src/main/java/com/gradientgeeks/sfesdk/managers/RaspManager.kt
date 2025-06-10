package com.gradientgeeks.sfesdk.managers

import android.content.Context

/**
 * Runtime Application Self-Protection (RASP) Manager
 * 
 * Provides security checks to detect threats and tampering attempts
 */
interface RaspManager {
    
    /**
     * Check if device is rooted
     * Detection methods:
     * - Checks for su binaries
     * - Root management apps
     * - test-keys in Build.TAGS
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
} 