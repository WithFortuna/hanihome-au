package com.hanihome.hanihome_au_api.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "즐겨찾기 수정 요청")
public class UpdateFavoriteRequest {

    @Schema(description = "카테고리", example = "관심매물")
    private String category;

    @Schema(description = "메모", example = "교통이 편리한 위치")
    private String notes;

    @Schema(description = "알림 활성화 여부", example = "true")
    private Boolean notificationEnabled;
}