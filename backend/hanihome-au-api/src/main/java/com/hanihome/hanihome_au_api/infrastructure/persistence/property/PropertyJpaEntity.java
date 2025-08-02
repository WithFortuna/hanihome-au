package com.hanihome.hanihome_au_api.infrastructure.persistence.property;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "properties")
public class PropertyJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 500)
    private String address;
    
    @Column(name = "detail_address", length = 100)
    private String detailAddress;
    
    @Column(name = "zip_code", length = 10)
    private String zipCode;
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 50)
    private String district;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 50)
    private PropertyTypeEnum propertyType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type", nullable = false, length = 50)
    private RentalTypeEnum rentalType;
    
    @Column(precision = 12, scale = 0)
    private BigDecimal deposit;
    
    @Column(name = "monthly_rent", precision = 10, scale = 0)
    private BigDecimal monthlyRent;
    
    @Column(name = "maintenance_fee", precision = 12, scale = 0)
    private BigDecimal maintenanceFee;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal area;
    
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    
    @Column(name = "total_floors")
    private Integer totalFloors;
    
    @Column(name = "available_date")
    private LocalDate availableDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PropertyStatusEnum status;
    
    @Column(name = "landlord_id", nullable = false)
    private Long landlordId;
    
    @Column(name = "agent_id")
    private Long agentId;
    
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;
    
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;
    
    @Column(name = "parking_available")
    private Boolean parkingAvailable;
    
    @Column(name = "pet_allowed")
    private Boolean petAllowed;
    
    private Boolean furnished;
    
    @Column(name = "short_term_available")
    private Boolean shortTermAvailable;
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;
    
    @Version
    private Long version;

    protected PropertyJpaEntity() {}

    public enum PropertyTypeEnum {
        APARTMENT, VILLA, STUDIO, TWO_ROOM, THREE_ROOM, OFFICETEL, HOUSE
    }

    public enum RentalTypeEnum {
        MONTHLY, JEONSE, SALE
    }

    public enum PropertyStatusEnum {
        ACTIVE, INACTIVE, PENDING_APPROVAL, REJECTED, COMPLETED, SUSPENDED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public PropertyTypeEnum getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyTypeEnum propertyType) { this.propertyType = propertyType; }
    
    public RentalTypeEnum getRentalType() { return rentalType; }
    public void setRentalType(RentalTypeEnum rentalType) { this.rentalType = rentalType; }
    
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }
    
    public BigDecimal getMaintenanceFee() { return maintenanceFee; }
    public void setMaintenanceFee(BigDecimal maintenanceFee) { this.maintenanceFee = maintenanceFee; }
    
    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }
    
    public Integer getRooms() { return rooms; }
    public void setRooms(Integer rooms) { this.rooms = rooms; }
    
    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }
    
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    
    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
    
    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }
    
    public PropertyStatusEnum getStatus() { return status; }
    public void setStatus(PropertyStatusEnum status) { this.status = status; }
    
    public Long getLandlordId() { return landlordId; }
    public void setLandlordId(Long landlordId) { this.landlordId = landlordId; }
    
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    
    public Boolean getParkingAvailable() { return parkingAvailable; }
    public void setParkingAvailable(Boolean parkingAvailable) { this.parkingAvailable = parkingAvailable; }
    
    public Boolean getPetAllowed() { return petAllowed; }
    public void setPetAllowed(Boolean petAllowed) { this.petAllowed = petAllowed; }
    
    public Boolean getFurnished() { return furnished; }
    public void setFurnished(Boolean furnished) { this.furnished = furnished; }
    
    public Boolean getShortTermAvailable() { return shortTermAvailable; }
    public void setShortTermAvailable(Boolean shortTermAvailable) { this.shortTermAvailable = shortTermAvailable; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}