package com.gradientgeeks.csfe.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for RootDetector
 */
@RunWith(RobolectricTestRunner::class)
class RootDetectorTest {
    
    private lateinit var context: Context
    private lateinit var rootDetector: RootDetector
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        rootDetector = RootDetector(context)
    }
    
    @Test
    fun testRootDetectionExecution() {
        // Test that root detection executes without crashing
        val result = rootDetector.isRooted()
        
        // In test environment, should typically return false
        // but we're mainly testing that it doesn't crash
        assertNotNull("Root detection result should not be null", result)
    }
    
    @Test
    fun testRootDetectionConsistency() {
        // Test that multiple calls return consistent results
        val result1 = rootDetector.isRooted()
        val result2 = rootDetector.isRooted()
        
        assertEquals("Root detection should return consistent results", result1, result2)
    }
}