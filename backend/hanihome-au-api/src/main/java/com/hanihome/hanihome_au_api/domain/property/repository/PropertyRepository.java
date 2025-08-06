package com.hanihome.hanihome_au_api.domain.property.repository;

import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertySpecs;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Property aggregate
 * Defines domain-focused query methods
 */
public interface PropertyRepository {
    
    // Basic CRUD operations
    Property save(Property property);
    Optional<Property> findById(PropertyId id);
    List<Property> findAllById(Collection<Long> ids);
    void delete(Property property);
    long count();
    
    // Domain-specific queries
    List<Property> findByOwnerId(UserId ownerId);
    List<Property> findByStatus(PropertyStatus status);
    List<Property> findByType(PropertyType type);
    List<Property> findAvailableProperties();
    
    // Location-based queries
    List<Property> findNearByLocation(Double latitude, Double longitude, Double radiusKm);
    
    // Complex domain queries
    List<Property> findSimilarProperties(Property property, int limit);
    List<Property> findByBudgetRange(Money minBudget, Money maxBudget);
    List<Property> findBySpecsAndBudget(PropertySpecs minimumSpecs, Money maxBudget);
    
    // Analytics queries
    long countByStatus(PropertyStatus status);
    Money calculateAverageRentInArea(Address area, Double radiusKm);
    
    // Exists checks for business rules
    boolean existsByOwnerIdAndStatus(UserId ownerId, PropertyStatus status);
}