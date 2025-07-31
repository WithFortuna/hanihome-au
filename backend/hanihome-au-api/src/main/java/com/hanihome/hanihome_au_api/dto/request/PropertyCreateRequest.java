package com.hanihome.hanihome_au_api.dto.request;

import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
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
public class PropertyCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;

    @Size(max = 5000, message = "설명은 5000자를 초과할 수 없습니다")
    private String description;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 500, message = "주소는 500자를 초과할 수 없습니다")
    private String address;

    @Size(max = 100, message = "상세주소는 100자를 초과할 수 없습니다")
    private String detailAddress;

    @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다")
    private String zipCode;

    @Size(max = 50, message = "시/도는 50자를 초과할 수 없습니다")
    private String city;

    @Size(max = 50, message = "구/군은 50자를 초과할 수 없습니다")
    private String district;

    @NotNull(message = "매물 유형은 필수입니다")
    private PropertyType propertyType;

    @NotNull(message = "임대 유형은 필수입니다")
    private RentalType rentalType;

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

    @Future(message = "입주 가능일은 미래 날짜여야 합니다")
    private LocalDate availableDate;

    private Long agentId;

    private List<String> options;

    @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다")
    @DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다")
    @Digits(integer = 3, fraction = 7, message = "위도는 소수점 7자리까지 입력 가능합니다")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다")
    @DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다")
    @Digits(integer = 3, fraction = 7, message = "경도는 소수점 7자리까지 입력 가능합니다")
    private BigDecimal longitude;

    private Boolean parkingAvailable;
    private Boolean petAllowed;
    private Boolean furnished;
    private Boolean shortTermAvailable;
}