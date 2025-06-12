package com.gradientgeeks.sfe.backendsdk.security;

import com.gradientgeeks.sfe.backendsdk.config.SfeBackendSdkConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private SfeBackendSdkConfig config;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenantId = request.getHeader(config.getMultiTenant().getTenantHeaderName());
        if (tenantId != null && !tenantId.isEmpty()) {
            if (config.getMultiTenant().getTenants().containsKey(tenantId)) {
                TenantContext.setCurrentTenant(tenantId);
                log.debug("Tenant context set to: {}", tenantId);
            } else {
                log.warn("Attempt to access with unknown tenant ID: {}", tenantId);
                // Optionally, you could reject the request here
                // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Tenant ID");
                // return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // No implementation needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
        log.debug("Tenant context cleared");
    }
}