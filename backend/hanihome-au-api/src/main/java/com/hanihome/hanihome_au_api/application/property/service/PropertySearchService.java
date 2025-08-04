package com.hanihome.hanihome_au_api.application.property.service;

import com.hanihome.hanihome_au_api.application.property.dto.PropertyResponseDto;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import com.hanihome.hanihome_au_api.infrastructure.persistence.property.PropertyJpaEntity;
import com.hanihome.hanihome_au_api.infrastructure.persistence.property.QPropertyJpaEntity;
import com.hanihome.hanihome_au_api.presentation.dto.PropertySearchRequest;
import com.hanihome.hanihome_au_api.presentation.dto.PropertySearchResponse;
import com.hanihome.hanihome_au_api.presentation.dto.PropertySearchCursor;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PropertySearchService {
    
    private final EntityManager entityManager;
    private final PropertyApplicationService propertyApplicationService;
    
    /**
     * Advanced search for properties with multiple filter criteria
     */
    @Cacheable(value = "propertySearch", 
               key = "#request.hashCode()", 
               condition = "#request.getCursor() == null || !#request.getCursor().getUseCursor()")
    public PropertySearchResponse searchProperties(PropertySearchRequest request) {
        log.info("Searching properties with request: {}", request);
        
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QPropertyJpaEntity property = QPropertyJpaEntity.propertyJpaEntity;
        
        // Build dynamic query with filters
        BooleanBuilder whereClause = buildSearchCriteria(request, property);
        
        // Create base query
        JPAQuery<PropertyJpaEntity> baseQuery = queryFactory
                .selectFrom(property)
                .where(whereClause);
        
        // Apply sorting
        OrderSpecifier<?> orderSpecifier = buildOrderSpecifier(request, property);
        if (orderSpecifier != null) {
            baseQuery.orderBy(orderSpecifier);
        }
        
        // Apply cursor-based pagination if cursor is provided
        if (request.getCursor() != null && Boolean.TRUE.equals(request.getCursor().getUseCursor())) {
            applyCursorPagination(baseQuery, request.getCursor(), property, request.getSortBy());
        }
        
        // Count total elements for pagination (skip for cursor-based to improve performance)
        long totalElements = 0;
        if (request.getCursor() == null || !Boolean.TRUE.equals(request.getCursor().getUseCursor())) {
            totalElements = queryFactory
                    .selectFrom(property)
                    .where(whereClause)
                    .fetchCount();
        }
        
        // Apply pagination and fetch results
        List<PropertyJpaEntity> entities;
        Pageable pageable = null;
        
        if (request.getCursor() != null && Boolean.TRUE.equals(request.getCursor().getUseCursor())) {
            // Cursor-based pagination - fetch one extra to determine if there's a next page
            entities = baseQuery
                    .limit(request.getSize() + 1)
                    .fetch();
        } else {
            // Traditional offset-based pagination
            pageable = PageRequest.of(request.getPage(), request.getSize());
            entities = baseQuery
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }
        
        // Handle cursor-based pagination response
        if (request.getCursor() != null && Boolean.TRUE.equals(request.getCursor().getUseCursor())) {
            return buildCursorBasedResponse(entities, request);
        }
        
        // Traditional pagination response
        List<PropertyResponseDto> propertyDtos = entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Create page for metadata
        Page<PropertyResponseDto> page = new PageImpl<>(propertyDtos, pageable, totalElements);
        
        // Build response with metadata
        return PropertySearchResponse.builder()
                .properties(propertyDtos)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .numberOfElements(page.getNumberOfElements())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .filterSummary(buildFilterSummary(request))
                .build();
    }
    
    /**
     * Build dynamic search criteria using QueryDSL BooleanBuilder
     */
    private BooleanBuilder buildSearchCriteria(PropertySearchRequest request, QPropertyJpaEntity property) {
        BooleanBuilder builder = new BooleanBuilder();
        
        // Only show active properties by default
        builder.and(property.status.eq(PropertyJpaEntity.PropertyStatusEnum.ACTIVE));
        
        // Keyword search (title and description)
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            String keyword = "%" + request.getKeyword().toLowerCase() + "%";
            builder.and(
                property.title.lower().like(keyword)
                    .or(property.description.lower().like(keyword))
            );
        }
        
        // Property types filter
        if (request.getPropertyTypes() != null && !request.getPropertyTypes().isEmpty()) {
            BooleanExpression typeFilter = null;
            for (PropertyType type : request.getPropertyTypes()) {
                PropertyJpaEntity.PropertyTypeEnum jpaType = convertToJpaPropertyType(type);
                if (typeFilter == null) {
                    typeFilter = property.propertyType.eq(jpaType);
                } else {
                    typeFilter = typeFilter.or(property.propertyType.eq(jpaType));
                }
            }
            if (typeFilter != null) {
                builder.and(typeFilter);
            }
        }
        
        // Rental types filter
        if (request.getRentalTypes() != null && !request.getRentalTypes().isEmpty()) {
            BooleanExpression rentalFilter = null;
            for (RentalType type : request.getRentalTypes()) {
                PropertyJpaEntity.RentalTypeEnum jpaType = convertToJpaRentalType(type);
                if (rentalFilter == null) {
                    rentalFilter = property.rentalType.eq(jpaType);
                } else {
                    rentalFilter = rentalFilter.or(property.rentalType.eq(jpaType));
                }
            }
            if (rentalFilter != null) {
                builder.and(rentalFilter);
            }
        }
        
        // Price range filters
        if (request.getMinRentPrice() != null) {
            builder.and(property.monthlyRent.goe(request.getMinRentPrice()));
        }
        if (request.getMaxRentPrice() != null) {
            builder.and(property.monthlyRent.loe(request.getMaxRentPrice()));
        }
        
        // Deposit range filters
        if (request.getMinDeposit() != null) {
            builder.and(property.deposit.goe(request.getMinDeposit()));
        }
        if (request.getMaxDeposit() != null) {
            builder.and(property.deposit.loe(request.getMaxDeposit()));
        }
        
        // Room count filters
        if (request.getMinBedrooms() != null) {
            builder.and(property.rooms.goe(request.getMinBedrooms()));
        }
        if (request.getMaxBedrooms() != null) {
            builder.and(property.rooms.loe(request.getMaxBedrooms()));
        }
        
        // Bathroom count filters
        if (request.getMinBathrooms() != null) {
            builder.and(property.bathrooms.goe(request.getMinBathrooms()));
        }
        if (request.getMaxBathrooms() != null) {
            builder.and(property.bathrooms.loe(request.getMaxBathrooms()));
        }
        
        // Floor area filters
        if (request.getMinFloorArea() != null) {
            builder.and(property.area.goe(request.getMinFloorArea()));
        }
        if (request.getMaxFloorArea() != null) {
            builder.and(property.area.loe(request.getMaxFloorArea()));
        }
        
        // Location filters
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            builder.and(property.city.containsIgnoreCase(request.getCity()));
        }
        
        // Distance-based filtering (using Haversine formula)
        if (request.getLatitude() != null && request.getLongitude() != null && request.getMaxDistance() != null) {
            builder.and(buildDistanceFilter(property, request.getLatitude(), request.getLongitude(), request.getMaxDistance()));
        }
        
        // Amenity filters
        if (Boolean.TRUE.equals(request.getParkingRequired())) {
            builder.and(property.parkingAvailable.isTrue());
        }
        if (Boolean.TRUE.equals(request.getPetAllowedRequired())) {
            builder.and(property.petAllowed.isTrue());
        }
        if (Boolean.TRUE.equals(request.getFurnishedRequired())) {
            builder.and(property.furnished.isTrue());
        }
        if (Boolean.TRUE.equals(request.getShortTermAvailableRequired())) {
            builder.and(property.shortTermAvailable.isTrue());
        }
        
        // Recent properties filter
        if (request.getRecentDays() != null) {
            LocalDateTime since = LocalDateTime.now().minusDays(request.getRecentDays());
            builder.and(property.createdDate.goe(since));
        }
        
        return builder;
    }
    
    /**
     * Build distance filter using Haversine formula approximation
     */
    private BooleanExpression buildDistanceFilter(QPropertyJpaEntity property, 
                                                BigDecimal centerLat, BigDecimal centerLon, BigDecimal maxDistance) {
        // Simplified distance calculation for PostgreSQL
        // Using a rectangular approximation for better performance
        // Real implementation should use PostGIS or similar for accurate distance calculation
        
        double latDiff = maxDistance.doubleValue() / 111.0; // Approximate degrees per km for latitude
        double lonDiff = maxDistance.doubleValue() / (111.0 * Math.cos(Math.toRadians(centerLat.doubleValue())));
        
        BigDecimal minLat = centerLat.subtract(BigDecimal.valueOf(latDiff));
        BigDecimal maxLat = centerLat.add(BigDecimal.valueOf(latDiff));
        BigDecimal minLon = centerLon.subtract(BigDecimal.valueOf(lonDiff));
        BigDecimal maxLon = centerLon.add(BigDecimal.valueOf(lonDiff));
        
        return property.latitude.between(minLat, maxLat)
                .and(property.longitude.between(minLon, maxLon));
    }
    
    /**
     * Build order specifier for sorting
     */
    private OrderSpecifier<?> buildOrderSpecifier(PropertySearchRequest request, QPropertyJpaEntity property) {
        Order order = "asc".equalsIgnoreCase(request.getSortDirection()) ? Order.ASC : Order.DESC;
        
        return switch (request.getSortBy().toLowerCase()) {
            case "rentprice" -> new OrderSpecifier<>(order, property.monthlyRent);
            case "createdat" -> new OrderSpecifier<>(order, property.createdDate);
            case "area" -> new OrderSpecifier<>(order, property.area);
            case "deposit" -> new OrderSpecifier<>(order, property.deposit);
            default -> new OrderSpecifier<>(order, property.createdDate);
        };
    }
    
    /**
     * Convert domain PropertyType to JPA PropertyTypeEnum
     */
    private PropertyJpaEntity.PropertyTypeEnum convertToJpaPropertyType(PropertyType type) {
        return switch (type) {
            case APARTMENT -> PropertyJpaEntity.PropertyTypeEnum.APARTMENT;
            case VILLA -> PropertyJpaEntity.PropertyTypeEnum.VILLA;
            case STUDIO -> PropertyJpaEntity.PropertyTypeEnum.STUDIO;
            case TWO_ROOM -> PropertyJpaEntity.PropertyTypeEnum.TWO_ROOM;
            case THREE_ROOM -> PropertyJpaEntity.PropertyTypeEnum.THREE_ROOM;
            case OFFICETEL -> PropertyJpaEntity.PropertyTypeEnum.OFFICETEL;
            case HOUSE -> PropertyJpaEntity.PropertyTypeEnum.HOUSE;
            // Map additional types to closest match or throw exception
            case TOWNHOUSE -> PropertyJpaEntity.PropertyTypeEnum.HOUSE;
            case CONDO -> PropertyJpaEntity.PropertyTypeEnum.APARTMENT;
            case ROOM -> PropertyJpaEntity.PropertyTypeEnum.STUDIO;
        };
    }
    
    /**
     * Convert domain RentalType to JPA RentalTypeEnum
     */
    private PropertyJpaEntity.RentalTypeEnum convertToJpaRentalType(RentalType type) {
        return switch (type) {
            case MONTHLY -> PropertyJpaEntity.RentalTypeEnum.MONTHLY;
            case JEONSE -> PropertyJpaEntity.RentalTypeEnum.JEONSE;
            case SALE -> PropertyJpaEntity.RentalTypeEnum.SALE;
        };
    }
    
    /**
     * Convert JPA entity to DTO
     */
    private PropertyResponseDto convertToDto(PropertyJpaEntity entity) {
        // Use existing conversion logic from PropertyApplicationService
        return propertyApplicationService.convertToDto(entity);
    }
    
    /**
     * Build filter summary for response metadata
     */
    private PropertySearchResponse.SearchFilterSummary buildFilterSummary(PropertySearchRequest request) {
        StringBuilder summary = new StringBuilder();
        int filterCount = 0;
        
        String priceRange = null;
        if (request.getMinRentPrice() != null || request.getMaxRentPrice() != null) {
            priceRange = String.format("$%s - $%s", 
                request.getMinRentPrice() != null ? request.getMinRentPrice() : "0",
                request.getMaxRentPrice() != null ? request.getMaxRentPrice() : "∞");
            filterCount++;
        }
        
        String locationSummary = null;
        if (request.getCity() != null) {
            locationSummary = request.getCity();
            filterCount++;
        }
        
        String amenitiesSummary = null;
        if (Boolean.TRUE.equals(request.getParkingRequired()) ||
            Boolean.TRUE.equals(request.getPetAllowedRequired()) ||
            Boolean.TRUE.equals(request.getFurnishedRequired())) {
            amenitiesSummary = "Specific amenities required";
            filterCount++;
        }
        
        return PropertySearchResponse.SearchFilterSummary.builder()
                .keyword(request.getKeyword())
                .priceRange(priceRange)
                .locationSummary(locationSummary)
                .amenitiesSummary(amenitiesSummary)
                .totalFiltersApplied(filterCount)
                .build();
    }
    
    /**
     * Apply cursor-based pagination to query
     */
    private void applyCursorPagination(JPAQuery<PropertyJpaEntity> query, PropertySearchCursor cursor, 
                                     QPropertyJpaEntity property, String sortBy) {
        if (cursor.getLastId() == null) {
            return;
        }
        
        switch (sortBy.toLowerCase()) {
            case "rentprice" -> {
                if (cursor.getLastSortValue() != null) {
                    query.where(property.monthlyRent.gt(new BigDecimal(cursor.getLastSortValue()))
                            .or(property.monthlyRent.eq(new BigDecimal(cursor.getLastSortValue()))
                                    .and(property.id.gt(cursor.getLastId()))));
                }
            }
            case "createdat" -> {
                if (cursor.getLastCreatedAt() != null) {
                    query.where(property.createdDate.lt(cursor.getLastCreatedAt())
                            .or(property.createdDate.eq(cursor.getLastCreatedAt())
                                    .and(property.id.gt(cursor.getLastId()))));
                }
            }
            default -> query.where(property.id.gt(cursor.getLastId()));
        }
    }
    
    /**
     * Build cursor-based pagination response
     */
    private PropertySearchResponse buildCursorBasedResponse(List<PropertyJpaEntity> entities, PropertySearchRequest request) {
        boolean hasNext = entities.size() > request.getSize();
        
        // Remove extra entity if it exists
        if (hasNext) {
            entities = entities.subList(0, request.getSize());
        }
        
        List<PropertyResponseDto> propertyDtos = entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        PropertySearchCursor nextCursor = null;
        if (hasNext && !entities.isEmpty()) {
            PropertyJpaEntity lastEntity = entities.get(entities.size() - 1);
            nextCursor = PropertySearchCursor.builder()
                    .lastId(lastEntity.getId())
                    .lastCreatedAt(lastEntity.getCreatedDate())
                    .lastSortValue(getSortValue(lastEntity, request.getSortBy()))
                    .useCursor(true)
                    .build();
        }
        
        return PropertySearchResponse.builder()
                .properties(propertyDtos)
                .currentPage(0) // Not applicable for cursor pagination
                .totalPages(0)  // Not calculated for performance
                .totalElements(0) // Not calculated for performance
                .numberOfElements(propertyDtos.size())
                .size(request.getSize())
                .first(request.getCursor().getLastId() == null)
                .last(!hasNext)
                .hasNext(hasNext)
                .hasPrevious(request.getCursor().getLastId() != null)
                .nextCursor(nextCursor)
                .filterSummary(buildFilterSummary(request))
                .build();
    }
    
    /**
     * Get sort value from entity based on sort field
     */
    private String getSortValue(PropertyJpaEntity entity, String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "rentprice" -> entity.getMonthlyRent() != null ? entity.getMonthlyRent().toString() : "0";
            case "createdat" -> entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : "";
            case "area" -> entity.getArea() != null ? entity.getArea().toString() : "0";
            case "deposit" -> entity.getDeposit() != null ? entity.getDeposit().toString() : "0";
            default -> entity.getId().toString();
        };
    }
}