package com.hanihome.hanihome_au_api.dto.request;

import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeographicSearchRequest {

    // Geographic center point
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    // Search radius in kilometers
    @NotNull(message = "Radius is required")
    @DecimalMin(value = "0.1", message = "Radius must be at least 0.1 km")
    @DecimalMax(value = "100.0", message = "Radius cannot exceed 100 km")
    private BigDecimal radiusKm;

    // Bounding box search (alternative to radius)
    private BigDecimal northLatitude;
    private BigDecimal southLatitude;
    private BigDecimal eastLongitude;
    private BigDecimal westLongitude;

    // Property filters
    private List<PropertyType> propertyTypes;
    private List<RentalType> rentalTypes;
    private List<PropertyStatus> statuses;

    // Price range
    @Min(value = 0, message = "Minimum rent cannot be negative")
    private BigDecimal minMonthlyRent;
    
    @Min(value = 0, message = "Maximum rent cannot be negative")
    private BigDecimal maxMonthlyRent;

    @Min(value = 0, message = "Minimum deposit cannot be negative")
    private BigDecimal minDeposit;
    
    @Min(value = 0, message = "Maximum deposit cannot be negative")
    private BigDecimal maxDeposit;

    // Property specifications
    @Min(value = 0, message = "Minimum rooms cannot be negative")
    private Integer minRooms;
    
    @Min(value = 0, message = "Maximum rooms cannot be negative")
    private Integer maxRooms;

    @Min(value = 0, message = "Minimum bathrooms cannot be negative")
    private Integer minBathrooms;
    
    @Min(value = 0, message = "Maximum bathrooms cannot be negative")
    private Integer maxBathrooms;

    @DecimalMin(value = "0.0", message = "Minimum area cannot be negative")
    private BigDecimal minArea;
    
    @DecimalMin(value = "0.0", message = "Maximum area cannot be negative")
    private BigDecimal maxArea;

    // Amenity filters
    private Boolean parkingRequired;
    private Boolean petAllowed;
    private Boolean furnished;
    private Boolean shortTermAvailable;

    // Location filters
    private List<String> cities;
    private List<String> districts;
    private List<String> zipCodes;

    // Pagination
    @Min(value = 0, message = "Page number cannot be negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;

    // Sorting
    @Builder.Default
    private String sortBy = "distance";

    @Pattern(regexp = "asc|desc", message = "Sort direction must be 'asc' or 'desc'")
    @Builder.Default
    private String sortDirection = "asc";

    // Include distance in response
    @Builder.Default
    private Boolean includeDistance = true;

    // Validation method for bounding box
    public boolean isBoundingBoxSearch() {
        return northLatitude != null && southLatitude != null && 
               eastLongitude != null && westLongitude != null;
    }

    // Validation method for radius search
    public boolean isRadiusSearch() {
        return latitude != null && longitude != null && radiusKm != null;
    }

    public void validateSearchType() {
        if (!isRadiusSearch() && !isBoundingBoxSearch()) {
            throw new IllegalArgumentException("Either radius search or bounding box search parameters must be provided");
        }
        
        if (isRadiusSearch() && isBoundingBoxSearch()) {
            throw new IllegalArgumentException("Cannot use both radius search and bounding box search simultaneously");
        }

        if (isBoundingBoxSearch()) {
            if (northLatitude.compareTo(southLatitude) <= 0) {
                throw new IllegalArgumentException("North latitude must be greater than south latitude");
            }
            if (eastLongitude.compareTo(westLongitude) <= 0) {
                throw new IllegalArgumentException("East longitude must be greater than west longitude");
            }
        }

        if (minMonthlyRent != null && maxMonthlyRent != null && 
            minMonthlyRent.compareTo(maxMonthlyRent) > 0) {
            throw new IllegalArgumentException("Minimum monthly rent cannot be greater than maximum monthly rent");
        }

        if (minDeposit != null && maxDeposit != null && 
            minDeposit.compareTo(maxDeposit) > 0) {
            throw new IllegalArgumentException("Minimum deposit cannot be greater than maximum deposit");
        }

        if (minRooms != null && maxRooms != null && minRooms > maxRooms) {
            throw new IllegalArgumentException("Minimum rooms cannot be greater than maximum rooms");
        }

        if (minBathrooms != null && maxBathrooms != null && minBathrooms > maxBathrooms) {
            throw new IllegalArgumentException("Minimum bathrooms cannot be greater than maximum bathrooms");
        }

        if (minArea != null && maxArea != null && minArea.compareTo(maxArea) > 0) {
            throw new IllegalArgumentException("Minimum area cannot be greater than maximum area");
        }
    }
}