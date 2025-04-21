package com.app.sfe;

/**
 * Exception class for SFE SDK errors.
 */
public class SFEException extends Exception {
    private final int errorCode;
    
    public SFEException(String message) {
        super(message);
        this.errorCode = 0;
    }
    
    public SFEException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SFEException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }
    
    public SFEException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}