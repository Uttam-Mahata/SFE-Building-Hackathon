package com.app.sfe.security;

import java.util.HashMap;
import java.util.Map;

/**
 * Result class for secure financial transactions.
 * Contains transaction result data and metadata.
 */
public class SFETransactionResult {
    private final boolean success;
    private final String transactionId;
    private final int errorCode;
    private final String errorMessage;
    private final Map<String, Object> metadata;
    
    private SFETransactionResult(Builder builder) {
        this.success = builder.success;
        this.transactionId = builder.transactionId;
        this.errorCode = builder.errorCode;
        this.errorMessage = builder.errorMessage;
        this.metadata = new HashMap<>(builder.metadata);
    }
    
    /**
     * Checks if the transaction was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Gets the transaction ID.
     * 
     * @return The transaction ID, or null if transaction failed
     */
    public String getTransactionId() {
        return transactionId;
    }
    
    /**
     * Gets the error code for failed transactions.
     * 
     * @return The error code, or 0 for successful transactions
     */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the error message for failed transactions.
     * 
     * @return The error message, or null for successful transactions
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Gets a metadata value by key.
     * 
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Gets all metadata as a map.
     * 
     * @return Unmodifiable map of all metadata
     */
    public Map<String, Object> getAllMetadata() {
        return new HashMap<>(metadata);
    }
    
    /**
     * Builder for SFETransactionResult.
     */
    public static class Builder {
        private boolean success = false;
        private String transactionId;
        private int errorCode = 0;
        private String errorMessage;
        private final Map<String, Object> metadata = new HashMap<>();
        
        /**
         * Sets the transaction as successful.
         * 
         * @param transactionId The transaction ID
         * @return This builder
         */
        public Builder success(String transactionId) {
            this.success = true;
            this.transactionId = transactionId;
            return this;
        }
        
        /**
         * Sets the transaction as failed.
         * 
         * @param errorCode The error code
         * @param errorMessage The error message
         * @return This builder
         */
        public Builder failure(int errorCode, String errorMessage) {
            this.success = false;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            return this;
        }
        
        /**
         * Adds a metadata value.
         * 
         * @param key The metadata key
         * @param value The metadata value
         * @return This builder
         */
        public Builder addMetadata(String key, Object value) {
            if (key != null && value != null) {
                metadata.put(key, value);
            }
            return this;
        }
        
        /**
         * Builds the transaction result.
         * 
         * @return The transaction result
         */
        public SFETransactionResult build() {
            return new SFETransactionResult(this);
        }
    }
}