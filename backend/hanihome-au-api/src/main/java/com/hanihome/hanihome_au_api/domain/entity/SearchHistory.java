package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import com.hanihome.hanihome_au_api.domain.shared.entity.AggregateRoot;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "search_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "search_name", length = 100)
    private String searchName; // For saved searches

    @Column(name = "keyword")
    private String keyword;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "search_history_property_types", 
                    joinColumns = @JoinColumn(name = "search_history_id"))
    @Column(name = "property_type")
    private List<PropertyType> propertyTypes;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "search_history_rental_types", 
                    joinColumns = @JoinColumn(name = "search_history_id"))
    @Column(name = "rental_type")
    private List<RentalType> rentalTypes;

    @Column(name = "min_rent_price", precision = 19, scale = 2)
    private BigDecimal minRentPrice;

    @Column(name = "max_rent_price", precision = 19, scale = 2)
    private BigDecimal maxRentPrice;

    @Column(name = "min_deposit", precision = 19, scale = 2)
    private BigDecimal minDeposit;

    @Column(name = "max_deposit", precision = 19, scale = 2)
    private BigDecimal maxDeposit;

    @Column(name = "min_bedrooms")
    private Integer minBedrooms;

    @Column(name = "max_bedrooms")
    private Integer maxBedrooms;

    @Column(name = "min_bathrooms")
    private Integer minBathrooms;

    @Column(name = "max_bathrooms")
    private Integer maxBathrooms;

    @Column(name = "min_floor_area", precision = 19, scale = 2)
    private BigDecimal minFloorArea;

    @Column(name = "max_floor_area", precision = 19, scale = 2)
    private BigDecimal maxFloorArea;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "latitude", precision = 19, scale = 10)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 19, scale = 10)
    private BigDecimal longitude;

    @Column(name = "max_distance", precision = 19, scale = 2)
    private BigDecimal maxDistance;

    @Column(name = "parking_required")
    private Boolean parkingRequired;

    @Column(name = "pet_allowed_required")
    private Boolean petAllowedRequired;

    @Column(name = "furnished_required")
    private Boolean furnishedRequired;

    @Column(name = "short_term_available_required")
    private Boolean shortTermAvailableRequired;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "search_history_required_options", 
                    joinColumns = @JoinColumn(name = "search_history_id"))
    @Column(name = "required_option")
    private List<String> requiredOptions;

    @Column(name = "sort_by")
    private String sortBy;

    @Column(name = "sort_direction")
    private String sortDirection;

    @Column(name = "available_from")
    private String availableFrom;

    @Column(name = "available_to")
    private String availableTo;

    @Column(name = "recent_days")
    private Integer recentDays;

    @Column(name = "is_saved")
    @Builder.Default
    private Boolean isSaved = false;

    @Column(name = "search_count")
    @Builder.Default
    private Integer searchCount = 1;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (lastUsedAt == null) {
            lastUsedAt = LocalDateTime.now();
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    public void incrementSearchCount() {
        this.searchCount = (this.searchCount == null ? 0 : this.searchCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void saveSearch(String name) {
        this.searchName = name;
        this.isSaved = true;
    }

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}