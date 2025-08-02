package com.hanihome.hanihome_au_api.domain.shared.valueobject;

import java.util.Objects;

public class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final String postalCode;
    private final Double latitude;
    private final Double longitude;

    public Address(String street, String city, String state, String country, String postalCode, 
                   Double latitude, Double longitude) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
        
        this.street = street;
        this.city = city.trim();
        this.state = state;
        this.country = country.trim();
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.trim().isEmpty()) {
            sb.append(street).append(", ");
        }
        sb.append(city);
        if (state != null && !state.trim().isEmpty()) {
            sb.append(", ").append(state);
        }
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            sb.append(" ").append(postalCode);
        }
        sb.append(", ").append(country);
        return sb.toString();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(country, address.country) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(latitude, address.latitude) &&
               Objects.equals(longitude, address.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, country, postalCode, latitude, longitude);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}