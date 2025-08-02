package com.hanihome.hanihome_au_api.application.property.usecase;

import com.hanihome.hanihome_au_api.application.property.dto.CreatePropertyCommand;
import com.hanihome.hanihome_au_api.application.property.dto.PropertyResponseDto;
import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.property.valueobject.*;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.enums.RentalType;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;

public class CreatePropertyUseCase {
    private final PropertyRepository propertyRepository;

    public CreatePropertyUseCase(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public PropertyResponseDto execute(CreatePropertyCommand command) {
        PropertyId propertyId = PropertyId.of(generateNewPropertyId());
        UserId ownerId = UserId.of(command.getOwnerId());
        
        PropertyType type = PropertyType.valueOf(command.getPropertyType().toUpperCase());
        RentalType rentalType = RentalType.valueOf(command.getRentalType().toUpperCase());
        
        Address address = new Address(
            command.getStreet(),
            command.getCity(),
            command.getState(),
            command.getCountry(),
            command.getPostalCode(),
            command.getLatitude(),
            command.getLongitude()
        );
        
        PropertySpecs specs = new PropertySpecs(
            command.getBedrooms(),
            command.getBathrooms(),
            command.getFloorArea(),
            command.getFloor(),
            command.getTotalFloors(),
            command.isHasParking(),
            command.isHasPet(),
            command.isHasElevator()
        );

        Money rentPrice = Money.of(command.getRentPrice(), command.getCurrency());
        Money depositAmount = Money.of(command.getDepositAmount(), command.getCurrency());

        Property property = Property.create(
            propertyId,
            ownerId,
            command.getTitle(),
            command.getDescription(),
            type,
            rentalType,
            address,
            specs,
            rentPrice,
            depositAmount
        );

        Property savedProperty = propertyRepository.save(property);

        return mapToResponseDto(savedProperty);
    }

    private Long generateNewPropertyId() {
        return System.currentTimeMillis();
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
}