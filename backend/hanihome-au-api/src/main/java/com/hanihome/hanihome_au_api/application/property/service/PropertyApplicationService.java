package com.hanihome.hanihome_au_api.application.property.service;

import com.hanihome.hanihome_au_api.application.property.dto.CreatePropertyCommand;
import com.hanihome.hanihome_au_api.application.property.dto.PropertyResponseDto;
import com.hanihome.hanihome_au_api.application.property.dto.UpdatePropertyCommand;
import com.hanihome.hanihome_au_api.application.property.usecase.CreatePropertyUseCase;
import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.property.service.PropertyDomainService;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.shared.event.DomainEventPublisher;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service for Property operations
 * Orchestrates domain objects and handles transaction boundaries
 */
@Service
@Transactional
public class PropertyApplicationService {
    private final PropertyRepository propertyRepository;
    private final PropertyDomainService propertyDomainService;
    private final CreatePropertyUseCase createPropertyUseCase;
    private final DomainEventPublisher domainEventPublisher;
    private final SearchCacheEvictionService cacheEvictionService;

    public PropertyApplicationService(PropertyRepository propertyRepository,
                                    PropertyDomainService propertyDomainService,
                                    CreatePropertyUseCase createPropertyUseCase,
                                    DomainEventPublisher domainEventPublisher,
                                    SearchCacheEvictionService cacheEvictionService) {
        this.propertyRepository = propertyRepository;
        this.propertyDomainService = propertyDomainService;
        this.createPropertyUseCase = createPropertyUseCase;
        this.domainEventPublisher = domainEventPublisher;
        this.cacheEvictionService = cacheEvictionService;
    }

    public PropertyResponseDto createProperty(CreatePropertyCommand command) {
        return createPropertyUseCase.execute(command);
    }

    public PropertyResponseDto getProperty(Long propertyId) {
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        return mapToResponseDto(property);
    }

    public List<PropertyResponseDto> getPropertiesByOwner(Long ownerId) {
        List<Property> properties = propertyRepository.findByOwnerId(UserId.of(ownerId));
        
        return properties.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<PropertyResponseDto> getAvailableProperties() {
        List<Property> properties = propertyRepository.findAvailableProperties();
        
        return properties.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates existing property with domain validation
     */
    public PropertyResponseDto updateProperty(Long propertyId, UpdatePropertyCommand command, Long ownerId) {
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        if (!property.canBeAccessedBy(UserId.of(ownerId))) {
            throw new IllegalArgumentException("User does not have permission to manage this property");
        }

        // Update property using domain methods
        if (command.getTitle() != null || command.getDescription() != null || command.getSpecs() != null) {
            property.updateDetails(command.getTitle(), command.getDescription(), command.getSpecs());
        }

        if (command.getRentPrice() != null || command.getDepositAmount() != null) {
            property.updatePricing(command.getRentPrice(), command.getDepositAmount());
        }

        Property savedProperty = propertyRepository.save(property);
        publishDomainEvents(savedProperty);
        
        return mapToResponseDto(savedProperty);
    }

    /**
     * Activates property with business rule validation
     */
    public void activateProperty(Long propertyId, Long ownerId) {
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        if (!propertyDomainService.canUserManageProperty(UserId.of(ownerId), property)) {
            throw new IllegalArgumentException("User does not have permission to manage this property");
        }

        // Use domain method which includes validation
        property.activate();
        
        Property savedProperty = propertyRepository.save(property);
        publishDomainEvents(savedProperty);
    }

    /**
     * Deactivates property
     */
    public void deactivateProperty(Long propertyId, Long ownerId) {
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        if (!propertyDomainService.canUserManageProperty(UserId.of(ownerId), property)) {
            throw new IllegalArgumentException("User does not have permission to manage this property");
        }

        property.deactivate();
        Property savedProperty = propertyRepository.save(property);
        publishDomainEvents(savedProperty);
    }

    /**
     * Approves property (admin/agent operation)
     */
    public void approveProperty(Long propertyId, Long approvedById) {
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        property.approve(UserId.of(approvedById));
        
        Property savedProperty = propertyRepository.save(property);
        publishDomainEvents(savedProperty);
    }

    /**
     * Rejects property (admin/agent operation)
     */
    public void rejectProperty(Long propertyId, String reason) {
        Property property = propertyRepository.findById(PropertyId.of(propertyId))
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        property.reject(reason);
        
        Property savedProperty = propertyRepository.save(property);
        publishDomainEvents(savedProperty);
    }

    private PropertyResponseDto mapToResponseDto(Property property) {
        return new PropertyResponseDto(
            property.getId().getValue(),
            property.getOwnerId().getValue(),
            property.getTitle(),
            property.getDescription(),
            property.getType().name(),
            property.getRentalType().name(),
            property.getStatus().name(),
            property.getAddress().getFullAddress(),
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

    /**
     * Convert JPA entity to response DTO (for search service)
     */
    public PropertyResponseDto convertToDto(com.hanihome.hanihome_au_api.infrastructure.persistence.property.PropertyJpaEntity entity) {
        return new PropertyResponseDto(
            entity.getId(),
            entity.getLandlordId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getPropertyType().name(),
            entity.getRentalType().name(),
            entity.getStatus().name(),
            entity.getAddress(),
            entity.getLatitude() != null ? entity.getLatitude().doubleValue() : null,
            entity.getLongitude() != null ? entity.getLongitude().doubleValue() : null,
            entity.getRooms() != null ? entity.getRooms() : 0,
            entity.getBathrooms() != null ? entity.getBathrooms() : 0,
            entity.getArea() != null ? entity.getArea().doubleValue() : null,
            entity.getFloor(),
            entity.getTotalFloors(),
            Boolean.TRUE.equals(entity.getParkingAvailable()),
            Boolean.TRUE.equals(entity.getPetAllowed()),
            false, // hasElevator - not in JPA entity, using default
            entity.getMonthlyRent(),
            entity.getDeposit(),
            "AUD", // Default currency - should be configurable
            entity.getAvailableDate() != null ? entity.getAvailableDate().atStartOfDay() : null,
            entity.getCreatedDate(),
            entity.getModifiedDate()
        );
    }

    /**
     * Publishes domain events from aggregate
     */
    private void publishDomainEvents(Property property) {
        property.getDomainEvents().forEach(domainEventPublisher::publish);
        property.clearDomainEvents();
    }
}