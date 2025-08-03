package com.hanihome.hanihome_au_api.presentation.dto;

import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Property search request with multiple filter criteria")
public class PropertySearchRequest {
    
    @Schema(description = "Search keyword for title or description")
    private String keyword;
    
    @Schema(description = "Property types to include in search")
    private List<PropertyType> propertyTypes;
    
    @Schema(description = "Rental types to include in search")
    private List<RentalType> rentalTypes;
    
    @Schema(description = "Minimum rent price")
    @DecimalMin(value = "0.0", message = "Minimum rent price cannot be negative")
    private BigDecimal minRentPrice;
    
    @Schema(description = "Maximum rent price")
    @DecimalMin(value = "0.0", message = "Maximum rent price cannot be negative")
    private BigDecimal maxRentPrice;
    
    @Schema(description = "Minimum deposit amount")
    @DecimalMin(value = "0.0", message = "Minimum deposit cannot be negative")
    private BigDecimal minDeposit;
    
    @Schema(description = "Maximum deposit amount")
    @DecimalMin(value = "0.0", message = "Maximum deposit cannot be negative")
    private BigDecimal maxDeposit;
    
    @Schema(description = "Minimum number of bedrooms")
    @Min(value = 0, message = "Minimum bedrooms cannot be negative")
    @Max(value = 20, message = "Maximum bedrooms cannot exceed 20")
    private Integer minBedrooms;
    
    @Schema(description = "Maximum number of bedrooms")
    @Min(value = 0, message = "Maximum bedrooms cannot be negative")
    @Max(value = 20, message = "Maximum bedrooms cannot exceed 20")
    private Integer maxBedrooms;
    
    @Schema(description = "Minimum number of bathrooms")
    @Min(value = 0, message = "Minimum bathrooms cannot be negative")
    @Max(value = 20, message = "Maximum bathrooms cannot exceed 20")
    private Integer minBathrooms;
    
    @Schema(description = "Maximum number of bathrooms")
    @Min(value = 0, message = "Maximum bathrooms cannot be negative")
    @Max(value = 20, message = "Maximum bathrooms cannot exceed 20")
    private Integer maxBathrooms;
    
    @Schema(description = "Minimum floor area in square meters")
    @DecimalMin(value = "0.0", message = "Minimum floor area cannot be negative")
    private BigDecimal minFloorArea;
    
    @Schema(description = "Maximum floor area in square meters")
    @DecimalMin(value = "0.0", message = "Maximum floor area cannot be negative")
    private BigDecimal maxFloorArea;
    
    // Location filters
    @Schema(description = "City name")
    private String city;
    
    @Schema(description = "State/Province")
    private String state;
    
    @Schema(description = "Country")
    private String country;
    
    @Schema(description = "Postal code")
    private String postalCode;
    
    @Schema(description = "Center latitude for distance-based search")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;
    
    @Schema(description = "Center longitude for distance-based search")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;
    
    @Schema(description = "Maximum distance from center point in kilometers")
    @DecimalMin(value = "0.1", message = "Distance must be at least 0.1 km")
    @DecimalMax(value = "1000.0", message = "Distance cannot exceed 1000 km")
    private BigDecimal maxDistance;
    
    // Amenity filters
    @Schema(description = "Parking required")
    private Boolean parkingRequired;
    
    @Schema(description = "Pet allowed required")
    private Boolean petAllowedRequired;
    
    @Schema(description = "Furnished required")
    private Boolean furnishedRequired;
    
    @Schema(description = "Short term rental available required")
    private Boolean shortTermAvailableRequired;
    
    @Schema(description = "Additional options/amenities")
    private List<String> requiredOptions;
    
    // Sorting and pagination
    @Schema(description = "Sort by field", allowableValues = {"createdAt", "rentPrice", "distance", "popularity"})
    private String sortBy = "createdAt";
    
    @Schema(description = "Sort direction", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";
    
    @Schema(description = "Page number (0-based)")
    @Min(value = 0, message = "Page number cannot be negative")
    private Integer page = 0;
    
    @Schema(description = "Page size")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 20;
    
    // Additional filters
    @Schema(description = "Available from date filter (ISO format)")
    private String availableFrom;
    
    @Schema(description = "Available to date filter (ISO format)")
    private String availableTo;
    
    @Schema(description = "Include only recently added properties (days)")
    @Min(value = 1, message = "Recent days must be at least 1")
    @Max(value = 365, message = "Recent days cannot exceed 365")
    private Integer recentDays;
    
    @Schema(description = "Cursor for pagination (optional, for infinite scroll)")
    private PropertySearchCursor cursor;
}