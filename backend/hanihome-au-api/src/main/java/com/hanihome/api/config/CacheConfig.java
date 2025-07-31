package com.hanihome.api.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configure JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User profile cache - longer TTL
        cacheConfigurations.put("userProfile", defaultConfig
                .entryTtl(Duration.ofHours(2))
                .prefixCacheNameWith("hanihome:user:"));

        // Session cache - shorter TTL
        cacheConfigurations.put("userSession", defaultConfig
                .entryTtl(Duration.ofMinutes(15))
                .prefixCacheNameWith("hanihome:session:"));

        // JWT blacklist cache - TTL based on token expiration
        cacheConfigurations.put("jwtBlacklist", defaultConfig
                .entryTtl(Duration.ofHours(24))
                .prefixCacheNameWith("hanihome:jwt:blacklist:"));

        // Rate limiting cache - short TTL
        cacheConfigurations.put("rateLimit", defaultConfig
                .entryTtl(Duration.ofMinutes(5))
                .prefixCacheNameWith("hanihome:ratelimit:"));

        // OAuth state cache - very short TTL
        cacheConfigurations.put("oauthState", defaultConfig
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("hanihome:oauth:"));

        // File metadata cache - medium TTL
        cacheConfigurations.put("fileMetadata", defaultConfig
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("hanihome:file:"));

        // Public profile cache - longer TTL since it changes less frequently
        cacheConfigurations.put("publicProfile", defaultConfig
                .entryTtl(Duration.ofHours(6))
                .prefixCacheNameWith("hanihome:public:"));

        log.info("Configured Redis cache manager with {} cache configurations", cacheConfigurations.size());

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}