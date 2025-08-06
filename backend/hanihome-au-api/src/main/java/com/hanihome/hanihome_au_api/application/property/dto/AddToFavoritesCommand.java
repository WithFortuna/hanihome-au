package com.hanihome.hanihome_au_api.application.property.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToFavoritesCommand {
    private Long userId;
    private Long propertyId;
    private String category;
    private String notes;
    private Boolean notificationEnabled = true;
}