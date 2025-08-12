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
import java.util.List;
import java.util.Arrays;

/**
 * Property 엔티티를 위한 테스트 데이터 빌더
 * 플루언트 인터페이스를 통해 직관적인 테스트 데이터 생성을 지원
 */
public class PropertyTestDataBuilder {
    
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
    private Money maintenanceFee;
    private LocalDateTime availableFrom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserId approvedBy;
    private LocalDateTime approvedAt;
    private Long agentId;
    private List<String> options;
    private List<String> imageUrls;
    private Boolean parkingAvailable;
    private Boolean petAllowed;
    private Boolean furnished;
    private Boolean shortTermAvailable;
    private String adminNotes;
    
    public PropertyTestDataBuilder() {
        // 기본값 설정
        this.id = PropertyId.of(BaseTestDataFactory.faker.internet().uuid());
        this.ownerId = UserId.of(BaseTestDataFactory.faker.internet().uuid());
        this.title = "Test Property";
        this.description = "A test property for unit testing";
        this.type = PropertyType.APARTMENT;
        this.rentalType = RentalType.LONG_TERM;
        this.status = PropertyStatus.ACTIVE;
        this.address = createDefaultAddress();
        this.specs = createDefaultSpecs();
        this.rentPrice = Money.of(new BigDecimal("500.00"));
        this.depositAmount = Money.of(new BigDecimal("2000.00"));
        this.maintenanceFee = Money.of(new BigDecimal("50.00"));
        this.availableFrom = LocalDateTime.now().plusDays(7);
        this.createdAt = LocalDateTime.now();
        this.options = Arrays.asList("에어컨", "냉장고");
        this.imageUrls = Arrays.asList("https://example.com/image1.jpg");
        this.parkingAvailable = true;
        this.petAllowed = false;
        this.furnished = true;
        this.shortTermAvailable = false;
    }
    
    public PropertyTestDataBuilder withId(String id) {
        this.id = PropertyId.of(id);
        return this;
    }
    
    public PropertyTestDataBuilder withRandomId() {
        this.id = PropertyId.of(BaseTestDataFactory.faker.internet().uuid());
        return this;
    }
    
    public PropertyTestDataBuilder withOwnerId(String ownerId) {
        this.ownerId = UserId.of(ownerId);
        return this;
    }
    
    public PropertyTestDataBuilder withRandomOwnerId() {
        this.ownerId = UserId.of(BaseTestDataFactory.faker.internet().uuid());
        return this;
    }
    
    public PropertyTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomTitle() {
        this.title = BaseTestDataFactory.faker.lorem().sentence(3, 6);
        return this;
    }
    
    public PropertyTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomDescription() {
        this.description = BaseTestDataFactory.faker.lorem().paragraph(2);
        return this;
    }
    
    public PropertyTestDataBuilder withType(PropertyType type) {
        this.type = type;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomType() {
        PropertyType[] types = PropertyType.values();
        this.type = types[BaseTestDataFactory.faker.random().nextInt(types.length)];
        return this;
    }
    
    public PropertyTestDataBuilder withRentalType(RentalType rentalType) {
        this.rentalType = rentalType;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomRentalType() {
        RentalType[] types = RentalType.values();
        this.rentalType = types[BaseTestDataFactory.faker.random().nextInt(types.length)];
        return this;
    }
    
    public PropertyTestDataBuilder withStatus(PropertyStatus status) {
        this.status = status;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomStatus() {
        PropertyStatus[] statuses = {PropertyStatus.ACTIVE, PropertyStatus.PENDING, PropertyStatus.INACTIVE};
        this.status = statuses[BaseTestDataFactory.faker.random().nextInt(statuses.length)];
        return this;
    }
    
    public PropertyTestDataBuilder withAddress(Address address) {
        this.address = address;
        return this;
    }
    
    public PropertyTestDataBuilder withAddress(String street, String suburb, String state, String postcode) {
        this.address = Address.builder()
            .streetAddress(street)
            .suburb(suburb)
            .state(state)
            .country("Australia")
            .postcode(postcode)
            .build();
        return this;
    }
    
    public PropertyTestDataBuilder withRandomAddress() {
        this.address = Address.builder()
            .streetAddress(BaseTestDataFactory.faker.address().streetAddress())
            .suburb(BaseTestDataFactory.faker.address().cityName())
            .state(getRandomAustralianState())
            .country("Australia")
            .postcode(BaseTestDataFactory.faker.address().zipCode())
            .latitude(BaseTestDataFactory.faker.address().latitude())
            .longitude(BaseTestDataFactory.faker.address().longitude())
            .build();
        return this;
    }
    
    public PropertyTestDataBuilder withSpecs(PropertySpecs specs) {
        this.specs = specs;
        return this;
    }
    
    public PropertyTestDataBuilder withSpecs(int bedrooms, int bathrooms, int parkingSpaces, BigDecimal totalArea) {
        this.specs = PropertySpecs.builder()
            .bedrooms(bedrooms)
            .bathrooms(bathrooms)
            .parkingSpaces(parkingSpaces)
            .totalAreaSqm(totalArea)
            .build();
        return this;
    }
    
    public PropertyTestDataBuilder withRandomSpecs() {
        this.specs = PropertySpecs.builder()
            .bedrooms(BaseTestDataFactory.faker.number().numberBetween(0, 5))
            .bathrooms(BaseTestDataFactory.faker.number().numberBetween(1, 4))
            .parkingSpaces(BaseTestDataFactory.faker.number().numberBetween(0, 3))
            .totalAreaSqm(new BigDecimal(BaseTestDataFactory.faker.number().numberBetween(30, 300)))
            .build();
        return this;
    }
    
    public PropertyTestDataBuilder withRentPrice(BigDecimal price) {
        this.rentPrice = Money.of(price);
        return this;
    }
    
    public PropertyTestDataBuilder withRentPrice(String price) {
        this.rentPrice = Money.of(new BigDecimal(price));
        return this;
    }
    
    public PropertyTestDataBuilder withRandomRentPrice() {
        BigDecimal price = new BigDecimal(300 + BaseTestDataFactory.faker.random().nextInt(2700));
        this.rentPrice = Money.of(price);
        return this;
    }
    
    public PropertyTestDataBuilder withDepositAmount(BigDecimal deposit) {
        this.depositAmount = Money.of(deposit);
        return this;
    }
    
    public PropertyTestDataBuilder withAutoCalculatedDeposit() {
        if (this.rentPrice != null) {
            int weeks = 4 + BaseTestDataFactory.faker.random().nextInt(5); // 4-8주
            this.depositAmount = Money.of(this.rentPrice.getAmount().multiply(new BigDecimal(weeks)));
        }
        return this;
    }
    
    public PropertyTestDataBuilder withMaintenanceFee(BigDecimal fee) {
        this.maintenanceFee = Money.of(fee);
        return this;
    }
    
    public PropertyTestDataBuilder withRandomMaintenanceFee() {
        this.maintenanceFee = Money.of(new BigDecimal(BaseTestDataFactory.faker.number().numberBetween(0, 200)));
        return this;
    }
    
    public PropertyTestDataBuilder withAvailableFrom(LocalDateTime availableFrom) {
        this.availableFrom = availableFrom;
        return this;
    }
    
    public PropertyTestDataBuilder withAvailableFromDaysFromNow(int days) {
        this.availableFrom = LocalDateTime.now().plusDays(days);
        return this;
    }
    
    public PropertyTestDataBuilder withRandomAvailableFrom() {
        this.availableFrom = LocalDateTime.now().plusDays(BaseTestDataFactory.faker.number().numberBetween(1, 90));
        return this;
    }
    
    public PropertyTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public PropertyTestDataBuilder withCreatedAtDaysAgo(int days) {
        this.createdAt = LocalDateTime.now().minusDays(days);
        return this;
    }
    
    public PropertyTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public PropertyTestDataBuilder withApprovedBy(String approverId) {
        this.approvedBy = UserId.of(approverId);
        this.approvedAt = LocalDateTime.now();
        return this;
    }
    
    public PropertyTestDataBuilder withAgentId(Long agentId) {
        this.agentId = agentId;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomAgentId() {
        this.agentId = BaseTestDataFactory.faker.number().numberBetween(1L, 1000L);
        return this;
    }
    
    public PropertyTestDataBuilder withOptions(List<String> options) {
        this.options = options;
        return this;
    }
    
    public PropertyTestDataBuilder withOptions(String... options) {
        this.options = Arrays.asList(options);
        return this;
    }
    
    public PropertyTestDataBuilder withBasicOptions() {
        this.options = Arrays.asList("에어컨", "냉장고", "세탁기");
        return this;
    }
    
    public PropertyTestDataBuilder withLuxuryOptions() {
        this.options = Arrays.asList("에어컨", "냉장고", "세탁기", "식기세척기", "오븐", "발코니", "체육관", "수영장", "컨시어지");
        return this;
    }
    
    public PropertyTestDataBuilder withImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        return this;
    }
    
    public PropertyTestDataBuilder withImageUrls(String... imageUrls) {
        this.imageUrls = Arrays.asList(imageUrls);
        return this;
    }
    
    public PropertyTestDataBuilder withRandomImageUrls(int count) {
        this.imageUrls = java.util.stream.IntStream.range(1, count + 1)
            .mapToObj(i -> "https://example.com/property/image" + i + ".jpg")
            .toList();
        return this;
    }
    
    public PropertyTestDataBuilder withParkingAvailable(boolean parkingAvailable) {
        this.parkingAvailable = parkingAvailable;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomParkingAvailable() {
        this.parkingAvailable = BaseTestDataFactory.faker.bool().bool();
        return this;
    }
    
    public PropertyTestDataBuilder withPetAllowed(boolean petAllowed) {
        this.petAllowed = petAllowed;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomPetAllowed() {
        this.petAllowed = BaseTestDataFactory.faker.bool().bool();
        return this;
    }
    
    public PropertyTestDataBuilder withFurnished(boolean furnished) {
        this.furnished = furnished;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomFurnished() {
        this.furnished = BaseTestDataFactory.faker.bool().bool();
        return this;
    }
    
    public PropertyTestDataBuilder withShortTermAvailable(boolean shortTermAvailable) {
        this.shortTermAvailable = shortTermAvailable;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomShortTermAvailable() {
        this.shortTermAvailable = BaseTestDataFactory.faker.bool().bool();
        return this;
    }
    
    public PropertyTestDataBuilder withAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
        return this;
    }
    
    public PropertyTestDataBuilder withRandomAdminNotes() {
        this.adminNotes = BaseTestDataFactory.faker.lorem().sentence();
        return this;
    }
    
    // Convenience methods for common scenarios
    public PropertyTestDataBuilder asStudio() {
        return withType(PropertyType.STUDIO)
            .withSpecs(0, 1, 0, new BigDecimal("35"))
            .withRentPrice("350");
    }
    
    public PropertyTestDataBuilder asApartment() {
        return withType(PropertyType.APARTMENT)
            .withSpecs(2, 1, 1, new BigDecimal("80"))
            .withRentPrice("500");
    }
    
    public PropertyTestDataBuilder asHouse() {
        return withType(PropertyType.HOUSE)
            .withSpecs(3, 2, 2, new BigDecimal("150"))
            .withRentPrice("800");
    }
    
    public PropertyTestDataBuilder asPenthouse() {
        return withType(PropertyType.PENTHOUSE)
            .withSpecs(4, 3, 3, new BigDecimal("250"))
            .withRentPrice("2000")
            .withLuxuryOptions();
    }
    
    public PropertyTestDataBuilder asAvailable() {
        return withStatus(PropertyStatus.ACTIVE)
            .withAvailableFromDaysFromNow(7);
    }
    
    public PropertyTestDataBuilder asUnavailable() {
        return withStatus(PropertyStatus.RENTED);
    }
    
    public PropertyTestDataBuilder asPending() {
        return withStatus(PropertyStatus.PENDING);
    }
    
    public PropertyTestDataBuilder asLuxury() {
        return asPenthouse()
            .withLuxuryOptions()
            .withRandomImageUrls(15)
            .withParkingAvailable(true)
            .withPetAllowed(true)
            .withFurnished(true)
            .withShortTermAvailable(true);
    }
    
    public PropertyTestDataBuilder asAffordable() {
        return asStudio()
            .withBasicOptions()
            .withParkingAvailable(false)
            .withPetAllowed(false)
            .withFurnished(false);
    }
    
    public PropertyTestDataBuilder allRandom() {
        return withRandomId()
            .withRandomOwnerId()
            .withRandomTitle()
            .withRandomDescription()
            .withRandomType()
            .withRandomRentalType()
            .withRandomStatus()
            .withRandomAddress()
            .withRandomSpecs()
            .withRandomRentPrice()
            .withAutoCalculatedDeposit()
            .withRandomMaintenanceFee()
            .withRandomAvailableFrom()
            .withRandomAgentId()
            .withRandomImageUrls(BaseTestDataFactory.faker.number().numberBetween(1, 10))
            .withRandomParkingAvailable()
            .withRandomPetAllowed()
            .withRandomFurnished()
            .withRandomShortTermAvailable();
    }
    
    public Property build() {
        return Property.builder()
            .id(id)
            .ownerId(ownerId)
            .title(title)
            .description(description)
            .type(type)
            .rentalType(rentalType)
            .status(status)
            .address(address)
            .specs(specs)
            .rentPrice(rentPrice)
            .depositAmount(depositAmount)
            .maintenanceFee(maintenanceFee)
            .availableFrom(availableFrom)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .approvedBy(approvedBy)
            .approvedAt(approvedAt)
            .agentId(agentId)
            .options(options)
            .imageUrls(imageUrls)
            .parkingAvailable(parkingAvailable)
            .petAllowed(petAllowed)
            .furnished(furnished)
            .shortTermAvailable(shortTermAvailable)
            .adminNotes(adminNotes)
            .build();
    }
    
    // Helper methods
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
    
    private PropertySpecs createDefaultSpecs() {
        return PropertySpecs.builder()
            .bedrooms(2)
            .bathrooms(1)
            .parkingSpaces(1)
            .totalAreaSqm(new BigDecimal("80.0"))
            .build();
    }
    
    private String getRandomAustralianState() {
        String[] states = {"NSW", "VIC", "QLD", "WA", "SA", "TAS", "ACT", "NT"};
        return states[BaseTestDataFactory.faker.random().nextInt(states.length)];
    }
}