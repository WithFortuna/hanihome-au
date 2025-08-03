package com.hanihome.hanihome_au_api.application.property.service;

import com.hanihome.hanihome_au_api.infrastructure.persistence.property.PropertyJpaEntity;
import com.hanihome.hanihome_au_api.infrastructure.persistence.property.QPropertyJpaEntity;
import com.hanihome.hanihome_au_api.presentation.dto.AutocompleteRequest;
import com.hanihome.hanihome_au_api.presentation.dto.AutocompleteResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PropertyAutocompleteService {
    
    private final EntityManager entityManager;
    
    /**
     * Generate autocomplete suggestions based on query
     */
    public AutocompleteResponse getAutocompleteSuggestions(AutocompleteRequest request) {
        log.info("Getting autocomplete suggestions for query: {}", request.getQuery());
        
        List<AutocompleteResponse.AutocompleteSuggestion> suggestions = new ArrayList<>();
        String query = request.getQuery().toLowerCase().trim();
        
        // Get suggestions based on type
        switch (request.getType().toLowerCase()) {
            case "location" -> suggestions.addAll(getLocationSuggestions(query, request.getLimit()));
            case "property_title" -> suggestions.addAll(getPropertyTitleSuggestions(query, request.getLimit()));
            case "all" -> {
                suggestions.addAll(getLocationSuggestions(query, request.getLimit() / 2));
                suggestions.addAll(getPropertyTitleSuggestions(query, request.getLimit() / 2));
                suggestions.addAll(getPopularSearchSuggestions(query, Math.max(2, request.getLimit() / 4)));
            }
        }
        
        // Sort by score and limit results
        suggestions = suggestions.stream()
                .sorted((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()))
                .limit(request.getLimit())
                .collect(Collectors.toList());
        
        return AutocompleteResponse.builder()
                .suggestions(suggestions)
                .query(request.getQuery())
                .totalSuggestions(suggestions.size())
                .build();
    }
    
    /**
     * Get location-based suggestions
     */
    private List<AutocompleteResponse.AutocompleteSuggestion> getLocationSuggestions(String query, Integer limit) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QPropertyJpaEntity property = QPropertyJpaEntity.propertyJpaEntity;
        
        // Search cities
        List<String> cities = queryFactory
                .select(property.city)
                .from(property)
                .where(property.city.isNotNull()
                        .and(property.city.toLowerCase().contains(query))
                        .and(property.status.eq(PropertyJpaEntity.PropertyStatusEnum.ACTIVE)))
                .groupBy(property.city)
                .orderBy(property.count().desc())
                .limit(limit)
                .fetch();
        
        List<AutocompleteResponse.AutocompleteSuggestion> suggestions = new ArrayList<>();
        
        for (String city : cities) {
            suggestions.add(AutocompleteResponse.AutocompleteSuggestion.builder()
                    .text(city)
                    .type("location")
                    .highlighted(highlightQuery(city, query))
                    .score(calculateLocationScore(city, query))
                    .context("City")
                    .build());
        }
        
        return suggestions;
    }
    
    /**
     * Get property title suggestions
     */
    private List<AutocompleteResponse.AutocompleteSuggestion> getPropertyTitleSuggestions(String query, Integer limit) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QPropertyJpaEntity property = QPropertyJpaEntity.propertyJpaEntity;
        
        List<PropertyJpaEntity> properties = queryFactory
                .selectFrom(property)
                .where(property.title.isNotNull()
                        .and(property.title.toLowerCase().contains(query))
                        .and(property.status.eq(PropertyJpaEntity.PropertyStatusEnum.ACTIVE)))
                .orderBy(property.createdDate.desc())
                .limit(limit)
                .fetch();
        
        return properties.stream()
                .map(p -> AutocompleteResponse.AutocompleteSuggestion.builder()
                        .text(p.getTitle())
                        .type("property_title")
                        .highlighted(highlightQuery(p.getTitle(), query))
                        .score(calculateTitleScore(p.getTitle(), query))
                        .context(p.getCity() + " • " + p.getPropertyType().name())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Get popular search suggestions
     */
    private List<AutocompleteResponse.AutocompleteSuggestion> getPopularSearchSuggestions(String query, Integer limit) {
        // Static popular searches that match the query
        Map<String, Integer> popularSearches = Map.of(
                "apartment", 100,
                "studio", 90,
                "two room", 85,
                "villa", 80,
                "house", 75,
                "officetel", 70,
                "furnished", 60,
                "parking", 55,
                "pet friendly", 50,
                "short term", 45
        );
        
        return popularSearches.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains(query))
                .map(entry -> AutocompleteResponse.AutocompleteSuggestion.builder()
                        .text(entry.getKey())
                        .type("popular_search")
                        .highlighted(highlightQuery(entry.getKey(), query))
                        .score(entry.getValue())
                        .context("Popular search")
                        .build())
                .sorted((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Highlight query matches in suggestion text
     */
    private String highlightQuery(String text, String query) {
        if (text == null || query == null || query.isEmpty()) {
            return text;
        }
        
        String lowercaseText = text.toLowerCase();
        String lowercaseQuery = query.toLowerCase();
        
        if (!lowercaseText.contains(lowercaseQuery)) {
            return text;
        }
        
        int startIndex = lowercaseText.indexOf(lowercaseQuery);
        int endIndex = startIndex + query.length();
        
        return text.substring(0, startIndex) + 
               "<mark>" + text.substring(startIndex, endIndex) + "</mark>" + 
               text.substring(endIndex);
    }
    
    /**
     * Calculate relevance score for location suggestions
     */
    private Integer calculateLocationScore(String location, String query) {
        if (location == null || query == null) {
            return 0;
        }
        
        String lowercaseLocation = location.toLowerCase();
        String lowercaseQuery = query.toLowerCase();
        
        // Exact match gets highest score
        if (lowercaseLocation.equals(lowercaseQuery)) {
            return 100;
        }
        
        // Starts with query gets high score
        if (lowercaseLocation.startsWith(lowercaseQuery)) {
            return 80;
        }
        
        // Contains query gets medium score
        if (lowercaseLocation.contains(lowercaseQuery)) {
            return 60;
        }
        
        return 40;
    }
    
    /**
     * Calculate relevance score for title suggestions
     */
    private Integer calculateTitleScore(String title, String query) {
        if (title == null || query == null) {
            return 0;
        }
        
        String lowercaseTitle = title.toLowerCase();
        String lowercaseQuery = query.toLowerCase();
        
        int score = 0;
        
        // Title starts with query
        if (lowercaseTitle.startsWith(lowercaseQuery)) {
            score += 50;
        }
        
        // Query appears in title
        if (lowercaseTitle.contains(lowercaseQuery)) {
            score += 30;
        }
        
        // Shorter titles get slight boost for relevance
        score += Math.max(0, 100 - title.length());
        
        return score;
    }
}