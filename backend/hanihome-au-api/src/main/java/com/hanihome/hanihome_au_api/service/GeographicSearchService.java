package com.hanihome.hanihome_au_api.service;

import com.hanihome.hanihome_au_api.dto.request.GeographicSearchRequest;
import com.hanihome.hanihome_au_api.dto.response.PropertyWithDistanceResponse;
import com.hanihome.hanihome_au_api.repository.GeographicPropertySearchRepository;
import com.hanihome.hanihome_au_api.repository.GeographicPropertySearchRepository.PropertyWithDistance;
import com.hanihome.hanihome_au_api.repository.GeographicPropertySearchRepository.PropertyCluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling geographic-based property searches
 * Provides optimized location-based search functionality with caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GeographicSearchService {

    private final GeographicPropertySearchRepository geographicRepository;

    /**
     * Search for properties within a radius of a given point
     */
    public Page<PropertyWithDistanceResponse> searchPropertiesWithinRadius(GeographicSearchRequest request) {
        log.debug("Searching properties within {}km of ({}, {})", 
            request.getRadiusKm(), request.getLatitude(), request.getLongitude());

        // Validate request
        request.validateSearchType();
        
        // Create pageable object
        Pageable pageable = createPageable(request);
        
        // Perform the search
        Page<PropertyWithDistance> results = geographicRepository.findPropertiesWithinRadius(
            request.getLatitude(),
            request.getLongitude(), 
            request.getRadiusKm(),
            request,
            pageable
        );

        // Convert to response DTOs
        return results.map(this::convertToResponse);
    }

    /**
     * Search for properties within a bounding box
     */
    public Page<PropertyWithDistanceResponse> searchPropertiesWithinBounds(GeographicSearchRequest request) {
        log.debug("Searching properties within bounds: N:{}, S:{}, E:{}, W:{}", 
            request.getNorthLatitude(), request.getSouthLatitude(), 
            request.getEastLongitude(), request.getWestLongitude());

        // Validate request
        request.validateSearchType();
        
        // Create pageable object
        Pageable pageable = createPageable(request);
        
        // Perform the search
        Page<PropertyWithDistance> results = geographicRepository.findPropertiesWithinBounds(
            request.getNorthLatitude(),
            request.getSouthLatitude(),
            request.getEastLongitude(),
            request.getWestLongitude(),
            request,
            pageable
        );

        // Convert to response DTOs
        return results.map(this::convertToResponse);
    }

    /**
     * Find nearest properties to a specific location
     */
    @Cacheable(value = "nearestProperties", key = "#latitude + '_' + #longitude + '_' + #limit")
    public List<PropertyWithDistanceResponse> findNearestProperties(
            BigDecimal latitude, BigDecimal longitude, int limit) {
        
        log.debug("Finding {} nearest properties to ({}, {})", limit, latitude, longitude);

        List<PropertyWithDistance> results = geographicRepository.findNearestProperties(
            latitude, longitude, limit
        );

        return results.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get property clusters for map display optimization
     */
    @Cacheable(value = "propertyClusters", 
               key = "#northLat + '_' + #southLat + '_' + #eastLng + '_' + #westLng + '_' + #zoomLevel")
    public List<PropertyClusterResponse> getPropertyClusters(
            BigDecimal northLat, BigDecimal southLat, 
            BigDecimal eastLng, BigDecimal westLng, 
            int zoomLevel) {
        
        log.debug("Getting property clusters for zoom level {} within bounds: N:{}, S:{}, E:{}, W:{}", 
            zoomLevel, northLat, southLat, eastLng, westLng);

        List<PropertyCluster> clusters = geographicRepository.getPropertyClusters(
            northLat, southLat, eastLng, westLng, zoomLevel
        );

        return clusters.stream()
            .map(this::convertToClusterResponse)
            .collect(Collectors.toList());
    }

    /**
     * Calculate distance between two geographic points
     */
    public BigDecimal calculateDistance(
            BigDecimal lat1, BigDecimal lng1, 
            BigDecimal lat2, BigDecimal lng2) {
        
        return geographicRepository.calculateDistance(lat1, lng1, lat2, lng2);
    }

    /**
     * Calculate bearing from one point to another
     */
    public Integer calculateBearing(
            BigDecimal lat1, BigDecimal lng1, 
            BigDecimal lat2, BigDecimal lng2) {
        
        return geographicRepository.calculateBearing(lat1, lng1, lat2, lng2);
    }

    /**
     * Perform a comprehensive geographic search based on request type
     */
    public Page<PropertyWithDistanceResponse> performGeographicSearch(GeographicSearchRequest request) {
        if (request.isRadiusSearch()) {
            return searchPropertiesWithinRadius(request);
        } else if (request.isBoundingBoxSearch()) {
            return searchPropertiesWithinBounds(request);
        } else {
            throw new IllegalArgumentException("Invalid search request: must specify either radius or bounding box");
        }
    }

    /**
     * Create Pageable object from request parameters
     */
    private Pageable createPageable(GeographicSearchRequest request) {
        Sort sort = createSort(request);
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    /**
     * Create Sort object based on request parameters
     */
    private Sort createSort(GeographicSearchRequest request) {
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;

        return switch (request.getSortBy().toLowerCase()) {
            case "distance" -> Sort.by(direction, "distance"); // Will be handled in application logic
            case "price", "rent" -> Sort.by(direction, "monthlyRent");
            case "area" -> Sort.by(direction, "area");
            case "rooms" -> Sort.by(direction, "rooms");
            case "created" -> Sort.by(direction, "createdDate");
            case "modified" -> Sort.by(direction, "modifiedDate");
            default -> Sort.by(Sort.Direction.ASC, "distance");
        };
    }

    /**
     * Convert PropertyWithDistance to response DTO
     */
    private PropertyWithDistanceResponse convertToResponse(PropertyWithDistance pwd) {
        return PropertyWithDistanceResponse.fromPropertyWithDistance(
            pwd.getProperty(),
            pwd.getDistanceKm(),
            pwd.getBearing()
        );
    }

    /**
     * Convert PropertyCluster to response DTO
     */
    private PropertyClusterResponse convertToClusterResponse(PropertyCluster cluster) {
        return PropertyClusterResponse.builder()
            .centerLatitude(cluster.getCenterLatitude())
            .centerLongitude(cluster.getCenterLongitude())
            .count(cluster.getCount())
            .propertyIds(cluster.getPropertyIds())
            .averagePrice(cluster.getAveragePrice())
            .build();
    }

    /**
     * Response DTO for property clusters
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PropertyClusterResponse {
        private BigDecimal centerLatitude;
        private BigDecimal centerLongitude;
        private Integer count;
        private List<Long> propertyIds;
        private BigDecimal averagePrice;
    }
}