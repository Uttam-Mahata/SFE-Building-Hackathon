package com.app.sfe;

/**
 * Configuration class for biometric authentication prompts.
 * Used to customize the appearance and behavior of biometric authentication.
 */
public class SFEBiometricPrompt {
    private final String title;
    private final String subtitle;
    private final String description;
    private final String negativeButtonText;
    
    private SFEBiometricPrompt(Builder builder) {
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.description = builder.description;
        this.negativeButtonText = builder.negativeButtonText;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getNegativeButtonText() {
        return negativeButtonText;
    }
    
    /**
     * Builder for SFEBiometricPrompt.
     */
    public static class Builder {
        private String title;
        private String subtitle;
        private String description;
        private String negativeButtonText = "Cancel";
        
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        
        public Builder setSubtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }
        
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder setNegativeButtonText(String negativeButtonText) {
            this.negativeButtonText = negativeButtonText;
            return this;
        }
        
        public SFEBiometricPrompt build() {
            if (title == null || title.isEmpty()) {
                throw new IllegalStateException("Title is required for biometric prompt");
            }
            return new SFEBiometricPrompt(this);
        }
    }
}