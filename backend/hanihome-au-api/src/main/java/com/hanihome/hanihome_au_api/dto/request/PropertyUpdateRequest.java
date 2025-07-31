package com.hanihome.hanihome_au_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyUpdateRequest {

    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;

    @Size(max = 5000, message = "설명은 5000자를 초과할 수 없습니다")
    private String description;

    @Size(max = 500, message = "주소는 500자를 초과할 수 없습니다")
    private String address;

    @Size(max = 100, message = "상세주소는 100자를 초과할 수 없습니다")
    private String detailAddress;

    @DecimalMin(value = "0", message = "보증금은 0 이상이어야 합니다")
    @Digits(integer = 12, fraction = 0, message = "보증금은 12자리를 초과할 수 없습니다")
    private BigDecimal deposit;

    @DecimalMin(value = "0", message = "월세는 0 이상이어야 합니다")
    @Digits(integer = 10, fraction = 0, message = "월세는 10자리를 초과할 수 없습니다")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0", message = "관리비는 0 이상이어야 합니다")
    @Digits(integer = 12, fraction = 0, message = "관리비는 12자리를 초과할 수 없습니다")
    private BigDecimal maintenanceFee;

    @DecimalMin(value = "0.1", message = "면적은 0.1㎡ 이상이어야 합니다")
    @Digits(integer = 6, fraction = 2, message = "면적은 소수점 2자리까지 입력 가능합니다")
    private BigDecimal area;

    @Min(value = 0, message = "방 개수는 0 이상이어야 합니다")
    @Max(value = 50, message = "방 개수는 50을 초과할 수 없습니다")
    private Integer rooms;

    @Min(value = 0, message = "화장실 개수는 0 이상이어야 합니다")
    @Max(value = 20, message = "화장실 개수는 20을 초과할 수 없습니다")
    private Integer bathrooms;

    @Min(value = -5, message = "층수는 -5층 이상이어야 합니다")
    @Max(value = 200, message = "층수는 200층을 초과할 수 없습니다")
    private Integer floor;

    @Min(value = 1, message = "총 층수는 1층 이상이어야 합니다")
    @Max(value = 200, message = "총 층수는 200층을 초과할 수 없습니다")
    private Integer totalFloors;

    private LocalDate availableDate;

    private List<String> options;

    private Boolean parkingAvailable;
    private Boolean petAllowed;
    private Boolean furnished;
    private Boolean shortTermAvailable;
}