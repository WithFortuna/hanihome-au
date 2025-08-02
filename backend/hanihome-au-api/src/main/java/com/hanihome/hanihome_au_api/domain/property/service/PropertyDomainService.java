package com.hanihome.hanihome_au_api.domain.property.service;

import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.exception.PropertyException;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain Service for Property-related business operations
 * Contains logic that doesn't naturally fit within a single aggregate
 */
public class PropertyDomainService {
    
    private final PropertyRepository propertyRepository;

    public PropertyDomainService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public boolean canUserManageProperty(UserId userId, Property property) {
        return property.isOwnedBy(userId);
    }

    public void validatePropertyForActivation(Property property) {
        if (property == null) {
            throw new PropertyException.PropertyValidationException("Property cannot be null");
        }
        
        if (property.getTitle() == null || property.getTitle().trim().isEmpty()) {
            throw new PropertyException.PropertyValidationException("Property must have a title to be activated");
        }
        
        if (property.getAddress() == null) {
            throw new PropertyException.PropertyValidationException("Property must have an address to be activated");
        }
        
        if (property.getRentPrice() == null) {
            throw new PropertyException.PropertyValidationException("Property must have rent price to be activated");
        }
        
        if (property.getSpecs() == null) {
            throw new PropertyException.PropertyValidationException("Property must have specifications to be activated");
        }
    }

    /**
     * Calculates average rent in a specific area using repository
     */
    public Money calculateAverageRentInArea(Address area, Double radiusKm) {
        return propertyRepository.calculateAverageRentInArea(area, radiusKm);
    }

    /**
     * Legacy method for backward compatibility
     */
    public Money calculateAverageRentInArea(List<Property> propertiesInArea) {
        if (propertiesInArea.isEmpty()) {
            return Money.zero("AUD");
        }

        List<Property> activeProperties = propertiesInArea.stream()
                .filter(p -> p.getStatus() == PropertyStatus.ACTIVE)
                .toList();

        if (activeProperties.isEmpty()) {
            return Money.zero("AUD");
        }

        BigDecimal totalRent = activeProperties.stream()
                .map(p -> p.getRentPrice().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = totalRent.divide(
                BigDecimal.valueOf(activeProperties.size()), 
                2, 
                java.math.RoundingMode.HALF_UP
        );

        return Money.of(average, "AUD");
    }

    /**
     * Validates if owner can create more properties based on business rules
     */
    public boolean canOwnerCreateMoreProperties(UserId ownerId) {
        // Business rule: Maximum 10 active properties per owner
        long activeCount = propertyRepository.countByStatus(PropertyStatus.ACTIVE);
        return activeCount < 10;
    }

    /**
     * Finds similar properties for recommendation
     */
    public List<Property> findSimilarProperties(Property property, int limit) {
        return propertyRepository.findSimilarProperties(property, limit);
    }

    public boolean isPriceCompetitive(Property property, List<Property> similarProperties) {
        Money averagePrice = calculateAverageRentInArea(similarProperties);
        
        if (averagePrice.getAmount().equals(BigDecimal.ZERO)) {
            return true;
        }

        BigDecimal propertyPrice = property.getRentPrice().getAmount();
        BigDecimal averageAmount = averagePrice.getAmount();
        
        BigDecimal ratio = propertyPrice.divide(averageAmount, 2, java.math.RoundingMode.HALF_UP);
        
        return ratio.compareTo(BigDecimal.valueOf(1.2)) <= 0;
    }

    public void validatePropertyTransition(Property property, PropertyStatus newStatus) {
        if (!property.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition property %s from %s to %s", 
                    property.getId(), property.getStatus(), newStatus));
        }

        if (newStatus == PropertyStatus.ACTIVE) {
            validatePropertyForActivation(property);
        }
    }
}