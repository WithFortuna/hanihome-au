package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties", indexes = {
    @Index(name = "idx_property_type", columnList = "propertyType"),
    @Index(name = "idx_rental_type", columnList = "rentalType"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_monthly_rent", columnList = "monthlyRent"),
    @Index(name = "idx_deposit", columnList = "deposit"),
    @Index(name = "idx_address", columnList = "address"),
    @Index(name = "idx_available_date", columnList = "availableDate"),
    @Index(name = "idx_created_date", columnList = "createdDate"),
    @Index(name = "idx_landlord_id", columnList = "landlordId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 100)
    private String detailAddress;

    @Column(length = 10)
    private String zipCode;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalType rentalType;

    @Column(precision = 12, scale = 0)
    private BigDecimal deposit;

    @Column(precision = 10, scale = 0)
    private BigDecimal monthlyRent;

    @Column(precision = 12, scale = 0)
    private BigDecimal maintenanceFee;

    @Column(precision = 8, scale = 2)
    private BigDecimal area;

    @Column
    private Integer rooms;

    @Column
    private Integer bathrooms;

    @Column
    private Integer floor;

    @Column
    private Integer totalFloors;

    @Column
    private LocalDate availableDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.PENDING_APPROVAL;

    @Column(nullable = false)
    private Long landlordId;

    @Column
    private Long agentId;

    @ElementCollection
    @CollectionTable(name = "property_options", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "option_name")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "property_images", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "image_url", length = 500)
    @OrderColumn(name = "image_order")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column
    private Boolean parkingAvailable;

    @Column
    private Boolean petAllowed;

    @Column
    private Boolean furnished;

    @Column
    private Boolean shortTermAvailable;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private Long approvedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @Version
    private Long version;

    public void approve(Long approvedBy) {
        this.status = PropertyStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approvedBy;
    }

    public void reject() {
        this.status = PropertyStatus.REJECTED;
    }

    public void suspend() {
        this.status = PropertyStatus.SUSPENDED;
    }

    public void complete() {
        this.status = PropertyStatus.COMPLETED;
    }

    public void activate() {
        this.status = PropertyStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = PropertyStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == PropertyStatus.ACTIVE;
    }

    public boolean isPendingApproval() {
        return status == PropertyStatus.PENDING_APPROVAL;
    }

    public boolean isCompleted() {
        return status == PropertyStatus.COMPLETED;
    }
}