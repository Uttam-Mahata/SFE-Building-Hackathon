package com.app.sfe;

import android.text.InputFilter;

/**
 * Configuration class for secure text fields.
 * Specifies appearance and behavior of a secure input field.
 */
public class SFETextFieldConfig {
    private final String hint;
    private final int maxLength;
    private final InputFilter[] inputFilters;
    private final boolean maskInput;
    private final int securityLevel;
    
    private SFETextFieldConfig(Builder builder) {
        this.hint = builder.hint;
        this.maxLength = builder.maxLength;
        this.inputFilters = builder.inputFilters;
        this.maskInput = builder.maskInput;
        this.securityLevel = builder.securityLevel;
    }
    
    public String getHint() {
        return hint;
    }
    
    public int getMaxLength() {
        return maxLength;
    }
    
    public InputFilter[] getInputFilters() {
        return inputFilters;
    }
    
    public boolean isMaskInput() {
        return maskInput;
    }
    
    public int getSecurityLevel() {
        return securityLevel;
    }
    
    /**
     * Builder for SFETextFieldConfig.
     */
    public static class Builder {
        private String hint = "";
        private int maxLength = 100;
        private InputFilter[] inputFilters = new InputFilter[0];
        private boolean maskInput = false;
        private int securityLevel = 1; // Default security level
        
        public Builder setHint(String hint) {
            this.hint = hint;
            return this;
        }
        
        public Builder setMaxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }
        
        public Builder setInputFilters(InputFilter... inputFilters) {
            this.inputFilters = inputFilters;
            return this;
        }
        
        public Builder setMaskInput(boolean maskInput) {
            this.maskInput = maskInput;
            return this;
        }
        
        public Builder setSecurityLevel(int securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }
        
        public SFETextFieldConfig build() {
            return new SFETextFieldConfig(this);
        }
    }
}