package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.dto.request.GeographicSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hanihome.hanihome_au_api.domain.entity.QProperty.property;

/**
 * Implementation of geographic property search repository using QueryDSL
 * Implements optimized geographic searches with Haversine formula for distance calculations
 */
@Repository
@RequiredArgsConstructor
public class GeographicPropertySearchRepositoryImpl implements GeographicPropertySearchRepository {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    // Earth's radius in kilometers
    private static final BigDecimal EARTH_RADIUS_KM = new BigDecimal("6371.0");
    private static final BigDecimal RAD_CONVERSION = new BigDecimal(Math.PI / 180.0);

    @Override
    public Page<PropertyWithDistance> findPropertiesWithinRadius(
            BigDecimal latitude, 
            BigDecimal longitude, 
            BigDecimal radiusKm,
            GeographicSearchRequest filters,
            Pageable pageable) {

        BooleanBuilder whereClause = buildSearchFilters(filters);
        
        // Add radius filter using bounding box for initial filter (optimization)
        BigDecimal latDelta = radiusKm.divide(new BigDecimal("111.0"), 6, RoundingMode.HALF_UP);
        BigDecimal lngDelta = radiusKm.divide(
            new BigDecimal("111.0").multiply(
                new BigDecimal(Math.cos(latitude.doubleValue() * Math.PI / 180.0))
            ), 6, RoundingMode.HALF_UP
        );

        whereClause.and(property.latitude.between(
            latitude.subtract(latDelta), 
            latitude.add(latDelta)
        ));
        whereClause.and(property.longitude.between(
            longitude.subtract(lngDelta), 
            longitude.add(lngDelta)
        ));

        // Build the main query with distance calculation
        JPAQuery<Property> baseQuery = queryFactory
            .selectFrom(property)
            .where(whereClause)
            .orderBy(property.createdDate.desc());

        // Get total count
        long total = baseQuery.fetchCount();

        // Fetch results with pagination
        List<Property> properties = baseQuery
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // Calculate distances and filter by exact radius
        List<PropertyWithDistance> propertiesWithDistance = properties.stream()
            .map(prop -> {
                BigDecimal distance = calculateDistance(latitude, longitude, 
                    prop.getLatitude(), prop.getLongitude());
                Integer bearing = calculateBearing(latitude, longitude,
                    prop.getLatitude(), prop.getLongitude());
                return new PropertyWithDistance(prop, distance, bearing);
            })
            .filter(pwd -> pwd.getDistanceKm().compareTo(radiusKm) <= 0)
            .sorted((a, b) -> a.getDistanceKm().compareTo(b.getDistanceKm()))
            .collect(Collectors.toList());

        return new PageImpl<>(propertiesWithDistance, pageable, total);
    }

    @Override
    public Page<PropertyWithDistance> findPropertiesWithinBounds(
            BigDecimal northLatitude,
            BigDecimal southLatitude,
            BigDecimal eastLongitude,
            BigDecimal westLongitude,
            GeographicSearchRequest filters,
            Pageable pageable) {

        BooleanBuilder whereClause = buildSearchFilters(filters);
        
        // Add bounding box constraints
        whereClause.and(property.latitude.between(southLatitude, northLatitude));
        whereClause.and(property.longitude.between(westLongitude, eastLongitude));

        JPAQuery<Property> baseQuery = queryFactory
            .selectFrom(property)
            .where(whereClause)
            .orderBy(property.createdDate.desc());

        long total = baseQuery.fetchCount();

        List<Property> properties = baseQuery
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // Calculate center point for distance calculations
        BigDecimal centerLat = northLatitude.add(southLatitude).divide(new BigDecimal("2.0"));
        BigDecimal centerLng = eastLongitude.add(westLongitude).divide(new BigDecimal("2.0"));

        List<PropertyWithDistance> propertiesWithDistance = properties.stream()
            .map(prop -> {
                BigDecimal distance = calculateDistance(centerLat, centerLng,
                    prop.getLatitude(), prop.getLongitude());
                Integer bearing = calculateBearing(centerLat, centerLng,
                    prop.getLatitude(), prop.getLongitude());
                return new PropertyWithDistance(prop, distance, bearing);
            })
            .collect(Collectors.toList());

        return new PageImpl<>(propertiesWithDistance, pageable, total);
    }

    @Override
    public List<PropertyWithDistance> findNearestProperties(
            BigDecimal latitude,
            BigDecimal longitude,
            int limit) {

        // Use a reasonable search radius (50km) for initial filtering
        BigDecimal searchRadius = new BigDecimal("50.0");
        BigDecimal latDelta = searchRadius.divide(new BigDecimal("111.0"), 6, RoundingMode.HALF_UP);
        BigDecimal lngDelta = searchRadius.divide(
            new BigDecimal("111.0").multiply(
                new BigDecimal(Math.cos(latitude.doubleValue() * Math.PI / 180.0))
            ), 6, RoundingMode.HALF_UP
        );

        List<Property> properties = queryFactory
            .selectFrom(property)
            .where(property.status.eq(PropertyStatus.ACTIVE)
                .and(property.latitude.between(latitude.subtract(latDelta), latitude.add(latDelta)))
                .and(property.longitude.between(longitude.subtract(lngDelta), longitude.add(lngDelta))))
            .fetch();

        return properties.stream()
            .map(prop -> {
                BigDecimal distance = calculateDistance(latitude, longitude,
                    prop.getLatitude(), prop.getLongitude());
                Integer bearing = calculateBearing(latitude, longitude,
                    prop.getLatitude(), prop.getLongitude());
                return new PropertyWithDistance(prop, distance, bearing);
            })
            .sorted((a, b) -> a.getDistanceKm().compareTo(b.getDistanceKm()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return BigDecimal.ZERO;
        }

        // Haversine formula implementation
        BigDecimal dLat = lat2.subtract(lat1).multiply(RAD_CONVERSION);
        BigDecimal dLng = lng2.subtract(lng1).multiply(RAD_CONVERSION);
        
        BigDecimal lat1Rad = lat1.multiply(RAD_CONVERSION);
        BigDecimal lat2Rad = lat2.multiply(RAD_CONVERSION);

        double a = Math.sin(dLat.doubleValue() / 2) * Math.sin(dLat.doubleValue() / 2) +
                   Math.cos(lat1Rad.doubleValue()) * Math.cos(lat2Rad.doubleValue()) *
                   Math.sin(dLng.doubleValue() / 2) * Math.sin(dLng.doubleValue() / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM.multiply(new BigDecimal(c)).setScale(3, RoundingMode.HALF_UP);
    }

    @Override
    public Integer calculateBearing(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return 0;
        }

        BigDecimal dLng = lng2.subtract(lng1).multiply(RAD_CONVERSION);
        BigDecimal lat1Rad = lat1.multiply(RAD_CONVERSION);
        BigDecimal lat2Rad = lat2.multiply(RAD_CONVERSION);

        double y = Math.sin(dLng.doubleValue()) * Math.cos(lat2Rad.doubleValue());
        double x = Math.cos(lat1Rad.doubleValue()) * Math.sin(lat2Rad.doubleValue()) -
                   Math.sin(lat1Rad.doubleValue()) * Math.cos(lat2Rad.doubleValue()) * Math.cos(dLng.doubleValue());

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (int) ((bearing + 360) % 360);
    }

    @Override
    public List<PropertyCluster> getPropertyClusters(
            BigDecimal northLatitude,
            BigDecimal southLatitude,
            BigDecimal eastLongitude,
            BigDecimal westLongitude,
            int zoomLevel) {

        // Determine cluster grid size based on zoom level
        double gridSize = calculateGridSize(zoomLevel);
        
        List<Property> properties = queryFactory
            .selectFrom(property)
            .where(property.status.eq(PropertyStatus.ACTIVE)
                .and(property.latitude.between(southLatitude, northLatitude))
                .and(property.longitude.between(westLongitude, eastLongitude)))
            .fetch();

        // Group properties into clusters based on grid
        Map<String, List<Property>> clusters = properties.stream()
            .collect(Collectors.groupingBy(prop -> {
                int latGrid = (int) (prop.getLatitude().doubleValue() / gridSize);
                int lngGrid = (int) (prop.getLongitude().doubleValue() / gridSize);
                return latGrid + "," + lngGrid;
            }));

        // Convert to PropertyCluster objects
        return clusters.entrySet().stream()
            .map(entry -> {
                List<Property> clusterProps = entry.getValue();
                
                // Calculate cluster center
                double avgLat = clusterProps.stream()
                    .mapToDouble(p -> p.getLatitude().doubleValue())
                    .average().orElse(0.0);
                double avgLng = clusterProps.stream()
                    .mapToDouble(p -> p.getLongitude().doubleValue())
                    .average().orElse(0.0);
                
                // Calculate average price
                BigDecimal avgPrice = clusterProps.stream()
                    .filter(p -> p.getMonthlyRent() != null)
                    .map(Property::getMonthlyRent)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(clusterProps.size()), 2, RoundingMode.HALF_UP);

                List<Long> propertyIds = clusterProps.stream()
                    .map(Property::getId)
                    .collect(Collectors.toList());

                return new PropertyCluster(
                    new BigDecimal(avgLat).setScale(6, RoundingMode.HALF_UP),
                    new BigDecimal(avgLng).setScale(6, RoundingMode.HALF_UP),
                    clusterProps.size(),
                    propertyIds,
                    avgPrice
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Build search filters based on the request criteria
     */
    private BooleanBuilder buildSearchFilters(GeographicSearchRequest filters) {
        BooleanBuilder whereClause = new BooleanBuilder();
        
        // Always filter for active properties
        whereClause.and(property.status.eq(PropertyStatus.ACTIVE));
        
        if (filters == null) {
            return whereClause;
        }

        if (filters.getPropertyTypes() != null && !filters.getPropertyTypes().isEmpty()) {
            whereClause.and(property.propertyType.in(filters.getPropertyTypes()));
        }

        if (filters.getRentalTypes() != null && !filters.getRentalTypes().isEmpty()) {
            whereClause.and(property.rentalType.in(filters.getRentalTypes()));
        }

        if (filters.getMinMonthlyRent() != null) {
            whereClause.and(property.monthlyRent.goe(filters.getMinMonthlyRent()));
        }

        if (filters.getMaxMonthlyRent() != null) {
            whereClause.and(property.monthlyRent.loe(filters.getMaxMonthlyRent()));
        }

        if (filters.getMinDeposit() != null) {
            whereClause.and(property.deposit.goe(filters.getMinDeposit()));
        }

        if (filters.getMaxDeposit() != null) {
            whereClause.and(property.deposit.loe(filters.getMaxDeposit()));
        }

        if (filters.getMinRooms() != null) {
            whereClause.and(property.rooms.goe(filters.getMinRooms()));
        }

        if (filters.getMaxRooms() != null) {
            whereClause.and(property.rooms.loe(filters.getMaxRooms()));
        }

        if (filters.getMinBathrooms() != null) {
            whereClause.and(property.bathrooms.goe(filters.getMinBathrooms()));
        }

        if (filters.getMaxBathrooms() != null) {
            whereClause.and(property.bathrooms.loe(filters.getMaxBathrooms()));
        }

        if (filters.getMinArea() != null) {
            whereClause.and(property.area.goe(filters.getMinArea()));
        }

        if (filters.getMaxArea() != null) {
            whereClause.and(property.area.loe(filters.getMaxArea()));
        }

        if (filters.getParkingRequired() != null && filters.getParkingRequired()) {
            whereClause.and(property.parkingAvailable.isTrue());
        }

        if (filters.getPetAllowed() != null && filters.getPetAllowed()) {
            whereClause.and(property.petAllowed.isTrue());
        }

        if (filters.getFurnished() != null && filters.getFurnished()) {
            whereClause.and(property.furnished.isTrue());
        }

        if (filters.getShortTermAvailable() != null && filters.getShortTermAvailable()) {
            whereClause.and(property.shortTermAvailable.isTrue());
        }

        if (filters.getCities() != null && !filters.getCities().isEmpty()) {
            whereClause.and(property.city.in(filters.getCities()));
        }

        if (filters.getDistricts() != null && !filters.getDistricts().isEmpty()) {
            whereClause.and(property.district.in(filters.getDistricts()));
        }

        if (filters.getZipCodes() != null && !filters.getZipCodes().isEmpty()) {
            whereClause.and(property.zipCode.in(filters.getZipCodes()));
        }

        return whereClause;
    }

    /**
     * Calculate grid size for clustering based on zoom level
     */
    private double calculateGridSize(int zoomLevel) {
        // Grid size decreases as zoom level increases
        // Zoom levels typically range from 1-20
        double baseGridSize = 0.1; // Degrees
        return baseGridSize / Math.pow(2, Math.max(0, zoomLevel - 10));
    }
}