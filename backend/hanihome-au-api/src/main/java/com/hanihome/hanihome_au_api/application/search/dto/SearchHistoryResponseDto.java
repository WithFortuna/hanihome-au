package com.hanihome.hanihome_au_api.application.search.dto;

import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search history response data")
public class SearchHistoryResponseDto {

    @Schema(description = "Search history ID")
    private Long id;

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Search name (for saved searches)")
    private String searchName;

    @Schema(description = "Search keyword")
    private String keyword;

    @Schema(description = "Property types")
    private List<PropertyType> propertyTypes;

    @Schema(description = "Rental types")
    private List<RentalType> rentalTypes;

    @Schema(description = "Minimum rent price")
    private BigDecimal minRentPrice;

    @Schema(description = "Maximum rent price")
    private BigDecimal maxRentPrice;

    @Schema(description = "Minimum deposit")
    private BigDecimal minDeposit;

    @Schema(description = "Maximum deposit")
    private BigDecimal maxDeposit;

    @Schema(description = "Minimum bedrooms")
    private Integer minBedrooms;

    @Schema(description = "Maximum bedrooms")
    private Integer maxBedrooms;

    @Schema(description = "Minimum bathrooms")
    private Integer minBathrooms;

    @Schema(description = "Maximum bathrooms")
    private Integer maxBathrooms;

    @Schema(description = "Minimum floor area")
    private BigDecimal minFloorArea;

    @Schema(description = "Maximum floor area")
    private BigDecimal maxFloorArea;

    @Schema(description = "City")
    private String city;

    @Schema(description = "State")
    private String state;

    @Schema(description = "Country")
    private String country;

    @Schema(description = "Postal code")
    private String postalCode;

    @Schema(description = "Latitude")
    private BigDecimal latitude;

    @Schema(description = "Longitude")
    private BigDecimal longitude;

    @Schema(description = "Maximum distance")
    private BigDecimal maxDistance;

    @Schema(description = "Parking required")
    private Boolean parkingRequired;

    @Schema(description = "Pet allowed required")
    private Boolean petAllowedRequired;

    @Schema(description = "Furnished required")
    private Boolean furnishedRequired;

    @Schema(description = "Short term available required")
    private Boolean shortTermAvailableRequired;

    @Schema(description = "Required options")
    private List<String> requiredOptions;

    @Schema(description = "Sort by field")
    private String sortBy;

    @Schema(description = "Sort direction")
    private String sortDirection;

    @Schema(description = "Available from date")
    private String availableFrom;

    @Schema(description = "Available to date")
    private String availableTo;

    @Schema(description = "Recent days filter")
    private Integer recentDays;

    @Schema(description = "Whether this search is saved")
    private Boolean isSaved;

    @Schema(description = "Number of times this search was used")
    private Integer searchCount;

    @Schema(description = "Last time this search was used")
    private LocalDateTime lastUsedAt;

    @Schema(description = "When this search was created")
    private LocalDateTime createdAt;

    @Schema(description = "When this search was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "Friendly display text for the search")
    private String displayText;

    @Schema(description = "Summary of search filters")
    private SearchSummary searchSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Summary of search filters for display")
    public static class SearchSummary {
        @Schema(description = "Location summary")
        private String location;

        @Schema(description = "Price range summary")
        private String priceRange;

        @Schema(description = "Property type summary")
        private String propertyTypes;

        @Schema(description = "Amenities summary")
        private String amenities;

        @Schema(description = "Total number of filters applied")
        private Integer totalFilters;
    }
}