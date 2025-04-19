package com.app.sfe.security;

/**
 * Class representing the result of SFE initialization.
 * Contains success status and error information if initialization failed.
 */
public class SFEInitResult {
    private final boolean success;
    private final String errorReason;
    private final int errorCode;

    private SFEInitResult(boolean success, String errorReason, int errorCode) {
        this.success = success;
        this.errorReason = errorReason;
        this.errorCode = errorCode;
    }

    public static SFEInitResult success() {
        return new SFEInitResult(true, null, 0);
    }

    public static SFEInitResult failure(String reason, int code) {
        return new SFEInitResult(false, reason, code);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public int getErrorCode() {
        return errorCode;
    }
}