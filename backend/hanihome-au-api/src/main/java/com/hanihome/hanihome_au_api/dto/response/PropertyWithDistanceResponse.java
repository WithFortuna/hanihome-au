package com.hanihome.hanihome_au_api.dto.response;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyWithDistanceResponse {
    
    private Long id;
    private String title;
    private String description;
    private String address;
    private String detailAddress;
    private String zipCode;
    private String city;
    private String district;
    
    private PropertyType propertyType;
    private RentalType rentalType;
    private PropertyStatus status;
    
    private BigDecimal deposit;
    private BigDecimal monthlyRent;
    private BigDecimal maintenanceFee;
    private BigDecimal area;
    
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer totalFloors;
    
    private LocalDate availableDate;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    private Boolean parkingAvailable;
    private Boolean petAllowed;
    private Boolean furnished;
    private Boolean shortTermAvailable;
    
    private List<String> options;
    private List<String> imageUrls;
    
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    
    // Geographic information
    private BigDecimal distanceKm;
    private String distanceDisplay;
    private Integer bearing; // Direction from search center in degrees (0-359)
    private String direction; // Compass direction (N, NE, E, SE, S, SW, W, NW)
    
    // Additional computed fields
    private BigDecimal pricePerSqm; // Monthly rent per square meter
    private String priceRange; // Budget-friendly, Mid-range, Premium, Luxury
    
    public static PropertyWithDistanceResponse fromProperty(Property property) {
        return PropertyWithDistanceResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .address(property.getAddress())
                .detailAddress(property.getDetailAddress())
                .zipCode(property.getZipCode())
                .city(property.getCity())
                .district(property.getDistrict())
                .propertyType(property.getPropertyType())
                .rentalType(property.getRentalType())
                .status(property.getStatus())
                .deposit(property.getDeposit())
                .monthlyRent(property.getMonthlyRent())
                .maintenanceFee(property.getMaintenanceFee())
                .area(property.getArea())
                .rooms(property.getRooms())
                .bathrooms(property.getBathrooms())
                .floor(property.getFloor())
                .totalFloors(property.getTotalFloors())
                .availableDate(property.getAvailableDate())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .parkingAvailable(property.getParkingAvailable())
                .petAllowed(property.getPetAllowed())
                .furnished(property.getFurnished())
                .shortTermAvailable(property.getShortTermAvailable())
                .options(property.getOptions())
                .imageUrls(property.getImageUrls())
                .createdDate(property.getCreatedDate())
                .modifiedDate(property.getModifiedDate())
                .build();
    }
    
    public static PropertyWithDistanceResponse fromPropertyWithDistance(
            Property property, 
            BigDecimal distanceKm,
            Integer bearing) {
        
        PropertyWithDistanceResponse response = fromProperty(property);
        response.setDistanceKm(distanceKm);
        response.setDistanceDisplay(formatDistance(distanceKm));
        response.setBearing(bearing);
        response.setDirection(bearingToDirection(bearing));
        
        // Calculate price per square meter if area is available
        if (property.getArea() != null && property.getArea().compareTo(BigDecimal.ZERO) > 0 && 
            property.getMonthlyRent() != null) {
            response.setPricePerSqm(
                property.getMonthlyRent()
                    .divide(property.getArea(), 2, RoundingMode.HALF_UP)
            );
        }
        
        // Determine price range
        if (property.getMonthlyRent() != null) {
            response.setPriceRange(determinePriceRange(property.getMonthlyRent()));
        }
        
        return response;
    }
    
    private static String formatDistance(BigDecimal distanceKm) {
        if (distanceKm == null) return null;
        
        if (distanceKm.compareTo(BigDecimal.ONE) < 0) {
            // Less than 1km, show in meters
            int meters = distanceKm.multiply(BigDecimal.valueOf(1000)).intValue();
            return meters + "m";
        } else {
            // 1km or more, show in km with 1 decimal place
            return distanceKm.setScale(1, RoundingMode.HALF_UP) + "km";
        }
    }
    
    private static String bearingToDirection(Integer bearing) {
        if (bearing == null) return null;
        
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(bearing / 45.0) % 8;
        return directions[index];
    }
    
    private static String determinePriceRange(BigDecimal monthlyRent) {
        if (monthlyRent == null) return "Unknown";
        
        // These ranges are for Australian rental market (weekly rent converted to monthly)
        if (monthlyRent.compareTo(BigDecimal.valueOf(1200)) <= 0) {
            return "Budget-friendly";
        } else if (monthlyRent.compareTo(BigDecimal.valueOf(2000)) <= 0) {
            return "Mid-range";
        } else if (monthlyRent.compareTo(BigDecimal.valueOf(3500)) <= 0) {
            return "Premium";
        } else {
            return "Luxury";
        }
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class GeographicSearchResponse {
    
    private List<PropertyWithDistanceResponse> properties;
    private SearchMetadata metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMetadata {
        private BigDecimal searchLatitude;
        private BigDecimal searchLongitude;
        private BigDecimal searchRadiusKm;
        
        // Bounding box for bounding box searches
        private BigDecimal northLatitude;
        private BigDecimal southLatitude;
        private BigDecimal eastLongitude;
        private BigDecimal westLongitude;
        
        private Integer totalResults;
        private Integer page;
        private Integer size;
        private Integer totalPages;
        private String sortBy;
        private String sortDirection;
        
        // Statistics
        private BigDecimal averageDistance;
        private BigDecimal minDistance;
        private BigDecimal maxDistance;
        private BigDecimal averagePrice;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        
        // Geographic bounds of results
        private BigDecimal resultsBoundNorth;
        private BigDecimal resultsBoundSouth;
        private BigDecimal resultsBoundEast;
        private BigDecimal resultsBoundWest;
        
        private Long searchTimeMs;
    }
}