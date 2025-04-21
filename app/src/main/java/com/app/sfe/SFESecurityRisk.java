package com.app.sfe;

/**
 * Class representing a security risk identified during device security check.
 */
public class SFESecurityRisk {
    private final RiskType type;
    private final String description;
    private final RiskLevel level;
    
    /**
     * Creates a new security risk.
     * 
     * @param type Type of risk
     * @param description Description of the risk
     * @param level Severity level of the risk
     */
    public SFESecurityRisk(RiskType type, String description, RiskLevel level) {
        this.type = type;
        this.description = description;
        this.level = level;
    }
    
    public RiskType getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public RiskLevel getLevel() {
        return level;
    }
    
    /**
     * Enumeration of security risk types.
     */
    public enum RiskType {
        DEVICE_ROOTED,              // Device is rooted/jailbroken
        DEVELOPER_OPTIONS_ENABLED,  // Developer options are enabled
        NO_DEVICE_LOCK,             // Device lock is not set up
        DEBUGGING_TOOLS_INSTALLED,  // Debugging tools are installed
        APP_INTEGRITY_COMPROMISED,  // App signature verification failed
        EMULATOR_DETECTED,          // App is running in an emulator
        UNSAFE_WIFI,                // Connected to unsecured WiFi
        SCREEN_OVERLAY_DETECTED,    // Screen overlay detected
        UNTRUSTED_KEYBOARD,         // Using a third-party keyboard
        SECURITY_PATCH_OUTDATED     // Security patch level is outdated
    }
    
    /**
     * Enumeration of risk severity levels.
     */
    public enum RiskLevel {
        LOW,      // Low severity risk
        MEDIUM,   // Medium severity risk
        HIGH,     // High severity risk
        CRITICAL  // Critical severity risk
    }
}