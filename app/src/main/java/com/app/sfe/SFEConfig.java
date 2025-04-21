package com.app.sfe;

import com.app.sfe.security.SFEProviderType;
import com.app.sfe.security.SFESecurityLevel;

/**
 * Configuration class for initializing the SFE SDK.
 * Contains all the necessary settings for the SDK to function properly.
 */
public class SFEConfig {
    private final String applicationId;
    private final SFEProviderType providerType;
    private final SFEEnvironment environment;
    private final SFESecurityLevel securityLevel;
    private final String apiEndpoint;
    private final boolean analyticsEnabled;
    private final long transactionTimeout;

    private SFEConfig(Builder builder) {
        this.applicationId = builder.applicationId;
        this.providerType = builder.providerType;
        this.environment = builder.environment;
        this.securityLevel = builder.securityLevel;
        this.apiEndpoint = builder.apiEndpoint;
        this.analyticsEnabled = builder.analyticsEnabled;
        this.transactionTimeout = builder.transactionTimeout;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public SFEProviderType getProviderType() {
        return providerType;
    }

    public SFEEnvironment getEnvironment() {
        return environment;
    }

    public SFESecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }

    public long getTransactionTimeout() {
        return transactionTimeout;
    }

    /**
     * Builder class for SFEConfig to enable fluent interface pattern.
     */
    public static class Builder {
        private String applicationId;
        private SFEProviderType providerType = SFEProviderType.FINTECH; // Default
        private SFEEnvironment environment = SFEEnvironment.SANDBOX; // Default
        private SFESecurityLevel securityLevel = SFESecurityLevel.STANDARD; // Default
        private String apiEndpoint;
        private boolean analyticsEnabled = true;
        private long transactionTimeout = 30000; // 30 seconds default

        public Builder setApplicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder setProviderType(SFEProviderType providerType) {
            this.providerType = providerType;
            return this;
        }

        public Builder setEnvironment(SFEEnvironment environment) {
            this.environment = environment;
            return this;
        }

        public Builder setSecurityLevel(SFESecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        public Builder setApiEndpoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
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

        public SFEConfig build() {
            if (applicationId == null || applicationId.isEmpty()) {
                throw new IllegalStateException("Application ID is required for SFE initialization");
            }
            if (apiEndpoint == null || apiEndpoint.isEmpty()) {
                throw new IllegalStateException("API endpoint is required for SFE initialization");
            }
            return new SFEConfig(this);
        }
    }
}