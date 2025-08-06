package com.hanihome.hanihome_au_api.application.property.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchPerformanceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    private static final String SEARCH_STATS_PREFIX = "search:stats:";
    private static final String SLOW_QUERY_PREFIX = "search:slow:";
    private static final String CACHE_STATS_PREFIX = "cache:stats:";

    /**
     * Record search performance metrics
     */
    public void recordSearchMetrics(String queryHash, long executionTimeMs, int resultCount, boolean fromCache) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH"));
            String key = SEARCH_STATS_PREFIX + timestamp;

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("queryHash", queryHash);
            metrics.put("executionTime", executionTimeMs);
            metrics.put("resultCount", resultCount);
            metrics.put("fromCache", fromCache);
            metrics.put("timestamp", LocalDateTime.now().toString());

            // Store in Redis hash for hourly aggregation
            redisTemplate.opsForHash().put(key, queryHash + ":" + System.currentTimeMillis(), metrics);
            redisTemplate.expire(key, 7, TimeUnit.DAYS); // Keep for 7 days

            // Record slow queries (> 1000ms)
            if (executionTimeMs > 1000 && !fromCache) {
                recordSlowQuery(queryHash, executionTimeMs, resultCount);
            }

            // Update cache statistics
            updateCacheStats(fromCache);

            log.debug("Recorded search metrics - Hash: {}, Time: {}ms, Results: {}, FromCache: {}", 
                     queryHash, executionTimeMs, resultCount, fromCache);

        } catch (Exception e) {
            log.error("Failed to record search metrics", e);
        }
    }

    /**
     * Record slow query for analysis
     */
    private void recordSlowQuery(String queryHash, long executionTimeMs, int resultCount) {
        String key = SLOW_QUERY_PREFIX + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        Map<String, Object> slowQuery = new HashMap<>();
        slowQuery.put("queryHash", queryHash);
        slowQuery.put("executionTime", executionTimeMs);
        slowQuery.put("resultCount", resultCount);
        slowQuery.put("timestamp", LocalDateTime.now().toString());

        redisTemplate.opsForList().leftPush(key, slowQuery);
        redisTemplate.expire(key, 30, TimeUnit.DAYS); // Keep slow queries for 30 days

        log.warn("Slow query detected - Hash: {}, Time: {}ms, Results: {}", 
                queryHash, executionTimeMs, resultCount);
    }

    /**
     * Update cache hit/miss statistics
     */
    private void updateCacheStats(boolean fromCache) {
        String key = CACHE_STATS_PREFIX + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        if (fromCache) {
            redisTemplate.opsForHash().increment(key, "hits", 1);
        } else {
            redisTemplate.opsForHash().increment(key, "misses", 1);
        }
        
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }

    /**
     * Get cache hit rate for today
     */
    public double getCacheHitRate() {
        String key = CACHE_STATS_PREFIX + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        try {
            Long hits = (Long) redisTemplate.opsForHash().get(key, "hits");
            Long misses = (Long) redisTemplate.opsForHash().get(key, "misses");
            
            if (hits == null) hits = 0L;
            if (misses == null) misses = 0L;
            
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
            
        } catch (Exception e) {
            log.error("Failed to calculate cache hit rate", e);
            return 0.0;
        }
    }

    /**
     * Get search statistics summary
     */
    public Map<String, Object> getSearchStatsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // Cache statistics
            String cacheKey = CACHE_STATS_PREFIX + today;
            Long hits = (Long) redisTemplate.opsForHash().get(cacheKey, "hits");
            Long misses = (Long) redisTemplate.opsForHash().get(cacheKey, "misses");
            
            hits = hits != null ? hits : 0L;
            misses = misses != null ? misses : 0L;
            
            double hitRate = (hits + misses) > 0 ? (double) hits / (hits + misses) : 0.0;
            
            summary.put("cacheHits", hits);
            summary.put("cacheMisses", misses);
            summary.put("cacheHitRate", hitRate);
            
            // Slow query count
            String slowQueryKey = SLOW_QUERY_PREFIX + today;
            Long slowQueryCount = redisTemplate.opsForList().size(slowQueryKey);
            summary.put("slowQueryCount", slowQueryCount != null ? slowQueryCount : 0L);
            
            // Search count for current hour
            String currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH"));
            String searchKey = SEARCH_STATS_PREFIX + currentHour;
            Long searchCount = redisTemplate.opsForHash().size(searchKey);
            summary.put("currentHourSearches", searchCount != null ? searchCount : 0L);
            
        } catch (Exception e) {
            log.error("Failed to get search stats summary", e);
        }
        
        return summary;
    }

    /**
     * Get slow queries for analysis
     */
    public java.util.List<Object> getSlowQueries(int limit) {
        String key = SLOW_QUERY_PREFIX + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return redisTemplate.opsForList().range(key, 0, limit - 1);
    }

    /**
     * Clear old cache entries manually
     */
    @SuppressWarnings("unchecked")
    public void clearOldCacheEntries() {
        try {
            if (cacheManager.getCache("propertySearch") != null) {
                cacheManager.getCache("propertySearch").clear();
                log.info("Cleared propertySearch cache");
            }
        } catch (Exception e) {
            log.error("Failed to clear cache entries", e);
        }
    }

    /**
     * Preload popular searches into cache
     */
    public void preloadPopularSearches() {
        // This would typically analyze search patterns and preload common queries
        // Implementation depends on specific business requirements
        log.info("Preloading popular searches into cache...");
    }
}