package com.app.sfe.security;

/**
 * Result class for SFE initialization operations.
 */
public class SFEInitResult {
    private final boolean success;
    private final int errorCode;
    private final String errorReason;
    
    /**
     * Creates a successful result.
     */
    public static SFEInitResult success() {
        return new SFEInitResult(true, 0, null);
    }
    
    /**
     * Creates a failed result with error details.
     * 
     * @param errorCode The error code
     * @param errorReason The reason for the error
     * @return A failure result
     */
    public static SFEInitResult failure(int errorCode, String errorReason) {
        return new SFEInitResult(false, errorCode, errorReason);
    }
    
    private SFEInitResult(boolean success, int errorCode, String errorReason) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorReason = errorReason;
    }
    
    /**
     * Checks if the operation was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Gets the error code for failed operations.
     * 
     * @return The error code, or 0 for successful operations
     */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the error reason for failed operations.
     * 
     * @return The error reason, or null for successful operations
     */
    public String getErrorReason() {
        return errorReason;
    }
}