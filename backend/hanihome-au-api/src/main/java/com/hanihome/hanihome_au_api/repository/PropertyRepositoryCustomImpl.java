package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.domain.entity.QProperty;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.dto.request.PropertySearchCriteria;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PropertyRepositoryCustomImpl implements PropertyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QProperty property = QProperty.property;

    @Override
    public Page<Property> searchPropertiesWithCriteria(PropertySearchCriteria criteria, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        
        // Status filter (default to ACTIVE if not specified)
        PropertyStatus status = criteria.getStatus() != null ? criteria.getStatus() : PropertyStatus.ACTIVE;
        builder.and(property.status.eq(status));
        
        // Basic filters
        addBasicFilters(builder, criteria);
        
        // Price filters
        addPriceFilters(builder, criteria);
        
        // Area and room filters
        addAreaAndRoomFilters(builder, criteria);
        
        // Location filters
        addLocationFilters(builder, criteria);
        
        // Feature filters
        addFeatureFilters(builder, criteria);
        
        // Date filters
        addDateFilters(builder, criteria);
        
        // Floor filters
        addFloorFilters(builder, criteria);
        
        // Text search
        addTextSearch(builder, criteria);
        
        // Required options filter
        addRequiredOptionsFilter(builder, criteria);

        JPAQuery<Property> query = queryFactory
            .selectFrom(property)
            .where(builder);

        // Add sorting
        addSorting(query, criteria);

        // Apply pagination
        query = query.offset(pageable.getOffset()).limit(pageable.getPageSize());

        List<Property> results = query.fetch();
        long total = queryFactory
            .selectFrom(property)
            .where(builder)
            .fetchCount();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public List<Property> findPropertiesNearby(Double latitude, Double longitude, Double radiusKm, int limit) {
        NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
            "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
            latitude, property.latitude, property.longitude, longitude);

        return queryFactory
            .selectFrom(property)
            .where(property.status.eq(PropertyStatus.ACTIVE)
                .and(property.latitude.isNotNull())
                .and(property.longitude.isNotNull())
                .and(distance.loe(radiusKm)))
            .orderBy(distance.asc())
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Property> findSimilarProperties(Long propertyId, int limit) {
        Property targetProperty = queryFactory
            .selectFrom(property)
            .where(property.id.eq(propertyId))
            .fetchOne();

        if (targetProperty == null) {
            return List.of();
        }

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(property.status.eq(PropertyStatus.ACTIVE));
        builder.and(property.id.ne(propertyId));

        // Similar property type
        if (targetProperty.getPropertyType() != null) {
            builder.and(property.propertyType.eq(targetProperty.getPropertyType()));
        }

        // Similar rental type
        if (targetProperty.getRentalType() != null) {
            builder.and(property.rentalType.eq(targetProperty.getRentalType()));
        }

        // Similar price range (±30%)
        if (targetProperty.getMonthlyRent() != null) {
            BigDecimal minRent = targetProperty.getMonthlyRent().multiply(BigDecimal.valueOf(0.7));
            BigDecimal maxRent = targetProperty.getMonthlyRent().multiply(BigDecimal.valueOf(1.3));
            builder.and(property.monthlyRent.between(minRent, maxRent));
        }

        // Similar area (±20%)
        if (targetProperty.getArea() != null) {
            BigDecimal minArea = targetProperty.getArea().multiply(BigDecimal.valueOf(0.8));
            BigDecimal maxArea = targetProperty.getArea().multiply(BigDecimal.valueOf(1.2));
            builder.and(property.area.between(minArea, maxArea));
        }

        // Similar location (same city/district)
        if (StringUtils.hasText(targetProperty.getDistrict())) {
            builder.and(property.district.eq(targetProperty.getDistrict()));
        } else if (StringUtils.hasText(targetProperty.getCity())) {
            builder.and(property.city.eq(targetProperty.getCity()));
        }

        return queryFactory
            .selectFrom(property)
            .where(builder)
            .orderBy(property.createdDate.desc())
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Object[]> getPropertyStatistics() {
        return queryFactory
            .select(property.propertyType, property.count())
            .from(property)
            .where(property.status.eq(PropertyStatus.ACTIVE))
            .groupBy(property.propertyType)
            .orderBy(property.count().desc())
            .fetch()
            .stream()
            .map(tuple -> new Object[]{tuple.get(property.propertyType), tuple.get(property.count())})
            .toList();
    }

    @Override
    public List<Object[]> getPriceRangeStatistics() {
        return queryFactory
            .select(
                property.monthlyRent.min(),
                property.monthlyRent.max(),
                property.monthlyRent.avg(),
                property.count()
            )
            .from(property)
            .where(property.status.eq(PropertyStatus.ACTIVE)
                .and(property.monthlyRent.isNotNull()))
            .groupBy(property.propertyType)
            .fetch()
            .stream()
            .map(tuple -> new Object[]{
                tuple.get(property.monthlyRent.min()),
                tuple.get(property.monthlyRent.max()),
                tuple.get(property.monthlyRent.avg()),
                tuple.get(property.count())
            })
            .toList();
    }

    @Override
    public List<Property> findPropertiesWithExpiringSoonAvailability(int daysAhead) {
        LocalDate cutoffDate = LocalDate.now().plusDays(daysAhead);
        
        return queryFactory
            .selectFrom(property)
            .where(property.status.eq(PropertyStatus.ACTIVE)
                .and(property.availableDate.isNotNull())
                .and(property.availableDate.loe(cutoffDate))
                .and(property.availableDate.goe(LocalDate.now())))
            .orderBy(property.availableDate.asc())
            .fetch();
    }

    private void addBasicFilters(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (criteria.getPropertyType() != null) {
            builder.and(property.propertyType.eq(criteria.getPropertyType()));
        }
        if (criteria.getRentalType() != null) {
            builder.and(property.rentalType.eq(criteria.getRentalType()));
        }
        if (criteria.getLandlordId() != null) {
            builder.and(property.landlordId.eq(criteria.getLandlordId()));
        }
        if (criteria.getAgentId() != null) {
            builder.and(property.agentId.eq(criteria.getAgentId()));
        }
    }

    private void addPriceFilters(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (criteria.getMinDeposit() != null) {
            builder.and(property.deposit.goe(criteria.getMinDeposit()));
        }
        if (criteria.getMaxDeposit() != null) {
            builder.and(property.deposit.loe(criteria.getMaxDeposit()));
        }
        if (criteria.getMinMonthlyRent() != null) {
            builder.and(property.monthlyRent.goe(criteria.getMinMonthlyRent()));
        }
        if (criteria.getMaxMonthlyRent() != null) {
            builder.and(property.monthlyRent.loe(criteria.getMaxMonthlyRent()));
        }
        if (criteria.getMaxMaintenanceFee() != null) {
            builder.and(property.maintenanceFee.loe(criteria.getMaxMaintenanceFee()));
        }
    }

    private void addAreaAndRoomFilters(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (criteria.getMinArea() != null) {
            builder.and(property.area.goe(criteria.getMinArea()));
        }
        if (criteria.getMaxArea() != null) {
            builder.and(property.area.loe(criteria.getMaxArea()));
        }
        if (criteria.getMinRooms() != null) {
            builder.and(property.rooms.goe(criteria.getMinRooms()));
        }
        if (criteria.getMaxRooms() != null) {
            builder.and(property.rooms.loe(criteria.getMaxRooms()));
        }
        if (criteria.getMinBathrooms() != null) {
            builder.and(property.bathrooms.goe(criteria.getMinBathrooms()));
        }
        if (criteria.getMaxBathrooms() != null) {
            builder.and(property.bathrooms.loe(criteria.getMaxBathrooms()));
        }
    }

    private void addLocationFilters(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (StringUtils.hasText(criteria.getCity())) {
            builder.and(property.city.containsIgnoreCase(criteria.getCity()));
        }
        if (StringUtils.hasText(criteria.getDistrict())) {
            builder.and(property.district.containsIgnoreCase(criteria.getDistrict()));
        }
        if (StringUtils.hasText(criteria.getAddressKeyword())) {
            builder.and(property.address.containsIgnoreCase(criteria.getAddressKeyword()));
        }

        // Geographic proximity filter
        if (criteria.hasLocationFilter()) {
            NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
                "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
                criteria.getLatitude(), property.latitude, property.longitude, criteria.getLongitude());
            
            builder.and(property.latitude.isNotNull())
                   .and(property.longitude.isNotNull())
                   .and(distance.loe(criteria.getRadiusKm()));
        }
    }

    private void addFeatureFilters(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (criteria.getParkingRequired() != null) {
            builder.and(property.parkingAvailable.eq(criteria.getParkingRequired()));
        }
        if (criteria.getPetAllowed() != null) {
            builder.and(property.petAllowed.eq(criteria.getPetAllowed()));
        }
        if (criteria.getFurnished() != null) {
            builder.and(property.furnished.eq(criteria.getFurnished()));
        }
        if (criteria.getShortTermAvailable() != null) {
            builder.and(property.shortTermAvailable.eq(criteria.getShortTermAvailable()));
        }
    }

    private void addDateFilters(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (criteria.getAvailableFrom() != null) {
            builder.and(property.availableDate.goe(criteria.getAvailableFrom()));
        }
        if (criteria.getAvailableTo() != null) {
            builder.and(property.availableDate.loe(criteria.getAvailableTo()));
        }
        if (Boolean.TRUE.equals(criteria.getAvailableNow())) {
            builder.and(property.availableDate.loe(LocalDate.now()));
        }
        if (criteria.getCreatedAfter() != null) {
            builder.and(property.createdDate.goe(criteria.getCreatedAfter().atStartOfDay()));
        }
        if (criteria.getCreatedBefore() != null) {
            builder.and(property.createdDate.loe(criteria.getCreatedBefore().plusDays(1).atStartOfDay()));
        }
    }

    private void addFloorFilters(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (criteria.getMinFloor() != null) {
            builder.and(property.floor.goe(criteria.getMinFloor()));
        }
        if (criteria.getMaxFloor() != null) {
            builder.and(property.floor.loe(criteria.getMaxFloor()));
        }
        if (Boolean.TRUE.equals(criteria.getExcludeBasement())) {
            builder.and(property.floor.gt(0));
        }
        if (Boolean.TRUE.equals(criteria.getExcludeRooftop())) {
            builder.and(property.floor.lt(property.totalFloors).or(property.totalFloors.isNull()));
        }
    }

    private void addTextSearch(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (StringUtils.hasText(criteria.getKeyword())) {
            BooleanExpression titleMatch = property.title.containsIgnoreCase(criteria.getKeyword());
            BooleanExpression descMatch = property.description.containsIgnoreCase(criteria.getKeyword());
            BooleanExpression addressMatch = property.address.containsIgnoreCase(criteria.getKeyword());
            
            builder.and(titleMatch.or(descMatch).or(addressMatch));
        }
    }

    private void addRequiredOptionsFilter(BooleanBuilder builder, PropertySearchCriteria criteria) {
        if (criteria.getRequiredOptions() != null && !criteria.getRequiredOptions().isEmpty()) {
            for (String option : criteria.getRequiredOptions()) {
                builder.and(property.options.contains(option));
            }
        }
    }

    private void addSorting(JPAQuery<Property> query, PropertySearchCriteria criteria) {
        String sortBy = criteria.getSortBy();
        boolean ascending = !"desc".equalsIgnoreCase(criteria.getSortDirection());

        if (sortBy == null) {
            query.orderBy(property.createdDate.desc());
            return;
        }

        OrderSpecifier<?> orderSpecifier = switch (sortBy.toLowerCase()) {
            case "price" -> ascending ? property.monthlyRent.asc() : property.monthlyRent.desc();
            case "deposit" -> ascending ? property.deposit.asc() : property.deposit.desc();
            case "area" -> ascending ? property.area.asc() : property.area.desc();
            case "date", "created" -> ascending ? property.createdDate.asc() : property.createdDate.desc();
            case "modified" -> ascending ? property.modifiedDate.asc() : property.modifiedDate.desc();
            case "available" -> ascending ? property.availableDate.asc() : property.availableDate.desc();
            case "rooms" -> ascending ? property.rooms.asc() : property.rooms.desc();
            case "floor" -> ascending ? property.floor.asc() : property.floor.desc();
            case "distance" -> {
                if (criteria.hasLocationFilter()) {
                    NumberExpression<Double> distance = calculateDistance(
                        criteria.getLatitude(), criteria.getLongitude(),
                        property.latitude, property.longitude
                    );
                    yield ascending ? distance.asc() : distance.desc();
                }
                yield property.createdDate.desc();
            }
            default -> property.createdDate.desc();
        };

        query.orderBy(orderSpecifier);
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private NumberExpression<Double> calculateDistance(BigDecimal lat1, BigDecimal lon1, 
                                                      NumberExpression<BigDecimal> lat2, 
                                                      NumberExpression<BigDecimal> lon2) {
        return Expressions.numberTemplate(Double.class,
            "6371 * acos(greatest(-1, least(1, " +
            "cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + " +
            "sin(radians({0})) * sin(radians({1})))))",
            lat1, lat2, lon2, lon1
        );
    }
}