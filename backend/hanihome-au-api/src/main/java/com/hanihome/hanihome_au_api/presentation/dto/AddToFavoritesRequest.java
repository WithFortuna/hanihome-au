package com.hanihome.hanihome_au_api.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "즐겨찾기 추가 요청")
public class AddToFavoritesRequest {

    @NotNull(message = "매물 ID는 필수입니다")
    @Schema(description = "매물 ID", example = "1", required = true)
    private Long propertyId;

    @Schema(description = "카테고리", example = "관심매물")
    private String category;

    @Schema(description = "메모", example = "교통이 편리한 위치")
    private String notes;

    @Schema(description = "알림 활성화 여부", example = "true")
    private Boolean notificationEnabled = true;
}