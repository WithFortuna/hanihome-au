package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.dto.request.GeographicSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for geographic property searches
 */
public interface GeographicPropertySearchRepository {

    /**
     * Find properties within a radius of a given point
     */
    Page<PropertyWithDistance> findPropertiesWithinRadius(
            BigDecimal latitude, 
            BigDecimal longitude, 
            BigDecimal radiusKm,
            GeographicSearchRequest filters,
            Pageable pageable
    );

    /**
     * Find properties within a bounding box
     */
    Page<PropertyWithDistance> findPropertiesWithinBounds(
            BigDecimal northLatitude,
            BigDecimal southLatitude,
            BigDecimal eastLongitude,
            BigDecimal westLongitude,
            GeographicSearchRequest filters,
            Pageable pageable
    );

    /**
     * Find nearest properties to a given point
     */
    List<PropertyWithDistance> findNearestProperties(
            BigDecimal latitude,
            BigDecimal longitude,
            int limit
    );

    /**
     * Calculate distance between two geographic points
     */
    BigDecimal calculateDistance(
            BigDecimal lat1, BigDecimal lng1,
            BigDecimal lat2, BigDecimal lng2
    );

    /**
     * Calculate bearing from one point to another
     */
    Integer calculateBearing(
            BigDecimal lat1, BigDecimal lng1,
            BigDecimal lat2, BigDecimal lng2
    );

    /**
     * Get properties clustered by geographic proximity
     */
    List<PropertyCluster> getPropertyClusters(
            BigDecimal northLatitude,
            BigDecimal southLatitude,
            BigDecimal eastLongitude,
            BigDecimal westLongitude,
            int zoomLevel
    );

    /**
     * Inner class to represent property with distance information
     */
    class PropertyWithDistance {
        private final Property property;
        private final BigDecimal distanceKm;
        private final Integer bearing;

        public PropertyWithDistance(Property property, BigDecimal distanceKm, Integer bearing) {
            this.property = property;
            this.distanceKm = distanceKm;
            this.bearing = bearing;
        }

        public Property getProperty() { return property; }
        public BigDecimal getDistanceKm() { return distanceKm; }
        public Integer getBearing() { return bearing; }
    }

    /**
     * Inner class to represent property clusters for map display
     */
    class PropertyCluster {
        private final BigDecimal centerLatitude;
        private final BigDecimal centerLongitude;
        private final Integer count;
        private final List<Long> propertyIds;
        private final BigDecimal averagePrice;

        public PropertyCluster(BigDecimal centerLatitude, BigDecimal centerLongitude, 
                             Integer count, List<Long> propertyIds, BigDecimal averagePrice) {
            this.centerLatitude = centerLatitude;
            this.centerLongitude = centerLongitude;
            this.count = count;
            this.propertyIds = propertyIds;
            this.averagePrice = averagePrice;
        }

        public BigDecimal getCenterLatitude() { return centerLatitude; }
        public BigDecimal getCenterLongitude() { return centerLongitude; }
        public Integer getCount() { return count; }
        public List<Long> getPropertyIds() { return propertyIds; }
        public BigDecimal getAveragePrice() { return averagePrice; }
    }
}