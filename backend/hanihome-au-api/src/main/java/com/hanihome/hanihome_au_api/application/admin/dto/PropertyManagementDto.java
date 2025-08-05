package com.hanihome.hanihome_au_api.application.admin.dto;

import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PropertyManagementDto {
    private Long propertyId;
    private String title;
    private String address;
    private PropertyType propertyType;
    private RentalType rentalType;
    private PropertyStatus status;
    private BigDecimal rent;
    private BigDecimal bond;
    private String landlordName;
    private String landlordEmail;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private Long totalViews;
    private Long totalViewings;
    private Long totalFavorites;
    private Boolean isApproved;
    private String adminNotes;
    private String rejectionReason;
    private LocalDateTime approvalDate;
    private String approvedBy;
}