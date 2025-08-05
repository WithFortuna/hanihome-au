package com.hanihome.hanihome_au_api.application.search.service;

import com.hanihome.hanihome_au_api.application.search.dto.CreateSearchHistoryCommand;
import com.hanihome.hanihome_au_api.application.search.dto.SaveSearchCommand;
import com.hanihome.hanihome_au_api.application.search.dto.SearchHistoryResponseDto;
import com.hanihome.hanihome_au_api.domain.entity.SearchHistory;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import com.hanihome.hanihome_au_api.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    @Value("${app.search-history.retention-days:90}")
    private int retentionDays;

    @Value("${app.search-history.max-history-per-user:100}")
    private int maxHistoryPerUser;

    /**
     * Record a new search or update existing similar search
     */
    public SearchHistoryResponseDto recordSearch(CreateSearchHistoryCommand command) {
        log.info("Recording search for user: {}", command.getUserId());

        // Check for similar existing search
        Optional<SearchHistory> existingSearch = findSimilarSearch(command);

        SearchHistory searchHistory;
        if (existingSearch.isPresent()) {
            // Update existing search
            searchHistory = existingSearch.get();
            searchHistory.incrementSearchCount();
            log.debug("Updated existing search count: {}", searchHistory.getSearchCount());
        } else {
            // Create new search history
            searchHistory = createNewSearchHistory(command);
            log.debug("Created new search history: {}", searchHistory.getId());
        }

        searchHistory = searchHistoryRepository.save(searchHistory);

        // Clean up old history if necessary
        cleanupOldHistory(command.getUserId());

        return convertToDto(searchHistory);
    }

    /**
     * Save a search with a custom name
     */
    public SearchHistoryResponseDto saveSearch(SaveSearchCommand command) {
        log.info("Saving search with name '{}' for user: {}", command.getSearchName(), command.getUserId());

        // Check if name already exists
        if (searchHistoryRepository.existsByUserIdAndSearchNameAndIsSavedTrue(
                command.getUserId(), command.getSearchName())) {
            throw new IllegalArgumentException("Search name already exists: " + command.getSearchName());
        }

        SearchHistory searchHistory = searchHistoryRepository.findById(command.getSearchHistoryId())
                .orElseThrow(() -> new IllegalArgumentException("Search history not found: " + command.getSearchHistoryId()));

        if (!searchHistory.getUserId().equals(command.getUserId())) {
            throw new IllegalArgumentException("Search history does not belong to user");
        }

        searchHistory.saveSearch(command.getSearchName());
        searchHistory = searchHistoryRepository.save(searchHistory);

        return convertToDto(searchHistory);
    }

    /**
     * Get search history for user with pagination
     */
    @Transactional(readOnly = true)
    public Page<SearchHistoryResponseDto> getSearchHistory(Long userId, boolean savedOnly, Pageable pageable) {
        log.debug("Getting search history for user: {}, savedOnly: {}", userId, savedOnly);

        Page<SearchHistory> searchHistoryPage;
        if (savedOnly) {
            searchHistoryPage = searchHistoryRepository.findByUserIdAndIsSavedTrueOrderByLastUsedAtDesc(userId, pageable);
        } else {
            searchHistoryPage = searchHistoryRepository.findByUserIdOrderByLastUsedAtDesc(userId, pageable);
        }

        return searchHistoryPage.map(this::convertToDto);
    }

    /**
     * Get frequent searches for user
     */
    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDto> getFrequentSearches(Long userId, int limit) {
        log.debug("Getting frequent searches for user: {}, limit: {}", userId, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<SearchHistory> frequentSearches = searchHistoryRepository.findMostFrequentSearches(userId, pageable);

        return frequentSearches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete search history entry
     */
    public void deleteSearchHistory(Long userId, Long searchHistoryId) {
        log.info("Deleting search history: {} for user: {}", searchHistoryId, userId);

        SearchHistory searchHistory = searchHistoryRepository.findById(searchHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("Search history not found: " + searchHistoryId));

        if (!searchHistory.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Search history does not belong to user");
        }

        searchHistoryRepository.delete(searchHistory);
    }

    /**
     * Clear all non-saved search history for user
     */
    public int clearAllHistory(Long userId) {
        log.info("Clearing all non-saved search history for user: {}", userId);
        return searchHistoryRepository.deleteAllNonSavedSearchHistory(userId);
    }

    /**
     * Get search statistics for user
     */
    @Transactional(readOnly = true)
    public SearchHistoryStats getSearchStats(Long userId) {
        long totalSearches = searchHistoryRepository.countByUserId(userId);
        long savedSearches = searchHistoryRepository.countByUserIdAndIsSavedTrue(userId);

        return SearchHistoryStats.builder()
                .totalSearches(totalSearches)
                .savedSearches(savedSearches)
                .recentSearches(totalSearches - savedSearches)
                .build();
    }

    /**
     * Convert PropertySearchRequest to CreateSearchHistoryCommand
     */
    public CreateSearchHistoryCommand fromSearchRequest(com.hanihome.hanihome_au_api.presentation.dto.PropertySearchRequest request, Long userId) {
        return CreateSearchHistoryCommand.builder()
                .userId(userId)
                .keyword(request.getKeyword())
                .propertyTypes(request.getPropertyTypes())
                .rentalTypes(request.getRentalTypes())
                .minRentPrice(request.getMinRentPrice())
                .maxRentPrice(request.getMaxRentPrice())
                .minDeposit(request.getMinDeposit())
                .maxDeposit(request.getMaxDeposit())
                .minBedrooms(request.getMinBedrooms())
                .maxBedrooms(request.getMaxBedrooms())
                .minBathrooms(request.getMinBathrooms())
                .maxBathrooms(request.getMaxBathrooms())
                .minFloorArea(request.getMinFloorArea())
                .maxFloorArea(request.getMaxFloorArea())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxDistance(request.getMaxDistance())
                .parkingRequired(request.getParkingRequired())
                .petAllowedRequired(request.getPetAllowedRequired())
                .furnishedRequired(request.getFurnishedRequired())
                .shortTermAvailableRequired(request.getShortTermAvailableRequired())
                .requiredOptions(request.getRequiredOptions())
                .sortBy(request.getSortBy())
                .sortDirection(request.getSortDirection())
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .recentDays(request.getRecentDays())
                .build();
    }

    private Optional<SearchHistory> findSimilarSearch(CreateSearchHistoryCommand command) {
        return searchHistoryRepository.findSimilarSearch(
                command.getUserId(),
                command.getKeyword(),
                command.getCity(),
                command.getMinRentPrice(),
                command.getMaxRentPrice()
        );
    }

    private SearchHistory createNewSearchHistory(CreateSearchHistoryCommand command) {
        return SearchHistory.builder()
                .userId(command.getUserId())
                .keyword(command.getKeyword())
                .propertyTypes(command.getPropertyTypes())
                .rentalTypes(command.getRentalTypes())
                .minRentPrice(command.getMinRentPrice())
                .maxRentPrice(command.getMaxRentPrice())
                .minDeposit(command.getMinDeposit())
                .maxDeposit(command.getMaxDeposit())
                .minBedrooms(command.getMinBedrooms())
                .maxBedrooms(command.getMaxBedrooms())
                .minBathrooms(command.getMinBathrooms())
                .maxBathrooms(command.getMaxBathrooms())
                .minFloorArea(command.getMinFloorArea())
                .maxFloorArea(command.getMaxFloorArea())
                .city(command.getCity())
                .state(command.getState())
                .country(command.getCountry())
                .postalCode(command.getPostalCode())
                .latitude(command.getLatitude())
                .longitude(command.getLongitude())
                .maxDistance(command.getMaxDistance())
                .parkingRequired(command.getParkingRequired())
                .petAllowedRequired(command.getPetAllowedRequired())
                .furnishedRequired(command.getFurnishedRequired())
                .shortTermAvailableRequired(command.getShortTermAvailableRequired())
                .requiredOptions(command.getRequiredOptions())
                .sortBy(command.getSortBy())
                .sortDirection(command.getSortDirection())
                .availableFrom(command.getAvailableFrom())
                .availableTo(command.getAvailableTo())
                .recentDays(command.getRecentDays())
                .isSaved(false)
                .searchCount(1)
                .lastUsedAt(LocalDateTime.now())
                .build();
    }

    private void cleanupOldHistory(Long userId) {
        // Delete old non-saved history based on retention policy
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = searchHistoryRepository.deleteOldSearchHistory(userId, cutoffDate);
        
        if (deletedCount > 0) {
            log.debug("Cleaned up {} old search history entries for user: {}", deletedCount, userId);
        }
    }

    private SearchHistoryResponseDto convertToDto(SearchHistory searchHistory) {
        SearchHistoryResponseDto.SearchSummary summary = buildSearchSummary(searchHistory);

        return SearchHistoryResponseDto.builder()
                .id(searchHistory.getId())
                .userId(searchHistory.getUserId())
                .searchName(searchHistory.getSearchName())
                .keyword(searchHistory.getKeyword())
                .propertyTypes(searchHistory.getPropertyTypes())
                .rentalTypes(searchHistory.getRentalTypes())
                .minRentPrice(searchHistory.getMinRentPrice())
                .maxRentPrice(searchHistory.getMaxRentPrice())
                .minDeposit(searchHistory.getMinDeposit())
                .maxDeposit(searchHistory.getMaxDeposit())
                .minBedrooms(searchHistory.getMinBedrooms())
                .maxBedrooms(searchHistory.getMaxBedrooms())
                .minBathrooms(searchHistory.getMinBathrooms())
                .maxBathrooms(searchHistory.getMaxBathrooms())
                .minFloorArea(searchHistory.getMinFloorArea())
                .maxFloorArea(searchHistory.getMaxFloorArea())
                .city(searchHistory.getCity())
                .state(searchHistory.getState())
                .country(searchHistory.getCountry())
                .postalCode(searchHistory.getPostalCode())
                .latitude(searchHistory.getLatitude())
                .longitude(searchHistory.getLongitude())
                .maxDistance(searchHistory.getMaxDistance())
                .parkingRequired(searchHistory.getParkingRequired())
                .petAllowedRequired(searchHistory.getPetAllowedRequired())
                .furnishedRequired(searchHistory.getFurnishedRequired())
                .shortTermAvailableRequired(searchHistory.getShortTermAvailableRequired())
                .requiredOptions(searchHistory.getRequiredOptions())
                .sortBy(searchHistory.getSortBy())
                .sortDirection(searchHistory.getSortDirection())
                .availableFrom(searchHistory.getAvailableFrom())
                .availableTo(searchHistory.getAvailableTo())
                .recentDays(searchHistory.getRecentDays())
                .isSaved(searchHistory.getIsSaved())
                .searchCount(searchHistory.getSearchCount())
                .lastUsedAt(searchHistory.getLastUsedAt())
                .createdAt(searchHistory.getCreatedAt())
                .updatedAt(searchHistory.getUpdatedAt())
                .displayText(buildDisplayText(searchHistory))
                .searchSummary(summary)
                .build();
    }

    private SearchHistoryResponseDto.SearchSummary buildSearchSummary(SearchHistory searchHistory) {
        StringBuilder location = new StringBuilder();
        if (searchHistory.getCity() != null) location.append(searchHistory.getCity());
        if (searchHistory.getState() != null) {
            if (location.length() > 0) location.append(", ");
            location.append(searchHistory.getState());
        }

        String priceRange = null;
        if (searchHistory.getMinRentPrice() != null || searchHistory.getMaxRentPrice() != null) {
            priceRange = String.format("$%s - $%s",
                    searchHistory.getMinRentPrice() != null ? searchHistory.getMinRentPrice() : "0",
                    searchHistory.getMaxRentPrice() != null ? searchHistory.getMaxRentPrice() : "∞");
        }

        String propertyTypesStr = null;
        if (searchHistory.getPropertyTypes() != null && !searchHistory.getPropertyTypes().isEmpty()) {
            propertyTypesStr = searchHistory.getPropertyTypes().stream()
                    .map(PropertyType::name)
                    .collect(Collectors.joining(", "));
        }

        String amenities = null;
        int amenityCount = 0;
        if (Boolean.TRUE.equals(searchHistory.getParkingRequired())) amenityCount++;
        if (Boolean.TRUE.equals(searchHistory.getPetAllowedRequired())) amenityCount++;
        if (Boolean.TRUE.equals(searchHistory.getFurnishedRequired())) amenityCount++;
        if (Boolean.TRUE.equals(searchHistory.getShortTermAvailableRequired())) amenityCount++;
        if (amenityCount > 0) {
            amenities = amenityCount + " amenities required";
        }

        int totalFilters = 0;
        if (searchHistory.getKeyword() != null && !searchHistory.getKeyword().trim().isEmpty()) totalFilters++;
        if (location.length() > 0) totalFilters++;
        if (priceRange != null) totalFilters++;
        if (propertyTypesStr != null) totalFilters++;
        if (amenities != null) totalFilters++;

        return SearchHistoryResponseDto.SearchSummary.builder()
                .location(location.length() > 0 ? location.toString() : null)
                .priceRange(priceRange)
                .propertyTypes(propertyTypesStr)
                .amenities(amenities)
                .totalFilters(totalFilters)
                .build();
    }

    private String buildDisplayText(SearchHistory searchHistory) {
        StringBuilder display = new StringBuilder();

        if (searchHistory.getIsSaved() && searchHistory.getSearchName() != null) {
            return searchHistory.getSearchName();
        }

        if (searchHistory.getKeyword() != null && !searchHistory.getKeyword().trim().isEmpty()) {
            display.append("\"").append(searchHistory.getKeyword()).append("\"");
        }

        if (searchHistory.getCity() != null) {
            if (display.length() > 0) display.append(" in ");
            display.append(searchHistory.getCity());
        }

        if (display.length() == 0) {
            display.append("Property search");
        }

        return display.toString();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchHistoryStats {
        private long totalSearches;
        private long savedSearches;
        private long recentSearches;
    }
}