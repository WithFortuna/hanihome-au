package com.hanihome.hanihome_au_api.dto.request;

import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchCriteria {
    
    private PropertyStatus status;
    private PropertyType propertyType;
    private RentalType rentalType;
    
    // Price range
    private BigDecimal minDeposit;
    private BigDecimal maxDeposit;
    private BigDecimal minMonthlyRent;
    private BigDecimal maxMonthlyRent;
    private BigDecimal maxMaintenanceFee;
    
    // Area and rooms
    private BigDecimal minArea;
    private BigDecimal maxArea;
    private Integer minRooms;
    private Integer maxRooms;
    private Integer minBathrooms;
    private Integer maxBathrooms;
    
    // Location
    private String city;
    private String district;
    private String addressKeyword;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Double radiusKm;
    
    // Floor preferences
    private Integer minFloor;
    private Integer maxFloor;
    private Boolean excludeBasement;
    private Boolean excludeRooftop;
    
    // Availability
    private LocalDate availableFrom;
    private LocalDate availableTo;
    private Boolean availableNow;
    
    // Features
    private Boolean parkingRequired;
    private Boolean petAllowed;
    private Boolean furnished;
    private Boolean shortTermAvailable;
    private List<String> requiredOptions;
    
    // Landlord/Agent filters
    private Long landlordId;
    private Long agentId;
    
    // Date filters
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    
    // Sorting
    private String sortBy; // price, area, date, distance
    private String sortDirection; // asc, desc
    
    // Text search
    private String keyword; // searches in title and description
    
    // Price per area ratio
    private BigDecimal maxPricePerSquareMeter;
    
    public boolean hasLocationFilter() {
        return latitude != null && longitude != null && radiusKm != null;
    }
    
    public boolean hasPriceFilter() {
        return minDeposit != null || maxDeposit != null || 
               minMonthlyRent != null || maxMonthlyRent != null;
    }
    
    public boolean hasAreaFilter() {
        return minArea != null || maxArea != null;
    }
    
    public boolean hasRoomFilter() {
        return minRooms != null || maxRooms != null || 
               minBathrooms != null || maxBathrooms != null;
    }
    
    public boolean hasDateFilter() {
        return availableFrom != null || availableTo != null || 
               createdAfter != null || createdBefore != null;
    }
    
    public boolean hasFloorFilter() {
        return minFloor != null || maxFloor != null || 
               Boolean.TRUE.equals(excludeBasement) || Boolean.TRUE.equals(excludeRooftop);
    }
}