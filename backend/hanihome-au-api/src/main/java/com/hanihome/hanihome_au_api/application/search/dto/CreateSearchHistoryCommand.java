package com.hanihome.hanihome_au_api.application.search.dto;

import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Command to create search history entry")
public class CreateSearchHistoryCommand {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user performing the search")
    private Long userId;

    @Schema(description = "Search keyword")
    private String keyword;

    @Schema(description = "Property types in search")
    private List<PropertyType> propertyTypes;

    @Schema(description = "Rental types in search")
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
}