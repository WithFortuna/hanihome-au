package com.hanihome.hanihome_au_api.application.property.dto;

import com.hanihome.hanihome_au_api.domain.entity.PropertyFavorite;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyFavoriteResponseDto {
    private Long id;
    private Long userId;
    private Long propertyId;
    private String category;
    private String notes;
    private LocalDateTime createdAt;
    private Boolean notificationEnabled;
    private PropertyResponseDto property;

    public static PropertyFavoriteResponseDto from(PropertyFavorite favorite) {
        return new PropertyFavoriteResponseDto(
            favorite.getId(),
            favorite.getUserId(),
            favorite.getPropertyId(),
            favorite.getCategory(),
            favorite.getNotes(),
            favorite.getCreatedAt(),
            favorite.getNotificationEnabled(),
            null // Property will be populated separately if needed
        );
    }

    public static PropertyFavoriteResponseDto from(PropertyFavorite favorite, PropertyResponseDto property) {
        PropertyFavoriteResponseDto dto = from(favorite);
        dto.setProperty(property);
        return dto;
    }
}