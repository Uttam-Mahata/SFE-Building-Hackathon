package com.gradientgeeks.sfe.backendsdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "sfe.backend.sdk")
public class SfeBackendSdkConfig {
    
    /**
     * Google Cloud Project ID for Play Integrity API
     */
    private String googleCloudProjectId;
    
    /**
     * Google Play Integrity API key
     */
    private String playIntegrityApiKey;
    
    /**
     * Whether to enable telemetry collection
     */
    private boolean enableTelemetry = true;
    
    /**
     * Regulatory API endpoint for compliance reporting
     */
    private String regulatoryApiEndpoint;
    
    /**
     * Enhanced production configuration
     */
    private RegulatoryConfig regulatory = new RegulatoryConfig();
    private MultiTenantConfig multiTenant = new MultiTenantConfig();
    private ThreatDetectionConfig threatDetection = new ThreatDetectionConfig();
    private PerformanceConfig performance = new PerformanceConfig();
    
    /**
     * Security policies configuration
     */
    private PolicyConfig policies = new PolicyConfig();
    
    /**
     * Enhanced telemetry configuration
     */
    private TelemetryConfig telemetry = new TelemetryConfig();
    
    @Data
    public static class RegulatoryConfig {
        private String authorityId = "UNKNOWN";
        private String jurisdictionCode = "GLOBAL";
        private boolean enableRealTimeReporting = true;
        private boolean enableEmergencyNotifications = true;
        private boolean enableAuditTrail = true;
        private boolean enableDataLocalization = true;
        private List<String> reportingEndpoints = List.of();
        private Map<String, String> complianceRequirements = Map.of();
        private int emergencyResponseTimeoutMs = 5000;
        private String emergencyContactEndpoint;
    }
    
    @Data
    public static class MultiTenantConfig {
        private boolean enableMultiTenant = false;
        private String tenantIdentificationStrategy = "HEADER"; // HEADER, SUBDOMAIN, PATH
        private String tenantHeaderName = "X-Tenant-ID";
        private boolean enableTenantIsolation = true;
        private boolean enablePerTenantConfig = true;
        private Map<String, TenantConfig> tenants = Map.of();
    }
    
    @Data
    public static class TenantConfig {
        private String tenantId;
        private String name;
        private List<String> allowedOrigins = List.of();
        private RegulatoryConfig regulatory;
        private PolicyConfig policies;
        private boolean active = true;
        private Map<String, Object> customSettings = Map.of();
    }
    
    @Data
    public static class ThreatDetectionConfig {
        private boolean enableAiThreatDetection = false;
        private boolean enableBehavioralAnalysis = false;
        private boolean enableAnomalyDetection = true;
        private boolean enableRealTimeBlocking = true;
        private int threatScoreThreshold = 70;
        private int criticalThreatThreshold = 90;
        private String threatIntelligenceEndpoint;
        private boolean enableThreatSharing = false;
        private List<String> enabledDetectors = List.of(
            "ROOT_DETECTION", "DEBUGGER_DETECTION", "APP_TAMPERING",
            "MALWARE_DETECTION", "FRAUD_DETECTION"
        );
    }
    
    @Data
    public static class PerformanceConfig {
        private boolean enablePerformanceMonitoring = true;
        private boolean enableRequestTracing = false;
        private boolean enableMetrics = true;
        private int requestTimeoutMs = 10000;
        private int maxConcurrentRequests = 1000;
        private boolean enableCaching = true;
        private int cacheExpirationMinutes = 30;
        private boolean enableLoadBalancing = false;
    }
    
    @Data
    public static class PolicyConfig {
        private SecurityPolicy rootDetection = new SecurityPolicy("REJECT", "HIGH");
        private SecurityPolicy debuggerDetection = new SecurityPolicy("WARN", "MEDIUM");
        private SecurityPolicy appTampering = new SecurityPolicy("REJECT", "CRITICAL");
        
        // Enhanced policies for production
        private SecurityPolicy malwareDetection = new SecurityPolicy("BLOCK", "CRITICAL");
        private SecurityPolicy fraudDetection = new SecurityPolicy("REQUIRE_ADDITIONAL_AUTH", "HIGH");
        private SecurityPolicy unusualBehavior = new SecurityPolicy("MONITOR", "MEDIUM");
        private SecurityPolicy deviceBinding = new SecurityPolicy("REQUIRE_REBIND", "HIGH");
        private SecurityPolicy networkSecurity = new SecurityPolicy("REJECT", "HIGH");
        private SecurityPolicy geolocationAnomaly = new SecurityPolicy("WARN", "MEDIUM");
        
        private boolean enableAdaptivePolicies = true;
        private boolean enablePolicyUpdates = true;
        private boolean enableCustomPolicies = false;
        private Map<String, SecurityPolicy> customPolicies = Map.of();
        
        @Data
        public static class SecurityPolicy {
            private String action;
            private String riskLevel;
            private boolean enabled = true;
            private Map<String, Object> parameters = Map.of();
            private long lastUpdated = System.currentTimeMillis();
            
            public SecurityPolicy() {}
            
            public SecurityPolicy(String action, String riskLevel) {
                this.action = action;
                this.riskLevel = riskLevel;
            }
        }
    }
    
    @Data
    public static class TelemetryConfig {
        private boolean enableBatching = true;
        private int batchSize = 100;
        private long batchTimeoutMs = 60000; // 1 minute
        private boolean enableAnonymization = true;
        private String saltKey = "default-salt-key";
        
        // Enhanced telemetry features
        private boolean enableRealTimeStream = false;
        private boolean enableAggregation = true;
        private boolean enableAlerts = true;
        private String streamingEndpoint;
        private List<String> enabledMetrics = List.of(
            "SECURITY_EVENTS", "PERFORMANCE_METRICS", "COMPLIANCE_EVENTS",
            "THREAT_DETECTION", "USER_BEHAVIOR"
        );
        private Map<String, Integer> alertThresholds = Map.of(
            "HIGH_RISK_EVENTS_PER_MINUTE", 10,
            "CRITICAL_THREATS_PER_HOUR", 5,
            "POLICY_VIOLATIONS_PER_DAY", 100
        );
        private boolean enableDataExport = false;
        private String exportFormat = "JSON"; // JSON, CSV, PARQUET
        private String exportDestination; // S3, GCS, Azure, etc.
    }
    
    @Data
    public static class IntegrationConfig {
        private boolean enablePlayIntegrityApi = true;
        private boolean enableHardwareAttestation = true;
        private boolean enableDeviceBinding = true;
        private boolean enableBiometricValidation = false;
        private boolean enableLocationValidation = false;
        
        // Third-party integrations
        private Map<String, ThirdPartyIntegration> thirdPartyIntegrations = Map.of();
    }
    
    @Data
    public static class ThirdPartyIntegration {
        private String name;
        private String endpoint;
        private String apiKey;
        private boolean enabled = false;
        private Map<String, String> parameters = Map.of();
    }
    
    @Data
    public static class SecurityConfig {
        private boolean enableEncryption = true;
        private String encryptionAlgorithm = "AES-256-GCM";
        private boolean enableTokenValidation = true;
        private int tokenExpirationMinutes = 60;
        private boolean enableRateLimiting = true;
        private int maxRequestsPerMinute = 100;
        private boolean enableIpWhitelisting = false;
        private List<String> whitelistedIps = List.of();
        private boolean enableTlsValidation = true;
        private String minimumTlsVersion = "1.2";
    }
    
    // Getters for enhanced configurations
    public RegulatoryConfig getRegulatory() { return regulatory; }
    public MultiTenantConfig getMultiTenant() { return multiTenant; }
    public ThreatDetectionConfig getThreatDetection() { return threatDetection; }
    public PerformanceConfig getPerformance() { return performance; }
} 