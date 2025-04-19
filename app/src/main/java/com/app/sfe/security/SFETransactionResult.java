package com.app.sfe.security;

/**
 * Class representing the result of a secure financial transaction.
 * Contains transaction status, data, and error information if the transaction failed.
 */
public class SFETransactionResult<T> {
    private final boolean success;
    private final T transactionData;
    private final String errorMessage;
    private final int errorCode;

    private SFETransactionResult(boolean success, T transactionData, String errorMessage, int errorCode) {
        this.success = success;
        this.transactionData = transactionData;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static <T> SFETransactionResult<T> success(T data) {
        return new SFETransactionResult<>(true, data, null, 0);
    }

    public static <T> SFETransactionResult<T> failure(String errorMessage, int errorCode) {
        return new SFETransactionResult<>(false, null, errorMessage, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getTransactionData() {
        return transactionData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }
}