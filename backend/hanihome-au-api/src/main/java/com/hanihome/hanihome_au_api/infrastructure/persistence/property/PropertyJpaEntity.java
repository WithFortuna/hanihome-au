package com.hanihome.hanihome_au_api.infrastructure.persistence.property;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
@Getter @Setter
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

    @Column(name = "rent_price", precision = 10, scale = 0)
    private BigDecimal rentPrice;

    @Column(precision = 8, scale = 2)
    private BigDecimal area;

    private Double floorArea;
    
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer bedrooms;
    
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

}