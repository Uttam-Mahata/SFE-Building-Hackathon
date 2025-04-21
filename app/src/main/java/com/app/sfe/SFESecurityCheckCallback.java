package com.app.sfe;

/**
 * Callback interface for security check results.
 */
public interface SFESecurityCheckCallback {
    /**
     * Called when the security check is complete.
     * 
     * @param result The result of the security check
     */
    void onResult(SFESecurityCheckResult result);
}