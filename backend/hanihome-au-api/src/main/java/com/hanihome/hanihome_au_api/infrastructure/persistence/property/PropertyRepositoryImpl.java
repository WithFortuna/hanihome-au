package com.hanihome.hanihome_au_api.infrastructure.persistence.property;

import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.property.valueobject.*;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PropertyRepositoryImpl implements PropertyRepository {
    
    private final PropertyJpaRepository propertyJpaRepository;

    public PropertyRepositoryImpl(PropertyJpaRepository propertyJpaRepository) {
        this.propertyJpaRepository = propertyJpaRepository;
    }

    @Override
    public Property save(Property property) {
        PropertyJpaEntity entity = mapToEntity(property);
        PropertyJpaEntity savedEntity = propertyJpaRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<Property> findById(PropertyId id) {
        return propertyJpaRepository.findById(id.getValue())
                .map(this::mapToDomain);
    }

    @Override
    public List<Property> findByOwnerId(UserId ownerId) {
        List<PropertyJpaEntity> entities = propertyJpaRepository.findByLandlordId(ownerId.getValue());
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findByStatus(PropertyStatus status) {
        PropertyJpaEntity.PropertyStatusEnum statusEnum = 
                PropertyJpaEntity.PropertyStatusEnum.valueOf(status.name());
        List<PropertyJpaEntity> entities = propertyJpaRepository.findByStatus(statusEnum);
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private PropertyJpaEntity mapToEntity(Property property) {
        PropertyJpaEntity entity = new PropertyJpaEntity();
        entity.setId(property.getId().getValue());
        entity.setTitle(property.getTitle());
        entity.setDescription(property.getDescription());
        
        // Map address - simplified approach
        if (property.getAddress() != null) {
            entity.setAddress(property.getAddress().getFullAddress());
            entity.setCity(property.getAddress().getCity());
        }
        
        // Map property type and rental type
        entity.setPropertyType(PropertyJpaEntity.PropertyTypeEnum.valueOf(property.getType().name()));
        entity.setRentalType(PropertyJpaEntity.RentalTypeEnum.valueOf(property.getRentalType().name()));
        entity.setStatus(PropertyJpaEntity.PropertyStatusEnum.valueOf(property.getStatus().name()));
        
        entity.setLandlordId(property.getOwnerId().getValue());
        
        // Map price info
        if (property.getRentPrice() != null) {
            entity.setMonthlyRent(property.getRentPrice().getAmount());
        }
        if (property.getDepositAmount() != null) {
            entity.setDeposit(property.getDepositAmount().getAmount());
        }
        
        // Map specs
        if (property.getSpecs() != null) {
            entity.setBathrooms(property.getSpecs().getBathrooms());
            entity.setRooms(property.getSpecs().getBedrooms());
            entity.setFloor(property.getSpecs().getFloor());
            entity.setTotalFloors(property.getSpecs().getTotalFloors());
            if (property.getSpecs().getFloorArea() != null) {
                entity.setArea(BigDecimal.valueOf(property.getSpecs().getFloorArea()));
            }
        }
        
        // Set timestamps
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());
        
        return entity;
    }

    private Property mapToDomain(PropertyJpaEntity entity) {
        // Create value objects
        PropertyId propertyId = PropertyId.of(entity.getId());
        UserId ownerId = UserId.of(entity.getLandlordId());
        
        // Create address (simplified)
        Address address = new Address(
            entity.getAddress() != null ? entity.getAddress() : "",
            entity.getCity() != null ? entity.getCity() : "",
            "",  // state
            "",  // country  
            entity.getZipCode(),
            entity.getLatitude() != null ? entity.getLatitude().doubleValue() : null,
            entity.getLongitude() != null ? entity.getLongitude().doubleValue() : null
        );
        
        // Create property specs
        PropertySpecs specs = new PropertySpecs(
            entity.getRooms() != null ? entity.getRooms() : 0,
            entity.getBathrooms() != null ? entity.getBathrooms() : 0,
            entity.getArea() != null ? entity.getArea().doubleValue() : null,
            entity.getFloor(),
            entity.getTotalFloors(),
            entity.getParkingAvailable() != null ? entity.getParkingAvailable() : false,
            entity.getPetAllowed() != null ? entity.getPetAllowed() : false,
            false  // hasElevator - not in DB
        );
        
        // Create money objects
        Money rentPrice = entity.getMonthlyRent() != null ? 
            Money.of(entity.getMonthlyRent(), "AUD") : Money.of(BigDecimal.ZERO, "AUD");
        Money depositAmount = entity.getDeposit() != null ? 
            Money.of(entity.getDeposit(), "AUD") : Money.of(BigDecimal.ZERO, "AUD");
        Money maintenanceFee = entity.getMaintenanceFee() != null ? 
            Money.of(entity.getMaintenanceFee(), "AUD") : null;
        
        // Map enums
        PropertyType propertyType = PropertyType.valueOf(entity.getPropertyType().name());
        RentalType rentalType = RentalType.valueOf(entity.getRentalType().name());
        
        // Use factory method to create Property
        Property property = Property.create(
            propertyId,
            ownerId,
            entity.getTitle(),
            entity.getDescription(),
            propertyType,
            rentalType,
            address,
            specs,
            rentPrice,
            depositAmount,
            maintenanceFee
        );
        
        // Set additional fields using reflection or getters/setters if available
        // For now, return the basic property
        return property;
    }

    @Override
    public void delete(Property property) {
        propertyJpaRepository.deleteById(property.getId().getValue());
    }

    @Override
    public List<Property> findByType(PropertyType type) {
        PropertyJpaEntity.PropertyTypeEnum typeEnum = 
                PropertyJpaEntity.PropertyTypeEnum.valueOf(type.name());
        List<PropertyJpaEntity> entities = propertyJpaRepository.findByPropertyType(typeEnum);
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findAvailableProperties() {
        List<PropertyJpaEntity> entities = propertyJpaRepository.findAvailableProperties();
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findNearByLocation(Double latitude, Double longitude, Double radiusKm) {
        List<PropertyJpaEntity> entities = propertyJpaRepository.findNearByLocation(latitude, longitude, radiusKm);
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findSimilarProperties(Property property, int limit) {
        // Simplified implementation - return empty list for now
        return List.of();
    }

    @Override
    public List<Property> findByBudgetRange(Money minBudget, Money maxBudget) {
        List<PropertyJpaEntity> entities = propertyJpaRepository.findByBudgetRange(
            minBudget.getAmount().doubleValue(), 
            maxBudget.getAmount().doubleValue());
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findBySpecsAndBudget(PropertySpecs minimumSpecs, Money maxBudget) {
        List<PropertyJpaEntity> entities = propertyJpaRepository.findBySpecsAndBudget(
            minimumSpecs.getBedrooms(),
            minimumSpecs.getBathrooms(),
            minimumSpecs.getFloorArea(),
            maxBudget.getAmount().doubleValue());
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(PropertyStatus status) {
        PropertyJpaEntity.PropertyStatusEnum statusEnum = 
                PropertyJpaEntity.PropertyStatusEnum.valueOf(status.name());
        return propertyJpaRepository.countByStatus(statusEnum);
    }

    @Override
    public Money calculateAverageRentInArea(Address area, Double radiusKm) {
        Double averageRent = propertyJpaRepository.calculateAverageRentInArea(
            area.getLatitude(), area.getLongitude(), radiusKm);
        return averageRent != null ? Money.of(BigDecimal.valueOf(averageRent), "AUD") : Money.of(BigDecimal.ZERO, "AUD");
    }

    @Override
    public boolean existsByOwnerIdAndStatus(UserId ownerId, PropertyStatus status) {
        PropertyJpaEntity.PropertyStatusEnum statusEnum = 
                PropertyJpaEntity.PropertyStatusEnum.valueOf(status.name());
        return propertyJpaRepository.existsByLandlordIdAndStatus(ownerId.getValue(), statusEnum);
    }

    @Override
    public List<Property> findAllById(Collection<Long> ids) {
        List<PropertyJpaEntity> entities = propertyJpaRepository.findAllById(ids);
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return propertyJpaRepository.count();
    }
}