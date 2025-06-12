package com.gradientgeeks.sfe.backendsdk.config;

import com.gradientgeeks.sfe.backendsdk.services.AttestationVerificationService;
import com.gradientgeeks.sfe.backendsdk.services.PolicyEnforcementService;
import com.gradientgeeks.sfe.backendsdk.services.TelemetryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableConfigurationProperties(SfeBackendSdkConfig.class)
@ComponentScan(basePackages = "com.gradientgeeks.sfe.backendsdk")
public class SfeAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public AttestationVerificationService attestationVerificationService(SfeBackendSdkConfig config) {
        return new AttestationVerificationService(config);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public TelemetryService telemetryService(SfeBackendSdkConfig config) {
        return new TelemetryService(config);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public PolicyEnforcementService policyEnforcementService(SfeBackendSdkConfig config, TelemetryService telemetryService) {
        return new PolicyEnforcementService(config, telemetryService);
    }
} 