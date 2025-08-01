package com.hanihome.platform.system.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Security Scan Service
 * Migrated from legacy api package to new DDD structure
 * Provides comprehensive security scanning for web requests
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityScanService {

    @Qualifier("sessionRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;
    private final SecurityAuditService securityAuditService;

    // Common SQL injection patterns
    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(union|select|insert|update|delete|drop|create|alter|exec|execute)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(script|javascript|vbscript|onload|onerror|onclick)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(<script|</script|<iframe|</iframe)", Pattern.CASE_INSENSITIVE)
    );

    // XSS patterns
    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE)
    );

    // Path traversal patterns
    private static final List<Pattern> PATH_TRAVERSAL_PATTERNS = Arrays.asList(
            Pattern.compile("\\.\\.[\\/\\\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\/\\\\]etc[\\/\\\\]passwd", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[\\/\\\\]windows[\\/\\\\]system32", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Scan for SQL injection patterns in input
     */
    public boolean scanForSqlInjection(String input, String source, String clientIp) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logSecurityThreat("SQL_INJECTION", input, source, clientIp, pattern.pattern());
                incrementThreatCounter(clientIp, "SQL_INJECTION");
                return true;
            }
        }
        return false;
    }

    /**
     * Scan for XSS patterns in input
     */
    public boolean scanForXSS(String input, String source, String clientIp) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logSecurityThreat("XSS", input, source, clientIp, pattern.pattern());
                incrementThreatCounter(clientIp, "XSS");
                return true;
            }
        }
        return false;
    }

    /**
     * Scan for path traversal patterns in input
     */
    public boolean scanForPathTraversal(String input, String source, String clientIp) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        for (Pattern pattern : PATH_TRAVERSAL_PATTERNS) {
            if (pattern.matcher(input).find()) {
                logSecurityThreat("PATH_TRAVERSAL", input, source, clientIp, pattern.pattern());
                incrementThreatCounter(clientIp, "PATH_TRAVERSAL");
                return true;
            }
        }
        return false;
    }

    /**
     * Comprehensive request scanning
     */
    public boolean scanRequest(HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        boolean threatDetected = false;

        // Scan request parameters
        if (request.getParameterMap() != null) {
            for (String paramName : request.getParameterMap().keySet()) {
                String[] paramValues = request.getParameterValues(paramName);
                if (paramValues != null) {
                    for (String value : paramValues) {
                        if (scanForSqlInjection(value, "PARAM:" + paramName, clientIp) ||
                            scanForXSS(value, "PARAM:" + paramName, clientIp) ||
                            scanForPathTraversal(value, "PARAM:" + paramName, clientIp)) {
                            threatDetected = true;
                        }
                    }
                }
            }
        }

        // Scan headers for suspicious content
        if (request.getHeaderNames() != null) {
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                if (scanForXSS(headerValue, "HEADER:" + headerName, clientIp)) {
                    threatDetected = true;
                }
            }
        }

        // Scan URI for path traversal
        if (scanForPathTraversal(request.getRequestURI(), "URI", clientIp)) {
            threatDetected = true;
        }

        return threatDetected;
    }

    /**
     * Check if IP is currently blocked
     */
    public boolean isIpBlocked(String clientIp) {
        String blockKey = "security:blocked_ip:" + clientIp;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }

    /**
     * Block an IP address for specified duration
     */
    public void blockIp(String clientIp, String reason, Duration blockDuration) {
        String blockKey = "security:blocked_ip:" + clientIp;
        redisTemplate.opsForValue().set(blockKey, reason, blockDuration);
        
        securityAuditService.logSecurityEvent(null, "IP_BLOCKED", 
                String.format("IP %s blocked for %s. Reason: %s", clientIp, blockDuration, reason), 
                clientIp);
        
        log.warn("IP {} blocked for {}. Reason: {}", clientIp, blockDuration, reason);
    }

    /**
     * Check threat counters and block suspicious IPs
     */
    public void checkAndBlockSuspiciousIp(String clientIp) {
        // Check threat counters for this IP
        long sqlInjectionCount = getThreatCount(clientIp, "SQL_INJECTION");
        long xssCount = getThreatCount(clientIp, "XSS");
        long pathTraversalCount = getThreatCount(clientIp, "PATH_TRAVERSAL");
        
        long totalThreats = sqlInjectionCount + xssCount + pathTraversalCount;
        
        // Block IP if too many threats detected
        if (totalThreats >= 5) {
            blockIp(clientIp, "Multiple security threats detected", Duration.ofHours(1));
        } else if (sqlInjectionCount >= 3) {
            blockIp(clientIp, "Multiple SQL injection attempts", Duration.ofMinutes(30));
        } else if (xssCount >= 3) {
            blockIp(clientIp, "Multiple XSS attempts", Duration.ofMinutes(30));
        } else if (pathTraversalCount >= 3) {
            blockIp(clientIp, "Multiple path traversal attempts", Duration.ofMinutes(30));
        }
    }

    /**
     * Perform comprehensive security scan
     */
    public void performSecurityScan() {
        log.info("Performing comprehensive security scan...");
        
        // Check for suspicious patterns in recent logs
        checkRecentSecurityEvents();
        
        // Check for unusual authentication patterns
        checkAuthenticationPatterns();
        
        // Check for potential brute force attacks
        checkBruteForceAttempts();
        
        log.info("Security scan completed");
    }

    /**
     * Log security threat event
     */
    private void logSecurityThreat(String threatType, String input, String source, String clientIp, String pattern) {
        String message = String.format("Security threat detected - Type: %s, Source: %s, Pattern: %s, Input: %s", 
                threatType, source, pattern, input.length() > 100 ? input.substring(0, 100) + "..." : input);
        
        securityAuditService.logSecurityEvent(null, threatType, message, clientIp);
        
        log.warn("Security threat detected from IP {}: {} in {}", clientIp, threatType, source);
    }

    /**
     * Increment threat counter for IP
     */
    private void incrementThreatCounter(String clientIp, String threatType) {
        String key = String.format("security:threat:%s:%s", clientIp, threatType);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(1));
    }

    /**
     * Get threat count for IP and type
     */
    private long getThreatCount(String clientIp, String threatType) {
        String key = String.format("security:threat:%s:%s", clientIp, threatType);
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0;
    }

    /**
     * Extract client IP from request
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Check recent security events for patterns
     */
    private void checkRecentSecurityEvents() {
        String recentEventsKey = "security:events:recent";
        Long eventCount = redisTemplate.opsForList().size(recentEventsKey);
        
        if (eventCount != null && eventCount > 100) {
            log.warn("High number of recent security events: {}", eventCount);
        }
    }

    /**
     * Check for unusual authentication patterns
     */
    private void checkAuthenticationPatterns() {
        log.debug("Checking authentication patterns...");
        // Analysis would include:
        // - Multiple failed logins from same IP
        // - Logins from unusual locations
        // - Rapid succession of login attempts
        // - Use of compromised credentials
    }

    /**
     * Check for potential brute force attempts
     */
    private void checkBruteForceAttempts() {
        log.debug("Checking for brute force attempts...");
        // Analysis would include:
        // - Failed login attempt frequency
        // - Dictionary attack patterns
        // - Credential stuffing attempts
        // - Password spray attacks
    }
}