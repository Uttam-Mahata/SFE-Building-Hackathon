package com.gradientgeeks.sfe.sfpaybackend.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtTokenService {
    
    private final SecretKey signingKey;
    private final long tokenValidityInSeconds;
    
    public JwtTokenService(@Value("${jwt.secret:sfe-payment-app-super-secret-key-change-in-production}") String jwtSecret,
                          @Value("${jwt.validity-seconds:3600}") long tokenValidityInSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.tokenValidityInSeconds = tokenValidityInSeconds;
    }
    
    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(String username, String userId, String role, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(tokenValidityInSeconds);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", expiration.getEpochSecond());
        
        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }
        
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
        
        log.debug("Generated JWT token for user: {}", username);
        return token;
    }
    
    /**
     * Generate token with SFE security assessment
     */
    public String generateTokenWithSecurity(String username, String userId, String role, String riskLevel, boolean deviceTrusted) {
        Map<String, Object> securityClaims = new HashMap<>();
        securityClaims.put("riskLevel", riskLevel);
        securityClaims.put("deviceTrusted", deviceTrusted);
        securityClaims.put("securityAssessmentTime", Instant.now().getEpochSecond());
        
        return generateToken(username, userId, role, securityClaims);
    }
    
    /**
     * Validate and parse JWT token
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            throw new JwtValidationException("Token has expired", e);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new JwtValidationException("Unsupported token format", e);
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new JwtValidationException("Malformed token", e);
        } catch (SecurityException e) {
            log.warn("JWT token security validation failed: {}", e.getMessage());
            throw new JwtValidationException("Token security validation failed", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is null or empty: {}", e.getMessage());
            throw new JwtValidationException("Token is null or empty", e);
        }
    }
    
    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getSubject();
        } catch (JwtValidationException e) {
            log.error("Error extracting username from token", e);
            return null;
        }
    }
    
    /**
     * Extract user ID from token
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.get("userId", String.class);
        } catch (JwtValidationException e) {
            log.error("Error extracting user ID from token", e);
            return null;
        }
    }
    
    /**
     * Extract role from token
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.get("role", String.class);
        } catch (JwtValidationException e) {
            log.error("Error extracting role from token", e);
            return null;
        }
    }
    
    /**
     * Extract security assessment from token
     */
    public SecurityAssessment getSecurityAssessmentFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            
            String riskLevel = claims.get("riskLevel", String.class);
            Boolean deviceTrusted = claims.get("deviceTrusted", Boolean.class);
            Long assessmentTime = claims.get("securityAssessmentTime", Long.class);
            
            return SecurityAssessment.builder()
                .riskLevel(riskLevel)
                .deviceTrusted(deviceTrusted != null ? deviceTrusted : false)
                .assessmentTime(assessmentTime != null ? assessmentTime : 0L)
                .build();
                
        } catch (JwtValidationException e) {
            log.error("Error extracting security assessment from token", e);
            return null;
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (JwtValidationException e) {
            return true; // Consider invalid tokens as expired
        }
    }
    
    /**
     * Refresh token if it's close to expiration
     */
    public String refreshTokenIfNeeded(String token, long refreshThresholdSeconds) {
        try {
            Claims claims = validateToken(token);
            Date expiration = claims.getExpiration();
            
            // Check if token expires within the threshold
            if (expiration.before(Date.from(Instant.now().plusSeconds(refreshThresholdSeconds)))) {
                String username = claims.getSubject();
                String userId = claims.get("userId", String.class);
                String role = claims.get("role", String.class);
                String riskLevel = claims.get("riskLevel", String.class);
                Boolean deviceTrusted = claims.get("deviceTrusted", Boolean.class);
                
                log.info("Refreshing token for user: {}", username);
                return generateTokenWithSecurity(username, userId, role, riskLevel, deviceTrusted != null ? deviceTrusted : false);
            }
            
            return token; // No refresh needed
            
        } catch (JwtValidationException e) {
            log.error("Cannot refresh invalid token", e);
            return null;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SecurityAssessment {
        private String riskLevel;
        private boolean deviceTrusted;
        private long assessmentTime;
    }
    
    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 