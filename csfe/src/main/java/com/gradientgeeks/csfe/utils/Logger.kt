package com.gradientgeeks.csfe.utils

import android.util.Log
import com.gradientgeeks.csfe.config.LogLevel

/**
 * Logger utility for the SDK.
 */
object Logger {
    private const val SDK_TAG = "SFE_SDK"
    private var logLevel = LogLevel.ERROR
    
    /**
     * Initialize the logger with the configured log level.
     */
    fun init(level: LogLevel) {
        this.logLevel = level
    }
    
    /**
     * Log a debug message.
     */
    fun d(tag: String, message: String) {
        if (shouldLog(LogLevel.DEBUG)) {
            Log.d("$SDK_TAG:$tag", message)
        }
    }
    
    /**
     * Log an info message.
     */
    fun i(tag: String, message: String) {
        if (shouldLog(LogLevel.INFO)) {
            Log.i("$SDK_TAG:$tag", message)
        }
    }
    
    /**
     * Log a warning message.
     */
    fun w(tag: String, message: String) {
        if (shouldLog(LogLevel.WARNING)) {
            Log.w("$SDK_TAG:$tag", message)
        }
    }
    
    /**
     * Log an error message.
     */
    fun e(tag: String, message: String) {
        if (shouldLog(LogLevel.ERROR)) {
            Log.e("$SDK_TAG:$tag", message)
        }
    }
    
    /**
     * Check if a message with the given level should be logged.
     */
    private fun shouldLog(messageLevel: LogLevel): Boolean {
        if (logLevel == LogLevel.NONE) {
            return false
        }
        
        return when (logLevel) {
            LogLevel.DEBUG -> true // Log everything
            LogLevel.INFO -> messageLevel != LogLevel.DEBUG
            LogLevel.WARNING -> messageLevel != LogLevel.DEBUG && messageLevel != LogLevel.INFO
            LogLevel.ERROR -> messageLevel == LogLevel.ERROR
            LogLevel.NONE -> false
        }
    }
}
