package com.app.sfe;

/**
 * Callback interface for biometric authentication results.
 */
public interface SFEBiometricCallback {
    /**
     * Called when authentication is successful.
     */
    void onSuccess();
    
    /**
     * Called when authentication fails with an error.
     * 
     * @param error Information about the error
     */
    void onError(SFEBiometric.SFEBiometricError error);
    
    /**
     * Called when the user cancels authentication.
     */
    void onCancel();
}