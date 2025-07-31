package com.hanihome.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceMonitoringService {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String PERFORMANCE_PREFIX = "perf:";
    private static final String REQUEST_COUNT_PREFIX = "req_count:";
    private static final String RESPONSE_TIME_PREFIX = "resp_time:";
    private static final String ERROR_COUNT_PREFIX = "error_count:";

    public void recordRequestStart(String endpoint, String requestId) {
        String key = PERFORMANCE_PREFIX + "start:" + requestId;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), Duration.ofMinutes(5));
    }

    public void recordRequestEnd(String endpoint, String requestId, boolean isError) {
        String startKey = PERFORMANCE_PREFIX + "start:" + requestId;
        String startTimeStr = redisTemplate.opsForValue().get(startKey);
        
        if (startTimeStr != null) {
            long startTime = Long.parseLong(startTimeStr);
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            recordResponseTime(endpoint, responseTime);
            incrementRequestCount(endpoint);
            
            if (isError) {
                incrementErrorCount(endpoint);
            }
            
            // Clean up start time
            redisTemplate.delete(startKey);
            
            log.debug("Request completed - Endpoint: {}, Response Time: {}ms, Error: {}", 
                     endpoint, responseTime, isError);
        }
    }

    public void recordAuthenticationAttempt(String provider, boolean success) {
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String countKey = String.format("auth:%s:%s:%s", provider, success ? "success" : "failure", dateKey);
        
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, 24, TimeUnit.HOURS);
    }

    public void recordTokenGeneration(long generationTime) {
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String timeKey = RESPONSE_TIME_PREFIX + "token_generation:" + dateKey;
        
        // Store as average response time
        String currentAvgStr = redisTemplate.opsForValue().get(timeKey);
        if (currentAvgStr == null) {
            redisTemplate.opsForValue().set(timeKey, String.valueOf(generationTime), Duration.ofHours(24));
        } else {
            long currentAvg = Long.parseLong(currentAvgStr);
            long newAvg = (currentAvg + generationTime) / 2;
            redisTemplate.opsForValue().set(timeKey, String.valueOf(newAvg), Duration.ofHours(24));
        }
    }

    public void recordDatabaseQuery(String operation, long queryTime) {
        if (queryTime > 1000) { // Log slow queries (> 1 second)
            log.warn("Slow database query detected - Operation: {}, Time: {}ms", operation, queryTime);
        }
        
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String timeKey = RESPONSE_TIME_PREFIX + "db:" + operation + ":" + dateKey;
        
        // Store average query time
        String currentAvgStr = redisTemplate.opsForValue().get(timeKey);
        if (currentAvgStr == null) {
            redisTemplate.opsForValue().set(timeKey, String.valueOf(queryTime), Duration.ofHours(24));
        } else {
            long currentAvg = Long.parseLong(currentAvgStr);
            long newAvg = (currentAvg + queryTime) / 2;
            redisTemplate.opsForValue().set(timeKey, String.valueOf(newAvg), Duration.ofHours(24));
        }
    }

    public void recordCacheHit(String cacheType, boolean hit) {
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String hitKey = String.format("cache:%s:%s:%s", cacheType, hit ? "hit" : "miss", dateKey);
        
        redisTemplate.opsForValue().increment(hitKey);
        redisTemplate.expire(hitKey, 24, TimeUnit.HOURS);
    }

    public void recordMemoryUsage(long usedMemory, long totalMemory) {
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String memoryKey = "memory:usage:" + dateKey;
        
        double usagePercentage = (double) usedMemory / totalMemory * 100;
        redisTemplate.opsForValue().set(memoryKey, String.valueOf(usagePercentage), Duration.ofHours(24));
        
        if (usagePercentage > 80) {
            log.warn("High memory usage detected: {:.2f}% ({} MB / {} MB)", 
                    usagePercentage, usedMemory / 1048576, totalMemory / 1048576);
        }
    }

    public long getRequestCount(String endpoint, String period) {
        String key = REQUEST_COUNT_PREFIX + endpoint + ":" + period;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0;
    }

    public long getAverageResponseTime(String endpoint, String period) {
        String key = RESPONSE_TIME_PREFIX + endpoint + ":" + period;
        String avgTime = redisTemplate.opsForValue().get(key);
        return avgTime != null ? Long.parseLong(avgTime) : 0;
    }

    public long getErrorCount(String endpoint, String period) {
        String key = ERROR_COUNT_PREFIX + endpoint + ":" + period;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0;
    }

    public double getErrorRate(String endpoint, String period) {
        long totalRequests = getRequestCount(endpoint, period);
        long errorCount = getErrorCount(endpoint, period);
        
        if (totalRequests == 0) return 0.0;
        return (double) errorCount / totalRequests * 100;
    }

    public void generatePerformanceReport() {
        String currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        
        log.info("=== Performance Report for {} ===", currentHour);
        
        // Authentication metrics
        long googleAuthSuccess = getMetric("auth:google:success:" + currentHour);
        long googleAuthFailure = getMetric("auth:google:failure:" + currentHour);
        long kakaoAuthSuccess = getMetric("auth:kakao:success:" + currentHour);
        long kakaoAuthFailure = getMetric("auth:kakao:failure:" + currentHour);
        
        log.info("Authentication - Google: {} success, {} failures", googleAuthSuccess, googleAuthFailure);
        log.info("Authentication - Kakao: {} success, {} failures", kakaoAuthSuccess, kakaoAuthFailure);
        
        // API endpoint metrics
        String[] endpoints = {"/api/profile", "/api/auth/login", "/api/auth/refresh"};
        for (String endpoint : endpoints) {
            long requests = getRequestCount(endpoint, currentHour);
            long avgResponseTime = getAverageResponseTime(endpoint, currentHour);
            double errorRate = getErrorRate(endpoint, currentHour);
            
            log.info("Endpoint: {} - Requests: {}, Avg Response Time: {}ms, Error Rate: {:.2f}%", 
                    endpoint, requests, avgResponseTime, errorRate);
        }
        
        // Token generation metrics
        long avgTokenGenTime = getMetric(RESPONSE_TIME_PREFIX + "token_generation:" + currentHour);
        log.info("Average token generation time: {}ms", avgTokenGenTime);
        
        // Database query metrics
        String[] dbOperations = {"user_find", "user_save", "user_update"};
        for (String operation : dbOperations) {
            long avgQueryTime = getMetric(RESPONSE_TIME_PREFIX + "db:" + operation + ":" + currentHour);
            log.info("DB Operation: {} - Average query time: {}ms", operation, avgQueryTime);
        }
        
        // Cache metrics
        String[] cacheTypes = {"user_profile", "session", "jwt_blacklist"};
        for (String cacheType : cacheTypes) {
            long hits = getMetric("cache:" + cacheType + ":hit:" + currentHour);
            long misses = getMetric("cache:" + cacheType + ":miss:" + currentHour);
            double hitRate = hits + misses > 0 ? (double) hits / (hits + misses) * 100 : 0;
            log.info("Cache: {} - Hit rate: {:.2f}% ({} hits, {} misses)", 
                    cacheType, hitRate, hits, misses);
        }
        
        log.info("=== End Performance Report ===");
    }

    private void recordResponseTime(String endpoint, long responseTime) {
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String timeKey = RESPONSE_TIME_PREFIX + endpoint + ":" + dateKey;
        
        String currentAvgStr = redisTemplate.opsForValue().get(timeKey);
        if (currentAvgStr == null) {
            redisTemplate.opsForValue().set(timeKey, String.valueOf(responseTime), Duration.ofHours(24));
        } else {
            long currentAvg = Long.parseLong(currentAvgStr);
            long newAvg = (currentAvg + responseTime) / 2;
            redisTemplate.opsForValue().set(timeKey, String.valueOf(newAvg), Duration.ofHours(24));
        }
    }

    private void incrementRequestCount(String endpoint) {
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String countKey = REQUEST_COUNT_PREFIX + endpoint + ":" + dateKey;
        
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, 24, TimeUnit.HOURS);
    }

    private void incrementErrorCount(String endpoint) {
        String dateKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String countKey = ERROR_COUNT_PREFIX + endpoint + ":" + dateKey;
        
        redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, 24, TimeUnit.HOURS);
    }

    private long getMetric(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }
}