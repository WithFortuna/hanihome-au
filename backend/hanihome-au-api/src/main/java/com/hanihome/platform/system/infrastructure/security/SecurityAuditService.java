package com.hanihome.platform.system.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Security Audit Service
 * Migrated from legacy api package to new DDD structure
 * Provides comprehensive security event logging and audit functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String AUDIT_LOG_PREFIX = "audit_log:";
    private static final String SECURITY_EVENT_PREFIX = "security_event:";
    private static final String USER_ACTION_PREFIX = "user_action:";

    /**
     * Log security-related events with high priority
     */
    public void logSecurityEvent(Long userId, String eventType, String description, String details) {
        String eventId = generateEventId();
        Map<String, Object> event = createBaseAuditEvent(userId, eventType, description, details);
        event.put("severity", "HIGH");
        event.put("category", "SECURITY");
        
        // Store in Redis with expiration (30 days)
        String key = SECURITY_EVENT_PREFIX + eventId;
        redisTemplate.opsForValue().set(key, event, 30, TimeUnit.DAYS);
        
        // Also log to application logs for immediate monitoring
        log.warn("Security Event [{}] User: {} Event: {} Description: {} Details: {}", 
            eventId, userId, eventType, description, details);
        
        // Store reference in user's security events list if user is specified
        if (userId != null) {
            String userSecurityKey = "user_security_events:" + userId;
            redisTemplate.opsForList().leftPush(userSecurityKey, eventId);
            redisTemplate.expire(userSecurityKey, 30, TimeUnit.DAYS);
        }

        // Store in recent events list for system monitoring
        String recentEventsKey = "security:events:recent";
        redisTemplate.opsForList().leftPush(recentEventsKey, eventId);
        redisTemplate.opsForList().trim(recentEventsKey, 0, 999); // Keep last 1000 events
        redisTemplate.expire(recentEventsKey, 7, TimeUnit.DAYS);
    }

    /**
     * Log regular user actions
     */
    public void logUserAction(Long userId, String actionType, String description, String details) {
        String eventId = generateEventId();
        Map<String, Object> event = createBaseAuditEvent(userId, actionType, description, details);
        event.put("severity", "INFO");
        event.put("category", "USER_ACTION");
        
        // Store in Redis with expiration (7 days for regular actions)
        String key = USER_ACTION_PREFIX + eventId;
        redisTemplate.opsForValue().set(key, event, 7, TimeUnit.DAYS);
        
        log.info("User Action [{}] User: {} Action: {} Description: {}", 
            eventId, userId, actionType, description);
        
        // Store reference in user's action log
        String userActionKey = "user_actions:" + userId;
        redisTemplate.opsForList().leftPush(userActionKey, eventId);
        redisTemplate.expire(userActionKey, 7, TimeUnit.DAYS);
        
        // Keep only last 100 actions per user
        redisTemplate.opsForList().trim(userActionKey, 0, 99);
    }

    /**
     * Log authentication attempts
     */
    public void logLoginAttempt(String email, String ipAddress, boolean successful, String userAgent) {
        String eventId = generateEventId();
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId);
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("email", email);
        event.put("ipAddress", ipAddress);
        event.put("userAgent", userAgent);
        event.put("successful", successful);
        event.put("eventType", "LOGIN_ATTEMPT");
        event.put("category", "AUTHENTICATION");
        event.put("severity", successful ? "INFO" : "WARN");
        
        String key = AUDIT_LOG_PREFIX + "login:" + eventId;
        redisTemplate.opsForValue().set(key, event, 30, TimeUnit.DAYS);
        
        if (successful) {
            log.info("Successful login attempt [{}] Email: {} IP: {}", eventId, email, ipAddress);
        } else {
            log.warn("Failed login attempt [{}] Email: {} IP: {}", eventId, email, ipAddress);
        }
        
        // Track failed attempts by IP for rate limiting
        if (!successful) {
            String failedAttemptsKey = "failed_login_attempts:" + ipAddress;
            redisTemplate.opsForValue().increment(failedAttemptsKey);
            redisTemplate.expire(failedAttemptsKey, 1, TimeUnit.HOURS);
        }
    }

    /**
     * Log API access patterns
     */
    public void logApiAccess(Long userId, String endpoint, String method, String ipAddress, 
                           int responseStatus, long responseTime) {
        // Only log sensitive endpoints and errors
        if (isSensitiveEndpoint(endpoint) || responseStatus >= 400) {
            String eventId = generateEventId();
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", eventId);
            event.put("timestamp", LocalDateTime.now().toString());
            event.put("userId", userId);
            event.put("endpoint", endpoint);
            event.put("method", method);
            event.put("ipAddress", ipAddress);
            event.put("responseStatus", responseStatus);
            event.put("responseTime", responseTime);
            event.put("eventType", "API_ACCESS");
            event.put("category", "ACCESS");
            
            String key = AUDIT_LOG_PREFIX + "api:" + eventId;
            redisTemplate.opsForValue().set(key, event, 7, TimeUnit.DAYS);
            
            if (responseStatus >= 400) {
                log.warn("API Error [{}] User: {} {} {} Status: {} Time: {}ms", 
                    eventId, userId, method, endpoint, responseStatus, responseTime);
            } else {
                log.debug("API Access [{}] User: {} {} {} Status: {} Time: {}ms", 
                    eventId, userId, method, endpoint, responseStatus, responseTime);
            }
        }
    }

    /**
     * Get failed login attempt count for IP
     */
    public int getFailedLoginAttempts(String ipAddress) {
        String key = "failed_login_attempts:" + ipAddress;
        Object attempts = redisTemplate.opsForValue().get(key);
        return attempts != null ? (Integer) attempts : 0;
    }

    /**
     * Clear failed login attempts for IP
     */
    public void clearFailedLoginAttempts(String ipAddress) {
        String key = "failed_login_attempts:" + ipAddress;
        redisTemplate.delete(key);
    }

    /**
     * Check if IP should be blocked based on failed attempts
     */
    public boolean isIpBlocked(String ipAddress) {
        return getFailedLoginAttempts(ipAddress) >= 10; // Block after 10 failed attempts
    }

    /**
     * Block IP explicitly
     */
    public void blockIp(String ipAddress, long durationMinutes) {
        String key = "blocked_ip:" + ipAddress;
        redisTemplate.opsForValue().set(key, "blocked", durationMinutes, TimeUnit.MINUTES);
        
        logSecurityEvent(null, "IP_BLOCKED", 
            "IP address blocked due to security violations", 
            "IP: " + ipAddress + ", Duration: " + durationMinutes + " minutes");
    }

    /**
     * Check if IP is explicitly blocked
     */
    public boolean isIpExplicitlyBlocked(String ipAddress) {
        String key = "blocked_ip:" + ipAddress;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Log OAuth2 authentication events
     */
    public void logOAuth2Event(String email, String provider, String eventType, String ipAddress, boolean successful) {
        String eventId = generateEventId();
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId);
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("email", email);
        event.put("provider", provider);
        event.put("eventType", eventType);
        event.put("ipAddress", ipAddress);
        event.put("successful", successful);
        event.put("category", "OAUTH2");
        event.put("severity", successful ? "INFO" : "WARN");
        
        String key = AUDIT_LOG_PREFIX + "oauth2:" + eventId;
        redisTemplate.opsForValue().set(key, event, 30, TimeUnit.DAYS);
        
        log.info("OAuth2 Event [{}] Email: {} Provider: {} Event: {} Success: {}", 
            eventId, email, provider, eventType, successful);
    }

    /**
     * Create base audit event structure
     */
    private Map<String, Object> createBaseAuditEvent(Long userId, String eventType, 
                                                   String description, String details) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", generateEventId());
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("userId", userId);
        event.put("eventType", eventType);
        event.put("description", description);
        event.put("details", details);
        return event;
    }

    /**
     * Generate unique event ID
     */
    private String generateEventId() {
        return System.currentTimeMillis() + "-" + 
               Integer.toHexString((int)(Math.random() * 0x10000));
    }

    /**
     * Check if endpoint is sensitive and should be logged
     */
    private boolean isSensitiveEndpoint(String endpoint) {
        return endpoint.contains("/auth/") || 
               endpoint.contains("/sessions/") || 
               endpoint.contains("/admin/") ||
               endpoint.contains("/profile/") ||
               endpoint.contains("/password") ||
               endpoint.contains("/properties/create") ||
               endpoint.contains("/properties/update") ||
               endpoint.contains("/users/");
    }
}