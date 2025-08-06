package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.enums.*;
import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertySpecs;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Property 엔티티용 TestDataFactory
 */
public class PropertyTestDataFactory extends BaseTestDataFactory<Property> {

    @Override
    public Property createDefault() {
        return Property.builder()
            .id(PropertyId.of(faker.internet().uuid()))
            .ownerId(UserId.of(faker.internet().uuid()))
            .title("Default Test Property")
            .description("A standard test property for integration testing")
            .type(PropertyType.APARTMENT)
            .rentalType(RentalType.LONG_TERM)
            .status(PropertyStatus.ACTIVE)
            .address(createDefaultAddress())
            .specs(createDefaultSpecs())
            .rentPrice(Money.of(new BigDecimal("500.00")))
            .depositAmount(Money.of(new BigDecimal("2000.00")))
            .maintenanceFee(Money.of(new BigDecimal("50.00")))
            .availableFrom(LocalDateTime.now().plusDays(7))
            .options(Arrays.asList("에어컨", "냉장고", "세탁기"))
            .imageUrls(Arrays.asList("https://example.com/image1.jpg", "https://example.com/image2.jpg"))
            .parkingAvailable(true)
            .petAllowed(false)
            .furnished(true)
            .shortTermAvailable(false)
            .build();
    }

    @Override
    public Property createMinimal() {
        return Property.builder()
            .id(PropertyId.of(faker.internet().uuid()))
            .ownerId(UserId.of(faker.internet().uuid()))
            .title("Minimal Property")
            .type(PropertyType.STUDIO)
            .rentalType(RentalType.LONG_TERM)
            .status(PropertyStatus.ACTIVE)
            .address(createMinimalAddress())
            .specs(createMinimalSpecs())
            .rentPrice(Money.of(new BigDecimal("300.00")))
            .build();
    }

    @Override
    public Property createMaximal() {
        return Property.builder()
            .id(PropertyId.of(faker.internet().uuid()))
            .ownerId(UserId.of(faker.internet().uuid()))
            .title("Luxury Penthouse with Harbor Views")
            .description("Stunning luxury penthouse featuring panoramic harbor views, premium finishes, " +
                        "state-of-the-art appliances, concierge service, and exclusive amenities")
            .type(PropertyType.PENTHOUSE)
            .rentalType(RentalType.LONG_TERM)
            .status(PropertyStatus.ACTIVE)
            .address(createLuxuryAddress())
            .specs(createMaximalSpecs())
            .rentPrice(Money.of(new BigDecimal("3000.00")))
            .depositAmount(Money.of(new BigDecimal("12000.00")))
            .maintenanceFee(Money.of(new BigDecimal("500.00")))
            .availableFrom(LocalDateTime.now().plusDays(30))
            .options(Arrays.asList("에어컨", "냉장고", "세탁기", "식기세척기", "오븐", "발코니", "체육관", "수영장", "컨시어지"))
            .imageUrls(IntStream.range(1, 21).mapToObj(i -> "https://example.com/luxury/image" + i + ".jpg").toList())
            .parkingAvailable(true)
            .petAllowed(true)
            .furnished(true)
            .shortTermAvailable(true)
            .adminNotes("Premium property requiring credit check and references")
            .build();
    }

    @Override
    public Property createInvalid() {
        return Property.builder()
            .id(null) // Invalid: null ID
            .ownerId(null) // Invalid: null owner
            .title("") // Invalid: empty title
            .type(null) // Invalid: null type
            .rentPrice(Money.of(new BigDecimal("-100.00"))) // Invalid: negative price
            .specs(PropertySpecs.builder()
                .bedrooms(-1) // Invalid: negative bedrooms
                .bathrooms(-1) // Invalid: negative bathrooms
                .totalAreaSqm(new BigDecimal("-50")) // Invalid: negative area
                .build())
            .build();
    }

    @Override
    public Property createRandom() {
        PropertyType[] types = PropertyType.values();
        RentalType[] rentalTypes = RentalType.values();
        PropertyStatus[] statuses = {PropertyStatus.ACTIVE, PropertyStatus.PENDING, PropertyStatus.INACTIVE};
        
        BigDecimal rentPrice = generateRealisticRentPrice();
        
        return Property.builder()
            .id(PropertyId.of(faker.internet().uuid()))
            .ownerId(UserId.of(faker.internet().uuid()))
            .title(faker.lorem().sentence(3, 6))
            .description(faker.lorem().paragraph(2))
            .type(types[faker.random().nextInt(types.length)])
            .rentalType(rentalTypes[faker.random().nextInt(rentalTypes.length)])
            .status(statuses[faker.random().nextInt(statuses.length)])
            .address(createRandomAddress())
            .specs(createRandomSpecs())
            .rentPrice(Money.of(rentPrice))
            .depositAmount(Money.of(calculateBondAmount(rentPrice)))
            .maintenanceFee(Money.of(new BigDecimal(faker.number().numberBetween(0, 200))))
            .availableFrom(LocalDateTime.now().plusDays(faker.number().numberBetween(1, 90)))
            .parkingAvailable(faker.bool().bool())
            .petAllowed(faker.bool().bool())
            .furnished(faker.bool().bool())
            .shortTermAvailable(faker.bool().bool())
            .build();
    }

    @Override
    public List<Property> createBulk(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createRandom())
            .toList();
    }

    // Helper methods for creating related objects
    private Address createDefaultAddress() {
        return Address.builder()
            .streetAddress("123 Test Street")
            .suburb("Sydney")
            .state("NSW")
            .country("Australia")
            .postcode("2000")
            .latitude(-33.8688)
            .longitude(151.2093)
            .build();
    }

    private Address createMinimalAddress() {
        return Address.builder()
            .streetAddress("1 Main St")
            .suburb("Melbourne")
            .state("VIC")
            .country("Australia")
            .postcode("3000")
            .build();
    }

    private Address createLuxuryAddress() {
        return Address.builder()
            .streetAddress("Penthouse, 200 Harbour View Drive")
            .suburb("Circular Quay")
            .state("NSW")
            .country("Australia")
            .postcode("2000")
            .latitude(-33.8568)
            .longitude(151.2153)
            .build();
    }

    private Address createRandomAddress() {
        return Address.builder()
            .streetAddress(faker.address().streetAddress())
            .suburb(faker.address().cityName())
            .state(getRandomAustralianState())
            .country("Australia")
            .postcode(faker.address().zipCode())
            .latitude(faker.address().latitude())
            .longitude(faker.address().longitude())
            .build();
    }

    private PropertySpecs createDefaultSpecs() {
        return PropertySpecs.builder()
            .bedrooms(2)
            .bathrooms(1)
            .parkingSpaces(1)
            .totalAreaSqm(new BigDecimal("80.0"))
            .build();
    }

    private PropertySpecs createMinimalSpecs() {
        return PropertySpecs.builder()
            .bedrooms(0) // Studio
            .bathrooms(1)
            .parkingSpaces(0)
            .totalAreaSqm(new BigDecimal("35.0"))
            .build();
    }

    private PropertySpecs createMaximalSpecs() {
        return PropertySpecs.builder()
            .bedrooms(4)
            .bathrooms(3)
            .parkingSpaces(3)
            .totalAreaSqm(new BigDecimal("250.0"))
            .build();
    }

    private PropertySpecs createRandomSpecs() {
        return PropertySpecs.builder()
            .bedrooms(faker.number().numberBetween(0, 5))
            .bathrooms(faker.number().numberBetween(1, 4))
            .parkingSpaces(faker.number().numberBetween(0, 3))
            .totalAreaSqm(new BigDecimal(faker.number().numberBetween(30, 300)))
            .build();
    }

    private String getRandomAustralianState() {
        String[] states = {"NSW", "VIC", "QLD", "WA", "SA", "TAS", "ACT", "NT"};
        return states[faker.random().nextInt(states.length)];
    }

    // Specific use-case factory methods
    public Property createForTesting(PropertyType type, BigDecimal rentPrice) {
        return createDefault().toBuilder()
            .type(type)
            .rentPrice(Money.of(rentPrice))
            .build();
    }

    public Property createUnavailable() {
        return createDefault().toBuilder()
            .status(PropertyStatus.RENTED)
            .build();
    }

    public Property createPending() {
        return createDefault().toBuilder()
            .status(PropertyStatus.PENDING)
            .build();
    }

    public Property createExpensive() {
        BigDecimal expensiveRent = new BigDecimal("2500.00");
        return createDefault().toBuilder()
            .rentPrice(Money.of(expensiveRent))
            .depositAmount(Money.of(calculateBondAmount(expensiveRent)))
            .type(PropertyType.HOUSE)
            .specs(createMaximalSpecs())
            .build();
    }

    public Property createAffordable() {
        BigDecimal affordableRent = new BigDecimal("350.00");
        return createDefault().toBuilder()
            .rentPrice(Money.of(affordableRent))
            .depositAmount(Money.of(calculateBondAmount(affordableRent)))
            .type(PropertyType.STUDIO)
            .specs(createMinimalSpecs())
            .build();
    }
}