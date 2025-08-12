package com.hanihome.hanihome_au_api.application.property.service;

import com.hanihome.hanihome_au_api.application.property.dto.*;
import com.hanihome.hanihome_au_api.domain.entity.PropertyFavorite;
import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.exception.GlobalExceptionHandler;
import com.hanihome.hanihome_au_api.repository.PropertyFavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PropertyFavoriteService {

    private final PropertyFavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    @CacheEvict(value = {"userFavorites", "favoriteStats"}, key = "#command.userId")
    public PropertyFavoriteResponseDto addToFavorites(AddToFavoritesCommand command) {
        // Check if already exists
        if (favoriteRepository.existsByUserIdAndPropertyId(command.getUserId(), command.getPropertyId())) {
            throw new IllegalArgumentException("Property is already in favorites");
        }

        // Verify property exists
        Property property = propertyRepository.findById(PropertyId.of(command.getPropertyId()))
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        PropertyFavorite favorite = new PropertyFavorite(
                command.getUserId(),
                command.getPropertyId(),
                command.getCategory(),
                command.getNotes()
        );

        if (command.getNotificationEnabled() != null) {
            if (command.getNotificationEnabled()) {
                favorite.enableNotification();
            } else {
                favorite.disableNotification();
            }
        }

        PropertyFavorite saved = favoriteRepository.save(favorite);
        log.info("Added property {} to favorites for user {}", command.getPropertyId(), command.getUserId());

        return PropertyFavoriteResponseDto.from(saved);
    }

    @Transactional
    @CacheEvict(value = {"userFavorites", "favoriteStats"}, key = "#userId")
    public void removeFromFavorites(Long userId, Long propertyId) {
        PropertyFavorite favorite = favoriteRepository.findByUserIdAndPropertyId(userId, propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found"));

        favoriteRepository.delete(favorite);
        log.info("Removed property {} from favorites for user {}", propertyId, userId);
    }

    @Transactional
    @CacheEvict(value = {"userFavorites", "favoriteStats"}, key = "#command.userId")
    public PropertyFavoriteResponseDto updateFavorite(UpdateFavoriteCommand command) {
        PropertyFavorite favorite = favoriteRepository.findById(command.getFavoriteId())
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found"));

        if (!favorite.getUserId().equals(command.getUserId())) {
            throw new IllegalArgumentException("Unauthorized to update this favorite");
        }

        if (command.getCategory() != null) {
            favorite.updateCategory(command.getCategory());
        }

        if (command.getNotes() != null) {
            favorite.updateNotes(command.getNotes());
        }

        if (command.getNotificationEnabled() != null) {
            if (command.getNotificationEnabled()) {
                favorite.enableNotification();
            } else {
                favorite.disableNotification();
            }
        }

        PropertyFavorite updated = favoriteRepository.save(favorite);
        log.info("Updated favorite {} for user {}", command.getFavoriteId(), command.getUserId());

        return PropertyFavoriteResponseDto.from(updated);
    }

    @Cacheable(value = "userFavorites", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PropertyFavoriteResponseDto> getUserFavorites(Long userId, Pageable pageable) {
        Page<PropertyFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // Get property IDs and fetch properties in batch
        Set<Long> propertyIds = favorites.getContent().stream()
                .map(PropertyFavorite::getPropertyId)
                .collect(Collectors.toSet());

        Map<Long, Property> propertyMap = propertyRepository.findAllById(propertyIds).stream()
                .collect(Collectors.toMap(property -> property.getId().getValue(), property -> property));

        return favorites.map(favorite -> {
            Property property = propertyMap.get(favorite.getPropertyId());
            PropertyResponseDto propertyDto = property != null ? PropertyResponseDto.from(property) : null;
            return PropertyFavoriteResponseDto.from(favorite, propertyDto);
        });
    }

    @Cacheable(value = "userFavorites", key = "#userId + '_category_' + #category + '_' + #pageable.pageNumber")
    public Page<PropertyFavoriteResponseDto> getUserFavoritesByCategory(Long userId, String category, Pageable pageable) {
        Page<PropertyFavorite> favorites = favoriteRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageable);
        
        Set<Long> propertyIds = favorites.getContent().stream()
                .map(PropertyFavorite::getPropertyId)
                .collect(Collectors.toSet());

        Map<Long, Property> propertyMap = propertyRepository.findAllById(propertyIds).stream()
                .collect(Collectors.toMap(property -> property.getId().getValue(), property -> property));

        return favorites.map(favorite -> {
            Property property = propertyMap.get(favorite.getPropertyId());
            PropertyResponseDto propertyDto = property != null ? PropertyResponseDto.from(property) : null;
            return PropertyFavoriteResponseDto.from(favorite, propertyDto);
        });
    }

    @Cacheable(value = "favoriteStats", key = "#userId + '_categories'")
    public List<String> getUserFavoriteCategories(Long userId) {
        return favoriteRepository.findDistinctCategoriesByUserId(userId);
    }

    @Cacheable(value = "favoriteStats", key = "#userId + '_count'")
    public long getUserFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    public boolean isFavorite(Long userId, Long propertyId) {
        return favoriteRepository.existsByUserIdAndPropertyId(userId, propertyId);
    }

    public List<Long> getUserFavoritePropertyIds(Long userId) {
        return favoriteRepository.findPropertyIdsByUserId(userId);
    }

    public Map<String, Long> getCategoryStats(Long userId) {
        List<String> categories = getUserFavoriteCategories(userId);
        return categories.stream()
                .collect(Collectors.toMap(
                        category -> category,
                        category -> favoriteRepository.countByUserIdAndCategory(userId, category)
                ));
    }

    public List<PropertyFavoriteResponseDto> getNotificationEnabledFavorites(Long userId) {
        List<PropertyFavorite> favorites = favoriteRepository.findByUserIdAndNotificationEnabledTrue(userId);
        
        Set<Long> propertyIds = favorites.stream()
                .map(PropertyFavorite::getPropertyId)
                .collect(Collectors.toSet());

        Map<Long, Property> propertyMap = propertyRepository.findAllById(propertyIds).stream()
                .collect(Collectors.toMap(property -> property.getId().getValue(), property -> property));

        return favorites.stream()
                .map(favorite -> {
                    Property property = propertyMap.get(favorite.getPropertyId());
                    PropertyResponseDto propertyDto = property != null ? PropertyResponseDto.from(property) : null;
                    return PropertyFavoriteResponseDto.from(favorite, propertyDto);
                })
                .collect(Collectors.toList());
    }
}