package com.hanihome.hanihome_au_api.application.property.dto;

import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PropertyResponseDto {
    private final Long id;
    private final Long ownerId;
    private final String title;
    private final String description;
    private final String propertyType;
    private final String rentalType;
    private final String status;
    private final String fullAddress;
    private final Double latitude;
    private final Double longitude;
    private final int bedrooms;
    private final int bathrooms;
    private final Double floorArea;
    private final Integer floor;
    private final Integer totalFloors;
    private final boolean hasParking;
    private final boolean hasPet;
    private final boolean hasElevator;
    private final BigDecimal rentPrice;
    private final BigDecimal depositAmount;
    private final String currency;
    private final LocalDateTime availableFrom;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PropertyResponseDto(Long id, Long ownerId, String title, String description, String propertyType,
                             String rentalType, String status, String fullAddress, Double latitude, Double longitude,
                             int bedrooms, int bathrooms, Double floorArea, Integer floor, Integer totalFloors,
                             boolean hasParking, boolean hasPet, boolean hasElevator, BigDecimal rentPrice,
                             BigDecimal depositAmount, String currency, LocalDateTime availableFrom,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.rentalType = rentalType;
        this.status = status;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.floorArea = floorArea;
        this.floor = floor;
        this.totalFloors = totalFloors;
        this.hasParking = hasParking;
        this.hasPet = hasPet;
        this.hasElevator = hasElevator;
        this.rentPrice = rentPrice;
        this.depositAmount = depositAmount;
        this.currency = currency;
        this.availableFrom = availableFrom;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() { return id; }
    public Long getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPropertyType() { return propertyType; }
    public String getRentalType() { return rentalType; }
    public String getStatus() { return status; }
    public String getFullAddress() { return fullAddress; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public Double getFloorArea() { return floorArea; }
    public Integer getFloor() { return floor; }
    public Integer getTotalFloors() { return totalFloors; }
    public boolean isHasParking() { return hasParking; }
    public boolean isHasPet() { return hasPet; }
    public boolean isHasElevator() { return hasElevator; }
    public BigDecimal getRentPrice() { return rentPrice; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public String getCurrency() { return currency; }
    public LocalDateTime getAvailableFrom() { return availableFrom; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static PropertyResponseDto from(Property property) {
        return new PropertyResponseDto(
                property.getId() != null ? property.getId().getValue() : null,
                property.getOwnerId() != null ? property.getOwnerId().getValue() : null,
                property.getTitle(),
                property.getDescription(),
                property.getType() != null ? property.getType().name() : null,
                property.getRentalType() != null ? property.getRentalType().name() : null,
                property.getStatus() != null ? property.getStatus().name() : null,
                property.getAddress() != null ? property.getAddress().getFullAddress() : null,
                property.getAddress() != null ? property.getAddress().getLatitude() : null,
                property.getAddress() != null ? property.getAddress().getLongitude() : null,
                property.getSpecs() != null ? property.getSpecs().getBedrooms() : 0,
                property.getSpecs() != null ? property.getSpecs().getBathrooms() : 0,
                property.getSpecs() != null ? property.getSpecs().getFloorArea() : null,
                property.getSpecs() != null ? property.getSpecs().getFloor() : null,
                property.getSpecs() != null ? property.getSpecs().getTotalFloors() : null,
                property.getParkingAvailable() != null ? property.getParkingAvailable() : false,
                property.getPetAllowed() != null ? property.getPetAllowed() : false,
                false, // hasElevator - not available in current Property model
                property.getRentPrice() != null ? property.getRentPrice().getAmount() : null,
                property.getDepositAmount() != null ? property.getDepositAmount().getAmount() : null,
                property.getRentPrice() != null ? property.getRentPrice().getCurrency() : "AUD",
                property.getAvailableFrom(),
                property.getCreatedAt(),
                property.getUpdatedAt()
        );
    }
}