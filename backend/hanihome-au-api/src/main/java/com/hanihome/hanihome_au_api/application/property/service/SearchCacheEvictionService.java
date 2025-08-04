package com.hanihome.hanihome_au_api.application.property.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchCacheEvictionService {

    /**
     * Evict all search cache when properties are modified
     */
    @CacheEvict(value = "propertySearch", allEntries = true)
    public void evictAllSearchCache() {
        log.info("Evicted all property search cache entries");
    }

    /**
     * Evict property-related caches when a property is modified
     */
    @CacheEvict(value = {"propertySearch", "propertyDetails", "similarProperties", "nearbyProperties"}, allEntries = true)
    public void evictPropertyRelatedCache() {
        log.info("Evicted all property-related cache entries");
    }

    /**
     * Evict user-specific caches
     */
    @CacheEvict(value = {"userFavorites", "favoriteStats"}, allEntries = true)
    public void evictUserSpecificCache() {
        log.info("Evicted all user-specific cache entries");
    }

    /**
     * Evict specific property cache by ID
     */
    @CacheEvict(value = "propertyDetails", key = "#propertyId")
    public void evictPropertyCache(Long propertyId) {
        log.info("Evicted cache for property ID: {}", propertyId);
    }
}