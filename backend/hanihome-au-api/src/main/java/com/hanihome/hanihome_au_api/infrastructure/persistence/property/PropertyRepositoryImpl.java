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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import org.springframework.data.domain.PageRequest;

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
        List<PropertyJpaEntity> entities = propertyJpaRepository.findByOwnerId(ownerId.getValue());
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
    public void delete(Property property) {
        propertyJpaRepository.deleteById(property.getId().getValue());
    }

    @Override
    public List<Property> findSimilarProperties(Property property, int limit) {
        BigDecimal priceThreshold = property.getRentPrice().getAmount().multiply(BigDecimal.valueOf(0.2)); // 20% price variance
        
        PropertyJpaEntity.PropertyTypeEnum typeEnum = 
                PropertyJpaEntity.PropertyTypeEnum.valueOf(property.getType().name());
        
        List<PropertyJpaEntity> entities = propertyJpaRepository.findSimilarProperties(
                typeEnum,
                property.getSpecs().getBedrooms(),
                property.getSpecs().getBathrooms(),
                property.getRentPrice().getAmount().doubleValue(),
                priceThreshold.doubleValue(),
                property.getId().getValue(),
                PageRequest.of(0, limit)
        );
        
        return entities.stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Property> findByBudgetRange(Money minBudget, Money maxBudget) {
        List<PropertyJpaEntity> entities = propertyJpaRepository.findByBudgetRange(
                minBudget.getAmount().doubleValue(), 
                maxBudget.getAmount().doubleValue()
        );
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
                maxBudget.getAmount().doubleValue()
        );
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
                area.getLatitude(), 
                area.getLongitude(), 
                radiusKm
        );
        
        if (averageRent == null) {
            return Money.of(BigDecimal.ZERO, "AUD");
        }
        
        return Money.of(BigDecimal.valueOf(averageRent), "AUD");
    }

    @Override
    public boolean existsByOwnerIdAndStatus(UserId ownerId, PropertyStatus status) {
        PropertyJpaEntity.PropertyStatusEnum statusEnum = 
                PropertyJpaEntity.PropertyStatusEnum.valueOf(status.name());
        return propertyJpaRepository.existsByOwnerIdAndStatus(ownerId.getValue(), statusEnum);
    }

    private PropertyJpaEntity mapToEntity(Property property) {
        return new PropertyJpaEntity(
            property.getId().getValue(),
            property.getOwnerId().getValue(),
            property.getTitle(),
            property.getDescription(),
            PropertyJpaEntity.PropertyTypeEnum.valueOf(property.getType().name()),
            PropertyJpaEntity.RentalTypeEnum.valueOf(property.getRentalType().name()),
            PropertyJpaEntity.PropertyStatusEnum.valueOf(property.getStatus().name()),
            property.getAddress().getStreet(),
            property.getAddress().getCity(),
            property.getAddress().getState(),
            property.getAddress().getCountry(),
            property.getAddress().getPostalCode(),
            property.getAddress().getLatitude(),
            property.getAddress().getLongitude(),
            property.getSpecs().getBedrooms(),
            property.getSpecs().getBathrooms(),
            property.getSpecs().getFloorArea(),
            property.getSpecs().getFloor(),
            property.getSpecs().getTotalFloors(),
            property.getSpecs().isHasParking(),
            property.getSpecs().isHasPet(),
            property.getSpecs().isHasElevator(),
            property.getRentPrice().getAmount(),
            property.getDepositAmount().getAmount(),
            property.getRentPrice().getCurrency(),
            property.getAvailableFrom(),
            property.getCreatedAt(),
            property.getUpdatedAt()
        );
    }

    private Property mapToDomain(PropertyJpaEntity entity) {
        PropertyId propertyId = PropertyId.of(entity.getId());
        UserId ownerId = UserId.of(entity.getOwnerId());
        PropertyType type = PropertyType.valueOf(entity.getPropertyType().name());
        RentalType rentalType = RentalType.valueOf(entity.getRentalType().name());
        
        Address address = new Address(
            entity.getStreet(),
            entity.getCity(),
            entity.getState(),
            entity.getCountry(),
            entity.getPostalCode(),
            entity.getLatitude(),
            entity.getLongitude()
        );
        
        PropertySpecs specs = new PropertySpecs(
            entity.getBedrooms(),
            entity.getBathrooms(),
            entity.getFloorArea(),
            entity.getFloor(),
            entity.getTotalFloors(),
            entity.isHasParking(),
            entity.isHasPet(),
            entity.isHasElevator()
        );
        
        Money rentPrice = Money.of(entity.getRentPrice(), entity.getCurrency());
        Money depositAmount = Money.of(entity.getDepositAmount(), entity.getCurrency());
        
        Property property = Property.create(
            propertyId,
            ownerId,
            entity.getTitle(),
            entity.getDescription(),
            type,
            rentalType,
            address,
            specs,
            rentPrice,
            depositAmount
        );
        
        // Set status if different from default
        PropertyStatus currentStatus = PropertyStatus.valueOf(entity.getStatus().name());
        if (currentStatus != PropertyStatus.DRAFT) {
            property.changeStatus(currentStatus);
        }
        
        return property;
    }
}