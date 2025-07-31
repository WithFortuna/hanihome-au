package com.hanihome.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitingService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";

    public boolean isRateLimited(String identifier, int maxAttempts, Duration window) {
        String key = RATE_LIMIT_PREFIX + identifier;
        
        String currentCount = redisTemplate.opsForValue().get(key);
        if (currentCount == null) {
            // First attempt
            redisTemplate.opsForValue().set(key, "1", window);
            return false;
        }
        
        int count = Integer.parseInt(currentCount);
        if (count >= maxAttempts) {
            log.warn("Rate limit exceeded for identifier: {}, attempts: {}", identifier, count);
            return true;
        }
        
        // Increment counter
        redisTemplate.opsForValue().increment(key);
        return false;
    }

    public boolean isLoginRateLimited(String ipAddress, int maxAttempts, Duration window) {
        return isRateLimited(LOGIN_ATTEMPT_PREFIX + ipAddress, maxAttempts, window);
    }

    public void recordFailedLogin(String ipAddress, Duration window) {
        String key = LOGIN_ATTEMPT_PREFIX + ipAddress;
        String currentCount = redisTemplate.opsForValue().get(key);
        
        if (currentCount == null) {
            redisTemplate.opsForValue().set(key, "1", window);
        } else {
            redisTemplate.opsForValue().increment(key);
        }
        
        log.info("Recorded failed login attempt for IP: {}", ipAddress);
    }

    public void resetFailedLogins(String ipAddress) {
        String key = LOGIN_ATTEMPT_PREFIX + ipAddress;
        redisTemplate.delete(key);
        log.info("Reset failed login attempts for IP: {}", ipAddress);
    }

    public int getFailedLoginCount(String ipAddress) {
        String key = LOGIN_ATTEMPT_PREFIX + ipAddress;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    public long getRemainingLockoutTime(String ipAddress, Duration window) {
        String key = LOGIN_ATTEMPT_PREFIX + ipAddress;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }

    public void clearRateLimit(String identifier) {
        String key = RATE_LIMIT_PREFIX + identifier;
        redisTemplate.delete(key);
        log.info("Cleared rate limit for identifier: {}", identifier);
    }

    public boolean isUserActionRateLimited(Long userId, String action, int maxAttempts, Duration window) {
        String identifier = String.format("user:%d:action:%s", userId, action);
        return isRateLimited(identifier, maxAttempts, window);
    }
}