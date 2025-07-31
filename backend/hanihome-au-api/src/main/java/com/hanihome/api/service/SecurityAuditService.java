package com.hanihome.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final RedisTemplate<String, Object> redisObjectTemplate;
    
    private static final String AUDIT_LOG_PREFIX = "audit_log:";
    private static final String SECURITY_EVENT_PREFIX = "security_event:";
    private static final String USER_ACTION_PREFIX = "user_action:";

    public void logSecurityEvent(Long userId, String eventType, String description, String details) {
        String eventId = generateEventId();
        Map<String, Object> event = createBaseAuditEvent(userId, eventType, description, details);
        event.put("severity", "HIGH");
        event.put("category", "SECURITY");
        
        // Store in Redis with expiration (30 days)
        String key = SECURITY_EVENT_PREFIX + eventId;
        redisObjectTemplate.opsForValue().set(key, event, 30, TimeUnit.DAYS);
        
        // Also log to application logs for immediate monitoring
        log.warn("Security Event [{}] User: {} Event: {} Description: {} Details: {}", 
            eventId, userId, eventType, description, details);
        
        // Store reference in user's security events list
        String userSecurityKey = "user_security_events:" + userId;
        redisObjectTemplate.opsForList().leftPush(userSecurityKey, eventId);
        redisObjectTemplate.expire(userSecurityKey, 30, TimeUnit.DAYS);
    }

    public void logUserAction(Long userId, String actionType, String description, String details) {
        String eventId = generateEventId();
        Map<String, Object> event = createBaseAuditEvent(userId, actionType, description, details);
        event.put("severity", "INFO");
        event.put("category", "USER_ACTION");
        
        // Store in Redis with expiration (7 days for regular actions)
        String key = USER_ACTION_PREFIX + eventId;
        redisObjectTemplate.opsForValue().set(key, event, 7, TimeUnit.DAYS);
        
        log.info("User Action [{}] User: {} Action: {} Description: {}", 
            eventId, userId, actionType, description);
        
        // Store reference in user's action log
        String userActionKey = "user_actions:" + userId;
        redisObjectTemplate.opsForList().leftPush(userActionKey, eventId);
        redisObjectTemplate.expire(userActionKey, 7, TimeUnit.DAYS);
        
        // Keep only last 100 actions per user
        redisObjectTemplate.opsForList().trim(userActionKey, 0, 99);
    }

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
        redisObjectTemplate.opsForValue().set(key, event, 30, TimeUnit.DAYS);
        
        if (successful) {
            log.info("Successful login attempt [{}] Email: {} IP: {}", eventId, email, ipAddress);
        } else {
            log.warn("Failed login attempt [{}] Email: {} IP: {}", eventId, email, ipAddress);
        }
        
        // Track failed attempts by IP for rate limiting
        if (!successful) {
            String failedAttemptsKey = "failed_login_attempts:" + ipAddress;
            redisObjectTemplate.opsForValue().increment(failedAttemptsKey);
            redisObjectTemplate.expire(failedAttemptsKey, 1, TimeUnit.HOURS);
        }
    }

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
            redisObjectTemplate.opsForValue().set(key, event, 7, TimeUnit.DAYS);
            
            if (responseStatus >= 400) {
                log.warn("API Error [{}] User: {} {} {} Status: {} Time: {}ms", 
                    eventId, userId, method, endpoint, responseStatus, responseTime);
            } else {
                log.debug("API Access [{}] User: {} {} {} Status: {} Time: {}ms", 
                    eventId, userId, method, endpoint, responseStatus, responseTime);
            }
        }
    }

    public int getFailedLoginAttempts(String ipAddress) {
        String key = "failed_login_attempts:" + ipAddress;
        Object attempts = redisObjectTemplate.opsForValue().get(key);
        return attempts != null ? (Integer) attempts : 0;
    }

    public void clearFailedLoginAttempts(String ipAddress) {
        String key = "failed_login_attempts:" + ipAddress;
        redisObjectTemplate.delete(key);
    }

    public boolean isIpBlocked(String ipAddress) {
        return getFailedLoginAttempts(ipAddress) >= 10; // Block after 10 failed attempts
    }

    public void blockIp(String ipAddress, long durationMinutes) {
        String key = "blocked_ip:" + ipAddress;
        redisObjectTemplate.opsForValue().set(key, "blocked", durationMinutes, TimeUnit.MINUTES);
        
        logSecurityEvent(null, "IP_BLOCKED", 
            "IP 주소가 보안 위반으로 차단되었습니다", 
            "IP: " + ipAddress + ", Duration: " + durationMinutes + " minutes");
    }

    public boolean isIpExplicitlyBlocked(String ipAddress) {
        String key = "blocked_ip:" + ipAddress;
        return Boolean.TRUE.equals(redisObjectTemplate.hasKey(key));
    }

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

    private String generateEventId() {
        return System.currentTimeMillis() + "-" + 
               Integer.toHexString((int)(Math.random() * 0x10000));
    }

    private boolean isSensitiveEndpoint(String endpoint) {
        return endpoint.contains("/auth/") || 
               endpoint.contains("/sessions/") || 
               endpoint.contains("/admin/") ||
               endpoint.contains("/profile/") ||
               endpoint.contains("/password");
    }
}