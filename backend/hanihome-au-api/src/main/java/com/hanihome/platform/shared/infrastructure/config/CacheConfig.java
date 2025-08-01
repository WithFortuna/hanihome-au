package com.hanihome.platform.shared.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Consolidated Cache Configuration
 * Merges cache settings from both api and hanihome_au_api packages
 * Provides Redis-based caching with fallback and domain-specific cache configurations
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // Cache names for different domains
    public static final String PROPERTY_CACHE = "properties";
    public static final String PROPERTY_SEARCH_CACHE = "property-search";
    public static final String USER_CACHE = "users";
    public static final String GEOGRAPHIC_SEARCH_CACHE = "geographic-search";
    public static final String SECURITY_CACHE = "security";
    public static final String SESSION_CACHE = "sessions";
    public static final String PERFORMANCE_CACHE = "performance";

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Property domain caches
        cacheConfigurations.put(PROPERTY_CACHE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(PROPERTY_SEARCH_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // User domain caches
        cacheConfigurations.put(USER_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Geographic search caches (shorter TTL due to location-based data)
        cacheConfigurations.put(GEOGRAPHIC_SEARCH_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Security domain caches
        cacheConfigurations.put(SECURITY_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        
        // Session management caches (from legacy api package)
        cacheConfigurations.put(SESSION_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Performance monitoring caches
        cacheConfigurations.put(PERFORMANCE_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Fallback cache manager for when Redis is unavailable
     * Maintains cache functionality using in-memory caches
     */
    @Bean("fallbackCacheManager")
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager(
            PROPERTY_CACHE,
            PROPERTY_SEARCH_CACHE,
            USER_CACHE,
            GEOGRAPHIC_SEARCH_CACHE,
            SECURITY_CACHE,
            SESSION_CACHE,
            PERFORMANCE_CACHE
        );
    }
}