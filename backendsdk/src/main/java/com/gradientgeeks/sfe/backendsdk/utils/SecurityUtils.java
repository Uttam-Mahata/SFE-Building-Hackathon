package com.gradientgeeks.sfe.backendsdk.utils;

import org.apache.commons.codec.digest.DigestUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class SecurityUtils {
    
    private static final String DEFAULT_SALT = "sfe-backend-salt-2025";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    public static String hashDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return "unknown";
        }
        return DigestUtils.sha256Hex(deviceId + DEFAULT_SALT);
    }
    
    public static String hashDeviceId(String deviceId, String salt) {
        if (deviceId == null || deviceId.isEmpty()) {
            return "unknown";
        }
        String saltToUse = (salt != null && !salt.isEmpty()) ? salt : DEFAULT_SALT;
        return DigestUtils.sha256Hex(deviceId + saltToUse);
    }
    
    public static String generateNonce() {
        byte[] nonce = new byte[32];
        SECURE_RANDOM.nextBytes(nonce);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
    }
    
    public static long truncateToHour(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return instant.truncatedTo(ChronoUnit.HOURS).toEpochMilli();
    }
    
    public static boolean isValidTimestamp(long timestamp) {
        Instant now = Instant.now();
        Instant requestTime = Instant.ofEpochMilli(timestamp);
        
        if (requestTime.isBefore(now.minus(5, ChronoUnit.MINUTES))) {
            return false;
        }
        
        if (requestTime.isAfter(now.plus(1, ChronoUnit.MINUTES))) {
            return false;
        }
        
        return true;
    }
    
    public static String sanitizeForLogging(String input) {
        if (input == null || input.length() <= 8) {
            return "***";
        }
        return input.substring(0, 4) + "****" + input.substring(input.length() - 4);
    }
    
    public static boolean isJwtFormat(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
    
    public static String generateDeviceFingerprint(String deviceModel, String osVersion, String networkOperator, String deviceBindingToken) {
        String combined = String.format("%s|%s|%s|%s", 
            deviceModel != null ? deviceModel : "",
            osVersion != null ? osVersion : "",
            networkOperator != null ? networkOperator : "",
            deviceBindingToken != null ? deviceBindingToken : ""
        );
        return DigestUtils.sha256Hex(combined + DEFAULT_SALT);
    }
    
    public static boolean isValidRiskLevel(String riskLevel) {
        if (riskLevel == null) {
            return false;
        }
        return riskLevel.matches("^(LOW|MEDIUM|HIGH|CRITICAL)$");
    }
    
    public static int compareRiskLevels(String riskLevel1, String riskLevel2) {
        int level1 = getRiskLevelValue(riskLevel1);
        int level2 = getRiskLevelValue(riskLevel2);
        return Integer.compare(level1, level2);
    }
    
    private static int getRiskLevelValue(String riskLevel) {
        if (riskLevel == null) return 0;
        switch (riskLevel.toUpperCase()) {
            case "LOW": return 1;
            case "MEDIUM": return 2;
            case "HIGH": return 3;
            case "CRITICAL": return 4;
            default: return 0;
        }
    }
} 