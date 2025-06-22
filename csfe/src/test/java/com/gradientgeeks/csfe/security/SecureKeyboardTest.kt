package com.gradientgeeks.csfe.security

import android.content.Context
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for SecureKeyboard
 */
@RunWith(RobolectricTestRunner::class)
class SecureKeyboardTest {
    
    private lateinit var context: Context
    private lateinit var secureKeyboard: SecureKeyboard
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        secureKeyboard = SecureKeyboard(context)
    }
    
    @Test
    fun testKeyboardSecurityCheck() {
        // Test that keyboard security check executes without crashing
        val result = secureKeyboard.isSecureKeyboard()
        
        assertNotNull("Keyboard security result should not be null", result)
    }
    
    @Test
    fun testSecureEditTextConfiguration() {
        val editText = EditText(context)
        
        // Test securing regular text field
        secureKeyboard.secureEditText(editText, false)
        
        // Verify that input type was modified to disable suggestions
        assertTrue("Input type should include NO_SUGGESTIONS flag", 
            (editText.inputType and android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0)
    }
    
    @Test
    fun testSecurePasswordFieldConfiguration() {
        val editText = EditText(context)\n        \n        // Test securing password field\n        secureKeyboard.secureEditText(editText, true)\n        \n        // Verify password input type\n        assertTrue(\"Input type should be password type\", \n            (editText.inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0)\n        \n        // Verify long click is disabled\n        assertFalse(\"Long click should be disabled for password fields\", editText.isLongClickable)\n    }\n    \n    @Test\n    fun testPinGeneration() {\n        val pin1 = secureKeyboard.generateSecurePin(6)\n        val pin2 = secureKeyboard.generateSecurePin(6)\n        \n        assertEquals(\"PIN should be 6 digits\", 6, pin1.length)\n        assertEquals(\"PIN should be 6 digits\", 6, pin2.length)\n        assertNotEquals(\"PINs should be different\", pin1, pin2)\n        \n        // Verify PIN contains only digits\n        assertTrue(\"PIN should contain only digits\", pin1.all { it.isDigit() })\n    }\n    \n    @Test\n    fun testInputObfuscation() {\n        val input = \"password123\"\n        \n        val obfuscated = secureKeyboard.obfuscateInput(input, true)\n        assertEquals(\"Obfuscated input should show last char\", \"*********3\", obfuscated)\n        \n        val fullyObfuscated = secureKeyboard.obfuscateInput(input, false)\n        assertEquals(\"Fully obfuscated input should be all stars\", \"***********\", fullyObfuscated)\n        \n        val emptyObfuscated = secureKeyboard.obfuscateInput(\"\", true)\n        assertEquals(\"Empty input should return empty\", \"\", emptyObfuscated)\n    }\n    \n    @Test\n    fun testKeyboardRecommendations() {\n        val recommendations = secureKeyboard.getSecureKeyboardRecommendations()\n        \n        assertNotNull(\"Recommendations should not be null\", recommendations)\n        assertTrue(\"Should have at least one recommendation\", recommendations.isNotEmpty())\n        assertTrue(\"Should include Gboard\", recommendations.any { it.contains(\"Gboard\") })\n    }\n    \n    @Test\n    fun testEnabledInputMethods() {\n        val inputMethods = secureKeyboard.getEnabledInputMethods()\n        \n        assertNotNull(\"Input methods list should not be null\", inputMethods)\n        // In test environment, this might be empty, which is acceptable\n    }\n}