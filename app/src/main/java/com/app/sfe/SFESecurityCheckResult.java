package com.app.sfe;

import java.util.Collections;
import java.util.List;

/**
 * Class containing the result of a device security check.
 */
public class SFESecurityCheckResult {
    private final boolean isDeviceSecure;
    private final List<SFESecurityRisk> securityRisks;
    
    /**
     * Creates a new security check result.
     * 
     * @param isDeviceSecure Whether the device passes security checks
     * @param securityRisks List of identified security risks
     */
    public SFESecurityCheckResult(boolean isDeviceSecure, List<SFESecurityRisk> securityRisks) {
        this.isDeviceSecure = isDeviceSecure;
        this.securityRisks = securityRisks != null 
            ? Collections.unmodifiableList(securityRisks) 
            : Collections.emptyList();
    }
    
    /**
     * Checks if the device is secure.
     * 
     * @return true if the device passes all security checks, false otherwise
     */
    public boolean isDeviceSecure() {
        return isDeviceSecure;
    }
    
    /**
     * Gets the list of security risks identified during the check.
     * 
     * @return Unmodifiable list of security risks
     */
    public List<SFESecurityRisk> getSecurityRisks() {
        return securityRisks;
    }
    
    /**
     * Checks if there are any high or critical risks.
     * 
     * @return true if high or critical risks were found, false otherwise
     */
    public boolean hasHighRisks() {
        for (SFESecurityRisk risk : securityRisks) {
            if (risk.getLevel() == SFESecurityRisk.RiskLevel.HIGH || 
                risk.getLevel() == SFESecurityRisk.RiskLevel.CRITICAL) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets a summary of the security check result.
     * 
     * @return String summarizing the security check result
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (isDeviceSecure) {
            summary.append("Device security check passed. No security risks detected.");
        } else {
            summary.append("Device security check failed. ")
                   .append(securityRisks.size())
                   .append(" security risks detected:");
            
            for (SFESecurityRisk risk : securityRisks) {
                summary.append("\n- ")
                       .append(risk.getDescription())
                       .append(" (")
                       .append(risk.getLevel())
                       .append(")");
            }
        }
        
        return summary.toString();
    }
}