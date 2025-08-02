package com.hanihome.hanihome_au_api.application.property.dto;

import java.math.BigDecimal;

public class CreatePropertyCommand {
    private final Long ownerId;
    private final String title;
    private final String description;
    private final String propertyType;
    private final String rentalType;
    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final String postalCode;
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

    public CreatePropertyCommand(Long ownerId, String title, String description, String propertyType,
                               String rentalType, String street, String city, String state, String country,
                               String postalCode, Double latitude, Double longitude, int bedrooms,
                               int bathrooms, Double floorArea, Integer floor, Integer totalFloors,
                               boolean hasParking, boolean hasPet, boolean hasElevator,
                               BigDecimal rentPrice, BigDecimal depositAmount, String currency) {
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.rentalType = rentalType;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
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
    }

    // Getters
    public Long getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPropertyType() { return propertyType; }
    public String getRentalType() { return rentalType; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
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
}