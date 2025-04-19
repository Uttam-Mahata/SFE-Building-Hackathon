package com.app.sfe.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for initializing the Secure Financial Environment.
 * Contains all the settings and parameters required for SFE operation.
 */
public class SFEConfiguration {
    private final String appId;
    private final SFEProviderType providerType;
    private final SFESecurityLevel securityLevel;
    private final List<String> allowedAPIs;
    private final boolean analyticsEnabled;
    private final long transactionTimeout;

    private SFEConfiguration(Builder builder) {
        this.appId = builder.appId;
        this.providerType = builder.providerType;
        this.securityLevel = builder.securityLevel;
        this.allowedAPIs = builder.allowedAPIs;
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

    public List<String> getAllowedAPIs() {
        return allowedAPIs;
    }

    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }

    public long getTransactionTimeout() {
        return transactionTimeout;
    }

    /**
     * Builder class for SFEConfiguration to enable fluent interface pattern.
     */
    public static class Builder {
        private String appId;
        private SFEProviderType providerType = SFEProviderType.FINTECH; // Default
        private SFESecurityLevel securityLevel = SFESecurityLevel.STANDARD; // Default
        private List<String> allowedAPIs = new ArrayList<>();
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

        public Builder setAllowedAPIs(List<String> allowedAPIs) {
            this.allowedAPIs = allowedAPIs;
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
                throw new IllegalStateException("App ID is required for SFE initialization");
            }
            return new SFEConfiguration(this);
        }
    }
}