package com.hanihome.hanihome_au_api.dto.response;

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
public class PropertyListResponse {

    private List<PropertyListItem> properties;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyListItem {
        private Long id;
        private String title;
        private String address;
        private String city;
        private String district;
        private PropertyType propertyType;
        private RentalType rentalType;
        private BigDecimal deposit;
        private BigDecimal monthlyRent;
        private BigDecimal area;
        private Integer rooms;
        private Integer bathrooms;
        private Integer floor;
        private LocalDate availableDate;
        private PropertyStatus status;
        private Boolean parkingAvailable;
        private Boolean petAllowed;
        private Boolean furnished;
        private String mainImageUrl;
        private String thumbnailUrl;
        private LocalDateTime createdDate;
    }
}