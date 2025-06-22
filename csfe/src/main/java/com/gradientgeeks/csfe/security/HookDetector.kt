package com.gradientgeeks.csfe.security

import android.content.Context
import android.content.pm.PackageManager
import com.gradientgeeks.csfe.utils.Logger
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.reflect.Method
import java.net.ServerSocket

/**
 * Hook detection for runtime manipulation tools like Frida, Xposed, Substrate, etc.
 */
class HookDetector(private val context: Context) {
    private val TAG = "HookDetector"
    
    /**
     * Comprehensive hook detection using multiple techniques
     */
    fun isHooked(): Boolean {
        Logger.d(TAG, "Starting comprehensive hook detection")
        
        val detectionResults = mutableListOf<Pair<String, Boolean>>()
        
        // Method 1: Check for Xposed framework
        detectionResults.add("XPOSED_FRAMEWORK" to checkXposedFramework())
        
        // Method 2: Check for Frida
        detectionResults.add("FRIDA_DETECTION" to checkFrida())
        
        // Method 3: Check for Substrate
        detectionResults.add("SUBSTRATE_DETECTION" to checkSubstrate())
        
        // Method 4: Check for hook-related processes
        detectionResults.add("HOOK_PROCESSES" to checkHookProcesses())
        
        // Method 5: Check for suspicious loaded libraries
        detectionResults.add("SUSPICIOUS_LIBRARIES" to checkSuspiciousLibraries())
        
        // Method 6: Check method hooking
        detectionResults.add("METHOD_HOOKING" to checkMethodHooking())
        
        // Method 7: Check for network-based hooks (Frida server)
        detectionResults.add("NETWORK_HOOKS" to checkNetworkHooks())
        
        // Method 8: Check environment for hook artifacts
        detectionResults.add("HOOK_ARTIFACTS" to checkHookArtifacts())
        
        // Log results for debugging
        detectionResults.forEach { (method, result) ->
            Logger.d(TAG, "Hook detection method $method: $result")
        }
        
        // Return true if any method detected hooks
        return detectionResults.any { it.second }
    }
    
    /**
     * Detect Xposed Framework
     */
    private fun checkXposedFramework(): Boolean {
        return try {
            // Method 1: Check for Xposed installer
            val xposedPackages = arrayOf(
                "de.robv.android.xposed.installer",
                "com.solohsu.android.edxp.manager",
                "org.meowcat.edxposed.manager",
                "me.weishu.exp"
            )
            
            val packageManager = context.packageManager
            val hasXposedInstaller = xposedPackages.any { packageName ->
                try {
                    packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                    true
                } catch (e: PackageManager.NameNotFoundException) {
                    false
                }
            }
            
            // Method 2: Check for Xposed bridge
            val hasXposedBridge = try {
                Class.forName("de.robv.android.xposed.XposedBridge")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
            
            // Method 3: Check for Xposed helpers
            val hasXposedHelpers = try {
                Class.forName("de.robv.android.xposed.XposedHelpers")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
            
            // Method 4: Check system property
            val xposedProp = getSystemProperty("ro.xposed")
            
            hasXposedInstaller || hasXposedBridge || hasXposedHelpers || xposedProp == "1"
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Detect Frida framework
     */
    private fun checkFrida(): Boolean {
        return try {
            // Method 1: Check for Frida server ports
            val fridaPorts = arrayOf(27042, 27043, 27044, 27045)
            val hasOpenFridaPort = fridaPorts.any { port ->
                try {
                    ServerSocket(port).use { false } // If we can bind, port is free
                } catch (e: Exception) {
                    true // Port is in use, possibly by Frida
                }
            }
            
            // Method 2: Check for Frida-related files
            val fridaFiles = arrayOf(
                "/data/local/tmp/frida-server",
                "/data/local/tmp/re.frida.server",
                "/system/bin/frida-server",
                "/system/xbin/frida-server",
                "/data/local/tmp/frida"
            )
            
            val hasFridaFiles = fridaFiles.any { path ->
                try {
                    File(path).exists()
                } catch (e: Exception) {
                    false
                }
            }
            
            // Method 3: Check for Frida in running processes
            val hasFridaProcess = checkProcessNames(arrayOf("frida-server", "frida", "gum-js-loop"))
            
            // Method 4: Check for Frida libraries in memory
            val hasFridaLibs = checkLoadedLibraries(arrayOf("frida", "gadget"))
            
            // Method 5: Check for Frida environment
            val fridaEnv = System.getenv("FRIDA_VERSION")
            
            hasOpenFridaPort || hasFridaFiles || hasFridaProcess || hasFridaLibs || !fridaEnv.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Detect Substrate framework (Cydia Substrate)
     */
    private fun checkSubstrate(): Boolean {
        return try {
            // Method 1: Check for Substrate classes
            val hasSubstrateClasses = try {
                Class.forName("com.saurik.substrate.MS")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
            
            // Method 2: Check for Substrate libraries
            val substrateLibs = arrayOf(
                "/system/lib/libsubstrate.so",
                "/system/lib64/libsubstrate.so",
                "/data/data/com.saurik.substrate/files/MSHookFunction"
            )
            
            val hasSubstrateFiles = substrateLibs.any { path ->
                try {
                    File(path).exists()
                } catch (e: Exception) {
                    false
                }
            }
            
            // Method 3: Check for Substrate in loaded libraries
            val hasSubstrateInMemory = checkLoadedLibraries(arrayOf("substrate", "mshook"))
            
            hasSubstrateClasses || hasSubstrateFiles || hasSubstrateInMemory
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for hook-related processes
     */
    private fun checkHookProcesses(): Boolean {
        val hookProcesses = arrayOf(
            "frida-server", "frida", "gum-js-loop", "gmain",
            "gdbus", "gjs", "node", "objection"
        )
        
        return checkProcessNames(hookProcesses)
    }
    
    /**
     * Check for suspicious loaded libraries
     */
    private fun checkSuspiciousLibraries(): Boolean {
        val suspiciousLibs = arrayOf(
            "frida", "gadget", "xposed", "substrate", "libhook", 
            "cydia", "hookzz", "dobby", "whale"
        )
        
        return checkLoadedLibraries(suspiciousLibs)
    }
    
    /**
     * Check for method hooking by testing known methods
     */
    private fun checkMethodHooking(): Boolean {
        return try {
            // Test if critical system methods are hooked
            val systemClass = System::class.java
            val exitMethod = systemClass.getMethod("exit", Int::class.javaPrimitiveType)
            
            // Check method signature - hooked methods often have modified signatures
            val methodName = exitMethod.name
            val isNativeMethod = exitMethod.modifiers and java.lang.reflect.Modifier.NATIVE != 0
            
            // Hooked methods might not be native when they should be
            if (methodName == "exit" && !isNativeMethod) {
                Logger.w(TAG, "System.exit method appears to be hooked (not native)")
                return true
            }
            
            // Check for unusual method behavior
            checkMethodIntegrity()
        } catch (e: Exception) {
            Logger.e(TAG, "Error checking method hooking: ${e.message}")
            false
        }
    }
    
    /**
     * Check for network-based hooks (Frida server, etc.)
     */
    private fun checkNetworkHooks(): Boolean {
        return try {
            // Check for listening sockets that might be hook frameworks
            val netstatOutput = executeCommand("netstat -an")
            val suspiciousConnections = arrayOf(
                ":27042", ":27043", ":27044", ":27045", // Frida default ports
                ":8080", ":8443" // Common proxy/hook ports
            )
            
            suspiciousConnections.any { port ->
                netstatOutput.contains(port)
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check environment for hook artifacts
     */
    private fun checkHookArtifacts(): Boolean {
        return try {
            // Check for hook-related environment variables
            val hookEnvVars = arrayOf(
                "FRIDA_VERSION", "XPOSED_BRIDGE_VERSION", "SUBSTRATE_ROOT"
            )
            
            val hasHookEnvVars = hookEnvVars.any { envVar ->
                !System.getenv(envVar).isNullOrEmpty()
            }
            
            // Check for hook-related system properties
            val hookProps = arrayOf(
                "ro.xposed", "persist.sys.substrate", "ro.frida"
            )
            
            val hasHookProps = hookProps.any { prop ->
                !getSystemProperty(prop).isNullOrEmpty()
            }
            
            hasHookEnvVars || hasHookProps
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if specific process names are running
     */
    private fun checkProcessNames(processNames: Array<String>): Boolean {
        return try {
            val procDir = File("/proc")
            val processDirs = procDir.listFiles { file -> 
                file.isDirectory && file.name.matches(Regex("\\d+"))
            } ?: return false
            
            processDirs.any { processDir ->
                try {
                    val cmdlineFile = File(processDir, "cmdline")
                    if (cmdlineFile.exists()) {
                        val cmdline = cmdlineFile.readText().toLowerCase()
                        processNames.any { processName -> 
                            cmdline.contains(processName.toLowerCase())
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
     * Check if suspicious libraries are loaded
     */
    private fun checkLoadedLibraries(libraryNames: Array<String>): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            
            BufferedReader(FileReader(mapsFile)).use { reader ->
                reader.lineSequence().any { line ->
                    libraryNames.any { libName ->
                        line.toLowerCase().contains(libName.toLowerCase())
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check method integrity by testing behavior
     */
    private fun checkMethodIntegrity(): Boolean {
        return try {
            // Test that system methods behave as expected
            val currentTime1 = System.currentTimeMillis()
            Thread.sleep(1) // Small delay
            val currentTime2 = System.currentTimeMillis()
            
            // If time doesn't advance, might be hooked
            if (currentTime2 <= currentTime1) {
                Logger.w(TAG, "System.currentTimeMillis() appears to be hooked")
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Execute system command and return output
     */
    private fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
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