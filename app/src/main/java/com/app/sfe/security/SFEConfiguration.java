package com.app.sfe.security;

/**
 * Internal configuration class for the Secure Financial Environment core.
 * Used for configuring the internal security components.
 */
public class SFEConfiguration {
    private final String appId;
    private final SFEProviderType providerType;
    private final SFESecurityLevel securityLevel;
    private final boolean analyticsEnabled;
    private final long transactionTimeout;
    
    private SFEConfiguration(Builder builder) {
        this.appId = builder.appId;
        this.providerType = builder.providerType;
        this.securityLevel = builder.securityLevel;
        this.analyticsEnabled = builder.analyticsEnabled;
        this.transactionTimeout = builder.transactionTimeout;
    }
    
    public String getAppId() {
        return appId;
    }
    
    public SFEProviderType getProviderType() {
        return providerType;
    }
    
    public SFESecurityLevel getSecurityLevel() {
        return securityLevel;
    }
    
    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }
    
    public long getTransactionTimeout() {
        return transactionTimeout;
    }
    
    /**
     * Builder for SFEConfiguration.
     */
    public static class Builder {
        private String appId;
        private SFEProviderType providerType = SFEProviderType.FINTECH;
        private SFESecurityLevel securityLevel = SFESecurityLevel.STANDARD;
        private boolean analyticsEnabled = true;
        private long transactionTimeout = 30000; // 30 seconds default
        
        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }
        
        public Builder setProviderType(SFEProviderType providerType) {
            this.providerType = providerType;
            return this;
        }
        
        public Builder setCustomSecurityLevel(SFESecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }
        
        public Builder setAnalyticsEnabled(boolean analyticsEnabled) {
            this.analyticsEnabled = analyticsEnabled;
            return this;
        }
        
        public Builder setTransactionTimeout(long transactionTimeout) {
            this.transactionTimeout = transactionTimeout;
            return this;
        }
        
        public SFEConfiguration build() {
            if (appId == null || appId.isEmpty()) {
                throw new IllegalStateException("Application ID is required for SFE initialization");
            }
            return new SFEConfiguration(this);
        }
    }
}