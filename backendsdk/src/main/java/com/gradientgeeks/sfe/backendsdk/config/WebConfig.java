package com.gradientgeeks.sfe.backendsdk.config;

import com.gradientgeeks.sfe.backendsdk.security.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SfeBackendSdkConfig sfeBackendSdkConfig;

    @Autowired
    private TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // This interceptor will extract the tenant ID from requests
        registry.addInterceptor(tenantInterceptor);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Configure CORS dynamically based on tenant configuration
                List<String> allowedOrigins = sfeBackendSdkConfig.getMultiTenant().getTenants().values().stream()
                        .flatMap(tenant -> tenant.getAllowedOrigins().stream())
                        .distinct()
                        .collect(Collectors.toList());

                if (allowedOrigins.isEmpty()) {
                    // Fallback for development or if no origins are configured
                    allowedOrigins.add("http://localhost:3000");
                }

                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
} 