package com.hanihome.hanihome_au_api.domain.property.entity;

import com.hanihome.hanihome_au_api.domain.property.valueobject.*;
import com.hanihome.hanihome_au_api.domain.property.event.PropertyCreatedEvent;
import com.hanihome.hanihome_au_api.domain.property.event.PropertyStatusChangedEvent;
import com.hanihome.hanihome_au_api.domain.property.event.PropertyPriceChangedEvent;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.shared.entity.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Property Aggregate Root
 * Encapsulates all business rules and logic related to property management
 */
public class Property extends AggregateRoot<PropertyId> {
    private PropertyId id;
    private UserId ownerId;
    private String title;
    private String description;
    private PropertyType type;
    private RentalType rentalType;
    private PropertyStatus status;
    private Address address;
    private PropertySpecs specs;
    private Money rentPrice;
    private Money depositAmount;
    private LocalDateTime availableFrom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserId approvedBy;
    private LocalDateTime approvedAt;
    
    protected Property() {}

    private Property(PropertyId id, UserId ownerId, String title, String description,
                   PropertyType type, RentalType rentalType, Address address,
                   PropertySpecs specs, Money rentPrice, Money depositAmount) {
        this.id = Objects.requireNonNull(id, "Property ID cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "Owner ID cannot be null");
        this.title = validateTitle(title);
        this.description = description;
        this.type = Objects.requireNonNull(type, "Property type cannot be null");
        this.rentalType = Objects.requireNonNull(rentalType, "Rental type cannot be null");
        this.status = PropertyStatus.DRAFT;
        this.address = Objects.requireNonNull(address, "Address cannot be null");
        this.specs = Objects.requireNonNull(specs, "Property specs cannot be null");
        this.rentPrice = Objects.requireNonNull(rentPrice, "Rent price cannot be null");
        this.depositAmount = Objects.requireNonNull(depositAmount, "Deposit amount cannot be null");
        
        validatePricing(rentPrice, depositAmount);
        
        this.availableFrom = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new PropertyCreatedEvent(id, ownerId, title, type));
    }

    public static Property create(PropertyId id, UserId ownerId, String title, String description,
                                PropertyType type, RentalType rentalType, Address address,
                                PropertySpecs specs, Money rentPrice, Money depositAmount) {
        return new Property(id, ownerId, title, description, type, rentalType, 
                          address, specs, rentPrice, depositAmount);
    }

    /**
     * Updates property details with business rule validation
     */
    public void updateDetails(String title, String description, PropertySpecs specs) {
        ensureCanBeModified();
        
        this.title = validateTitle(title);
        this.description = description;
        this.specs = Objects.requireNonNull(specs, "Property specs cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates pricing with validation and domain event
     */
    public void updatePricing(Money rentPrice, Money depositAmount) {
        ensureCanBeModified();
        
        Objects.requireNonNull(rentPrice, "Rent price cannot be null");
        Objects.requireNonNull(depositAmount, "Deposit amount cannot be null");
        
        validatePricing(rentPrice, depositAmount);
        
        Money oldRentPrice = this.rentPrice;
        this.rentPrice = rentPrice;
        this.depositAmount = depositAmount;
        this.updatedAt = LocalDateTime.now();
        
        // Raise domain event for significant price changes
        if (isPriceChangeSignificant(oldRentPrice, rentPrice)) {
            addDomainEvent(new PropertyPriceChangedEvent(id, oldRentPrice, rentPrice));
        }
    }

    /**
     * Changes property status with business rule enforcement
     */
    public void changeStatus(PropertyStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", status, newStatus));
        }
        
        // Additional business rules for specific transitions
        if (newStatus == PropertyStatus.ACTIVE) {
            validateForActivation();
        }
        
        PropertyStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        if (newStatus == PropertyStatus.ACTIVE) {
            this.approvedAt = LocalDateTime.now();
        }
        
        addDomainEvent(new PropertyStatusChangedEvent(id, oldStatus, newStatus));
    }

    public void activate() {
        changeStatus(PropertyStatus.ACTIVE);
    }

    public void deactivate() {
        changeStatus(PropertyStatus.INACTIVE);
    }

    public void markAsRented() {
        changeStatus(PropertyStatus.RENTED);
    }

    public boolean isAvailableForRent() {
        return status.isAvailableForRent() && 
               (availableFrom == null || availableFrom.isBefore(LocalDateTime.now()));
    }

    public boolean isOwnedBy(UserId userId) {
        return this.ownerId.equals(userId);
    }

    public Money getTotalUpfrontCost() {
        return rentPrice.add(depositAmount);
    }

    /**
     * Approves property with validation and domain event
     */
    public void approve(UserId approvedBy) {
        if (this.status != PropertyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only properties pending approval can be approved");
        }
        
        validateForActivation();
        
        this.status = PropertyStatus.ACTIVE;
        this.approvedBy = Objects.requireNonNull(approvedBy, "Approved by cannot be null");
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        addDomainEvent(new PropertyStatusChangedEvent(id, PropertyStatus.PENDING_APPROVAL, PropertyStatus.ACTIVE));
    }

    /**
     * Rejects property with reason
     */
    public void reject(String reason) {
        if (this.status != PropertyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only properties pending approval can be rejected");
        }
        
        changeStatus(PropertyStatus.INACTIVE);
    }

    /**
     * Checks if user can manage this property
     */
    public boolean canBeAccessedBy(UserId userId) {
        return this.ownerId.equals(userId);
    }

    /**
     * Calculates total monthly cost
     */
    public Money getTotalMonthlyCost() {
        return rentPrice; // Could include maintenance fees in future
    }

    /**
     * Checks if property is suitable for given criteria
     */
    public boolean matchesCriteria(PropertySpecs minimumSpecs, Money maxBudget) {
        return specs.getBedrooms() >= minimumSpecs.getBedrooms() &&
               specs.getBathrooms() >= minimumSpecs.getBathrooms() &&
               rentPrice.isLessThan(maxBudget) &&
               status.isAvailableForRent();
    }

    // Private validation methods
    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        return title.trim();
    }

    private void validatePricing(Money rentPrice, Money depositAmount) {
        if (!rentPrice.getCurrency().equals(depositAmount.getCurrency())) {
            throw new IllegalArgumentException("Rent price and deposit must be in same currency");
        }
        
        if (rentPrice.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rent price must be positive");
        }
        
        if (depositAmount.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Deposit amount cannot be negative");
        }
    }

    private void ensureCanBeModified() {
        if (status == PropertyStatus.RENTED || status == PropertyStatus.DELETED) {
            throw new IllegalStateException("Cannot modify property in status: " + status);
        }
    }

    private void validateForActivation() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalStateException("Property must have a title to be activated");
        }
        
        if (address == null) {
            throw new IllegalStateException("Property must have an address to be activated");
        }
        
        if (rentPrice == null) {
            throw new IllegalStateException("Property must have rent price to be activated");
        }
        
        if (specs == null) {
            throw new IllegalStateException("Property must have specifications to be activated");
        }
    }

    private boolean isPriceChangeSignificant(Money oldPrice, Money newPrice) {
        if (oldPrice == null || newPrice == null) return true;
        
        var changePercentage = newPrice.subtract(oldPrice)
                                     .getAmount()
                                     .abs()
                                     .divide(oldPrice.getAmount(), 2, java.math.RoundingMode.HALF_UP);
        
        return changePercentage.compareTo(java.math.BigDecimal.valueOf(0.1)) > 0; // 10% change
    }

    // Getters
    public PropertyId getId() { return id; }
    public UserId getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public PropertyType getType() { return type; }
    public RentalType getRentalType() { return rentalType; }
    public PropertyStatus getStatus() { return status; }
    public Address getAddress() { return address; }
    public PropertySpecs getSpecs() { return specs; }
    public Money getRentPrice() { return rentPrice; }
    public Money getDepositAmount() { return depositAmount; }
    public LocalDateTime getAvailableFrom() { return availableFrom; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}