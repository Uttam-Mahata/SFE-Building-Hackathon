package com.gradientgeeks.csfe.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for EmulatorDetector
 */
@RunWith(RobolectricTestRunner::class)
class EmulatorDetectorTest {
    
    private lateinit var context: Context
    private lateinit var emulatorDetector: EmulatorDetector
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        emulatorDetector = EmulatorDetector(context)
    }
    
    @Test
    fun testEmulatorDetectionExecution() {
        // Test that emulator detection executes without crashing
        val result = emulatorDetector.isEmulator()
        
        assertNotNull("Emulator detection result should not be null", result)
    }
    
    @Test
    fun testEmulatorDetectionInTestEnvironment() {
        // In Robolectric test environment, should typically detect as emulator-like
        val result = emulatorDetector.isEmulator()
        
        // We expect this to be true in test environment due to build characteristics
        assertTrue("Should detect test environment as emulator-like", result)
    }
    
    @Test
    fun testEmulatorDetectionConsistency() {
        // Test that multiple calls return consistent results
        val result1 = emulatorDetector.isEmulator()
        val result2 = emulatorDetector.isEmulator()
        
        assertEquals("Emulator detection should return consistent results", result1, result2)
    }
}