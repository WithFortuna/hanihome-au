package com.hanihome.hanihome_au_api.domain.property.valueobject;

import java.util.Objects;

public class PropertySpecs {
    private final int bedrooms;
    private final int bathrooms;
    private final Double floorArea;
    private final Integer floor;
    private final Integer totalFloors;
    private final boolean hasParking;
    private final boolean hasPet;
    private final boolean hasElevator;

    public PropertySpecs(int bedrooms, int bathrooms, Double floorArea, Integer floor, 
                        Integer totalFloors, boolean hasParking, boolean hasPet, boolean hasElevator) {
        if (bedrooms < 0) {
            throw new IllegalArgumentException("Bedrooms cannot be negative");
        }
        if (bathrooms < 0) {
            throw new IllegalArgumentException("Bathrooms cannot be negative");
        }
        if (floorArea != null && floorArea <= 0) {
            throw new IllegalArgumentException("Floor area must be positive");
        }
        if (floor != null && totalFloors != null && floor > totalFloors) {
            throw new IllegalArgumentException("Floor cannot be higher than total floors");
        }

        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.floorArea = floorArea;
        this.floor = floor;
        this.totalFloors = totalFloors;
        this.hasParking = hasParking;
        this.hasPet = hasPet;
        this.hasElevator = hasElevator;
    }

    public String getSpaceSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(bedrooms).append("침실 ");
        sb.append(bathrooms).append("욕실");
        if (floorArea != null) {
            sb.append(" ").append(floorArea).append("㎡");
        }
        return sb.toString();
    }

    public boolean isStudio() {
        return bedrooms == 0;
    }

    // Getters
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public Double getFloorArea() { return floorArea; }
    public Integer getFloor() { return floor; }
    public Integer getTotalFloors() { return totalFloors; }
    public boolean isHasParking() { return hasParking; }
    public boolean isHasPet() { return hasPet; }
    public boolean isHasElevator() { return hasElevator; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertySpecs that = (PropertySpecs) o;
        return bedrooms == that.bedrooms &&
               bathrooms == that.bathrooms &&
               hasParking == that.hasParking &&
               hasPet == that.hasPet &&
               hasElevator == that.hasElevator &&
               Objects.equals(floorArea, that.floorArea) &&
               Objects.equals(floor, that.floor) &&
               Objects.equals(totalFloors, that.totalFloors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bedrooms, bathrooms, floorArea, floor, totalFloors, hasParking, hasPet, hasElevator);
    }
}