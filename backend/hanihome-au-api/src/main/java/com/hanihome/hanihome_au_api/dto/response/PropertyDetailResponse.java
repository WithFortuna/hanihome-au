package com.hanihome.hanihome_au_api.dto.response;

import com.hanihome.hanihome_au_api.domain.entity.PropertyImage;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String address;
    private String detailAddress;
    private String zipCode;
    private String city;
    private String district;
    private PropertyType propertyType;
    private RentalType rentalType;
    private BigDecimal deposit;
    private BigDecimal monthlyRent;
    private BigDecimal maintenanceFee;
    private BigDecimal area;
    private Integer rooms;
    private Integer bathrooms;
    private Integer floor;
    private Integer totalFloors;
    private LocalDate availableDate;
    private PropertyStatus status;
    private Long landlordId;
    private Long agentId;
    private List<String> options;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean parkingAvailable;
    private Boolean petAllowed;
    private Boolean furnished;
    private Boolean shortTermAvailable;
    private List<PropertyImage> images;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private LocalDateTime approvedAt;
    private Long approvedBy;
}