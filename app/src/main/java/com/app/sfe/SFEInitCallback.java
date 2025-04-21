package com.app.sfe;

/**
 * Callback interface for SFE SDK initialization results.
 */
public interface SFEInitCallback {
    /**
     * Called when SDK initialization is successful
     */
    void onSuccess();
    
    /**
     * Called when SDK initialization fails
     * 
     * @param e Exception with details about the failure
     */
    void onError(SFEException e);
}