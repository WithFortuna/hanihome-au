package com.hanihome.hanihome_au_api.application.property.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFavoriteCommand {
    private Long favoriteId;
    private Long userId;
    private String category;
    private String notes;
    private Boolean notificationEnabled;
}