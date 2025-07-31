package com.hanihome.hanihome_au_api.config;

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

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Property details cache - 1 hour TTL
        cacheConfigurations.put("propertyDetails", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Property list cache - 15 minutes TTL
        cacheConfigurations.put("propertyList", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Property search cache - 10 minutes TTL  
        cacheConfigurations.put("propertySearch", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Property statistics cache - 4 hours TTL
        cacheConfigurations.put("propertyStats", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // Property images cache - 24 hours TTL
        cacheConfigurations.put("propertyImages", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Similar properties cache - 2 hours TTL
        cacheConfigurations.put("similarProperties", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Nearby properties cache - 30 minutes TTL
        cacheConfigurations.put("nearbyProperties", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}