package com.hanihome.hanihome_au_api.infrastructure.persistence.property;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "properties")
public class PropertyJpaEntity {
    
    @Id
    private Long id;
    
    @Column(nullable = false)
    private Long ownerId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyTypeEnum propertyType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalTypeEnum rentalType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatusEnum status;
    
    // Address fields
    private String street;
    @Column(nullable = false)
    private String city;
    private String state;
    @Column(nullable = false)
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    
    // Property specs
    private int bedrooms;
    private int bathrooms;
    private Double floorArea;
    private Integer floor;
    private Integer totalFloors;
    private boolean hasParking = false;
    private boolean hasPet = false;
    private boolean hasElevator = false;
    
    // Pricing
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rentPrice;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal depositAmount;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal maintenanceFee;
    
    @Column(nullable = false)
    private String currency;
    
    private LocalDateTime availableFrom;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private Long agentId;
    
    @ElementCollection
    @CollectionTable(name = "property_options", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "option_name")
    private List<String> options = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "property_images", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();
    
    private Boolean furnished;
    private Boolean shortTermAvailable;
    
    @Column(columnDefinition = "TEXT")
    private String adminNotes;
    
    @Version
    private Long version;

    protected PropertyJpaEntity() {}

    public enum PropertyTypeEnum {
        APARTMENT, HOUSE, TOWNHOUSE, CONDO, STUDIO, ROOM
    }

    public enum RentalTypeEnum {
        LONG_TERM, SHORT_TERM, MONTHLY, WEEKLY, DAILY
    }

    public enum PropertyStatusEnum {
        DRAFT, PENDING_APPROVAL, ACTIVE, INACTIVE, RENTED, SUSPENDED, DELETED
    }

    // Constructor with all fields
    public PropertyJpaEntity(Long id, Long ownerId, String title, String description,
                           PropertyTypeEnum propertyType, RentalTypeEnum rentalType, PropertyStatusEnum status,
                           String street, String city, String state, String country, String postalCode,
                           Double latitude, Double longitude, int bedrooms, int bathrooms, Double floorArea,
                           Integer floor, Integer totalFloors, boolean hasParking, boolean hasPet,
                           boolean hasElevator, BigDecimal rentPrice, BigDecimal depositAmount, 
                           BigDecimal maintenanceFee, String currency, LocalDateTime availableFrom, 
                           LocalDateTime createdAt, LocalDateTime updatedAt, Long agentId, 
                           List<String> options, List<String> imageUrls, Boolean furnished, 
                           Boolean shortTermAvailable, String adminNotes, Long version) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.rentalType = rentalType;
        this.status = status;
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
        this.maintenanceFee = maintenanceFee;
        this.currency = currency;
        this.availableFrom = availableFrom;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.agentId = agentId;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.furnished = furnished;
        this.shortTermAvailable = shortTermAvailable;
        this.adminNotes = adminNotes;
        this.version = version;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public PropertyTypeEnum getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyTypeEnum propertyType) { this.propertyType = propertyType; }
    
    public RentalTypeEnum getRentalType() { return rentalType; }
    public void setRentalType(RentalTypeEnum rentalType) { this.rentalType = rentalType; }
    
    public PropertyStatusEnum getStatus() { return status; }
    public void setStatus(PropertyStatusEnum status) { this.status = status; }
    
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }
    
    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }
    
    public Double getFloorArea() { return floorArea; }
    public void setFloorArea(Double floorArea) { this.floorArea = floorArea; }
    
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    
    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
    
    public boolean isHasParking() { return hasParking; }
    public void setHasParking(boolean hasParking) { this.hasParking = hasParking; }
    
    public boolean isHasPet() { return hasPet; }
    public void setHasPet(boolean hasPet) { this.hasPet = hasPet; }
    
    public boolean isHasElevator() { return hasElevator; }
    public void setHasElevator(boolean hasElevator) { this.hasElevator = hasElevator; }
    
    public BigDecimal getRentPrice() { return rentPrice; }
    public void setRentPrice(BigDecimal rentPrice) { this.rentPrice = rentPrice; }
    
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDateTime getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDateTime availableFrom) { this.availableFrom = availableFrom; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public BigDecimal getMaintenanceFee() { return maintenanceFee; }
    public void setMaintenanceFee(BigDecimal maintenanceFee) { this.maintenanceFee = maintenanceFee; }
    
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    public Boolean getFurnished() { return furnished; }
    public void setFurnished(Boolean furnished) { this.furnished = furnished; }
    
    public Boolean getShortTermAvailable() { return shortTermAvailable; }
    public void setShortTermAvailable(Boolean shortTermAvailable) { this.shortTermAvailable = shortTermAvailable; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}