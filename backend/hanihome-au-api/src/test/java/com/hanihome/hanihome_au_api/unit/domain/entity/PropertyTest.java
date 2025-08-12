package com.hanihome.hanihome_au_api.unit.domain.entity;

import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertySpecs;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.enums.*;
import com.hanihome.hanihome_au_api.domain.property.event.*;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.testutil.TestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Property Entity Tests")
class PropertyTest {

    private PropertyId propertyId;
    private UserId ownerId;
    private String validTitle;
    private String validDescription;
    private PropertyType propertyType;
    private RentalType rentalType;
    private Address address;
    private PropertySpecs specs;
    private Money rentPrice;
    private Money depositAmount;
    private Money maintenanceFee;

    @BeforeEach
    void setUp() {
        propertyId = new PropertyId(UUID.randomUUID().toString());
        ownerId = new UserId(UUID.randomUUID().toString());
        validTitle = "Beautiful 2BR Apartment";
        validDescription = "Spacious apartment with great views";
        propertyType = PropertyType.APARTMENT;
        rentalType = RentalType.LONG_TERM;
        address = TestDataFactory.createDefaultAddress();
        specs = new PropertySpecs(2, 1, 1);
        rentPrice = Money.of(new BigDecimal("500.00"));
        depositAmount = Money.of(new BigDecimal("2000.00"));
        maintenanceFee = Money.of(new BigDecimal("50.00"));
    }

    @Nested
    @DisplayName("Property Creation")
    class PropertyCreationTests {

        @Test
        @DisplayName("Should create property with valid data")
        void should_CreateProperty_When_ValidDataProvided() {
            // Act
            Property property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs, 
                rentPrice, depositAmount, maintenanceFee
            );

            // Assert
            assertThat(property).isNotNull();
            assertThat(property.getId()).isEqualTo(propertyId);
            assertThat(property.getOwnerId()).isEqualTo(ownerId);
            assertThat(property.getTitle()).isEqualTo(validTitle);
            assertThat(property.getDescription()).isEqualTo(validDescription);
            assertThat(property.getType()).isEqualTo(propertyType);
            assertThat(property.getRentalType()).isEqualTo(rentalType);
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.PENDING_APPROVAL);
            assertThat(property.getAddress()).isEqualTo(address);
            assertThat(property.getSpecs()).isEqualTo(specs);
            assertThat(property.getRentPrice()).isEqualTo(rentPrice);
            assertThat(property.getDepositAmount()).isEqualTo(depositAmount);
            assertThat(property.getMaintenanceFee()).isEqualTo(maintenanceFee);
            assertThat(property.getCreatedAt()).isNotNull();
            assertThat(property.getUpdatedAt()).isNotNull();
            assertThat(property.getVersion()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should create property without maintenance fee")
        void should_CreateProperty_When_NoMaintenanceFeeProvided() {
            // Act
            Property property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount
            );

            // Assert
            assertThat(property.getMaintenanceFee()).isNull();
            assertThat(property.getTotalMonthlyCost()).isEqualTo(rentPrice);
        }

        @Test
        @DisplayName("Should raise PropertyCreatedEvent when property is created")
        void should_RaisePropertyCreatedEvent_When_PropertyIsCreated() {
            // Act
            Property property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );

            // Assert
            assertThat(property.getDomainEvents()).hasSize(1);
            assertThat(property.getDomainEvents().get(0)).isInstanceOf(PropertyCreatedEvent.class);
            
            PropertyCreatedEvent event = (PropertyCreatedEvent) property.getDomainEvents().get(0);
            assertThat(event.getPropertyId()).isEqualTo(propertyId);
            assertThat(event.getOwnerId()).isEqualTo(ownerId);
            assertThat(event.getTitle()).isEqualTo(validTitle);
            assertThat(event.getPropertyType()).isEqualTo(propertyType);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("Should throw exception when title is invalid")
        void should_ThrowException_When_TitleIsInvalid(String invalidTitle) {
            // Act & Assert
            assertThatThrownBy(() -> Property.create(
                propertyId, ownerId, invalidTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Title cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception when title exceeds 100 characters")
        void should_ThrowException_When_TitleTooLong() {
            // Arrange
            String longTitle = "a".repeat(101);

            // Act & Assert
            assertThatThrownBy(() -> Property.create(
                propertyId, ownerId, longTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Title cannot exceed 100 characters");
        }

        @Test
        @DisplayName("Should throw exception when required fields are null")
        void should_ThrowException_When_RequiredFieldsAreNull() {
            // Act & Assert
            assertThatThrownBy(() -> Property.create(
                null, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Property ID cannot be null");

            assertThatThrownBy(() -> Property.create(
                propertyId, null, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Owner ID cannot be null");

            assertThatThrownBy(() -> Property.create(
                propertyId, ownerId, validTitle, validDescription,
                null, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Property type cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when prices have different currencies")
        void should_ThrowException_When_PricesHaveDifferentCurrencies() {
            // Arrange
            Money usdPrice = new Money(new BigDecimal("500.00"), "USD");

            // Act & Assert
            assertThatThrownBy(() -> Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                usdPrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rent price and deposit must be in same currency");
        }

        @Test
        @DisplayName("Should throw exception when rent price is not positive")
        void should_ThrowException_When_RentPriceNotPositive() {
            // Arrange
            Money zeroPrice = Money.of(BigDecimal.ZERO);
            Money negativePrice = Money.of(new BigDecimal("-100.00"));

            // Act & Assert
            assertThatThrownBy(() -> Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                zeroPrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rent price must be positive");

            assertThatThrownBy(() -> Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                negativePrice, depositAmount, maintenanceFee
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rent price must be positive");
        }
    }

    @Nested
    @DisplayName("Property Status Management")
    class PropertyStatusManagementTests {

        private Property property;

        @BeforeEach
        void setUp() {
            property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );
            property.clearDomainEvents(); // Clear creation event for cleaner tests
        }

        @Test
        @DisplayName("Should activate property when approved")
        void should_ActivateProperty_When_Approved() {
            // Arrange
            UserId adminId = new UserId(UUID.randomUUID().toString());

            // Act
            property.approve(adminId);

            // Assert
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(property.getApprovedBy()).isEqualTo(adminId);
            assertThat(property.getApprovedAt()).isNotNull();
            assertThat(property.isAvailableForRent()).isTrue();
        }

        @Test
        @DisplayName("Should raise PropertyStatusChangedEvent when status changes")
        void should_RaiseStatusChangedEvent_When_StatusChanges() {
            // Act
            property.activate();

            // Assert
            assertThat(property.getDomainEvents()).hasSize(1);
            assertThat(property.getDomainEvents().get(0)).isInstanceOf(PropertyStatusChangedEvent.class);
            
            PropertyStatusChangedEvent event = (PropertyStatusChangedEvent) property.getDomainEvents().get(0);
            assertThat(event.getPropertyId()).isEqualTo(propertyId);
            assertThat(event.getOldStatus()).isEqualTo(PropertyStatus.PENDING_APPROVAL);
            assertThat(event.getNewStatus()).isEqualTo(PropertyStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should deactivate active property")
        void should_DeactivateProperty_When_Active() {
            // Arrange
            property.activate();
            property.clearDomainEvents();

            // Act
            property.deactivate();

            // Assert
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.INACTIVE);
            assertThat(property.isAvailableForRent()).isFalse();
        }

        @Test
        @DisplayName("Should mark property as rented")
        void should_MarkAsRented_When_PropertyRented() {
            // Arrange
            property.activate();
            property.clearDomainEvents();

            // Act
            property.markAsRented();

            // Assert
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.RENTED);
            assertThat(property.isAvailableForRent()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when invalid status transition attempted")
        void should_ThrowException_When_InvalidStatusTransition() {
            // Act & Assert - Cannot go directly from PENDING_APPROVAL to RENTED
            assertThatThrownBy(() -> property.changeStatus(PropertyStatus.RENTED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from PENDING_APPROVAL to RENTED");
        }

        @Test
        @DisplayName("Should reject property with reason")
        void should_RejectProperty_When_InPendingApproval() {
            // Act
            property.reject("Missing required documents");

            // Assert
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("Property Price Management")
    class PropertyPriceManagementTests {

        private Property property;

        @BeforeEach
        void setUp() {
            property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );
            property.clearDomainEvents();
        }

        @Test
        @DisplayName("Should update pricing with valid values")
        void should_UpdatePricing_When_ValidValuesProvided() {
            // Arrange
            Money newRentPrice = Money.of(new BigDecimal("600.00"));
            Money newDepositAmount = Money.of(new BigDecimal("2400.00"));

            // Act
            property.updatePricing(newRentPrice, newDepositAmount);

            // Assert
            assertThat(property.getRentPrice()).isEqualTo(newRentPrice);
            assertThat(property.getDepositAmount()).isEqualTo(newDepositAmount);
            assertThat(property.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should raise PriceChangedEvent when significant price change")
        void should_RaisePriceChangedEvent_When_SignificantPriceChange() {
            // Arrange - 20% increase (significant)
            Money newRentPrice = Money.of(new BigDecimal("600.00")); // 500 -> 600 = 20% increase

            // Act
            property.updatePricing(newRentPrice, depositAmount);

            // Assert
            assertThat(property.getDomainEvents()).hasSize(1);
            assertThat(property.getDomainEvents().get(0)).isInstanceOf(PropertyPriceChangedEvent.class);
            
            PropertyPriceChangedEvent event = (PropertyPriceChangedEvent) property.getDomainEvents().get(0);
            assertThat(event.getPropertyId()).isEqualTo(propertyId);
            assertThat(event.getOldPrice()).isEqualTo(rentPrice);
            assertThat(event.getNewPrice()).isEqualTo(newRentPrice);
        }

        @Test
        @DisplayName("Should not raise PriceChangedEvent when minor price change")
        void should_NotRaisePriceChangedEvent_When_MinorPriceChange() {
            // Arrange - 5% increase (not significant)
            Money newRentPrice = Money.of(new BigDecimal("525.00")); // 500 -> 525 = 5% increase

            // Act
            property.updatePricing(newRentPrice, depositAmount);

            // Assert
            assertThat(property.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Should calculate total upfront cost correctly")
        void should_CalculateTotalUpfrontCost_Correctly() {
            // Act
            Money totalUpfront = property.getTotalUpfrontCost();

            // Assert
            Money expectedTotal = rentPrice.add(depositAmount);
            assertThat(totalUpfront).isEqualTo(expectedTotal);
        }

        @Test
        @DisplayName("Should calculate total monthly cost with maintenance fee")
        void should_CalculateTotalMonthlyCost_WithMaintenanceFee() {
            // Act
            Money totalMonthly = property.getTotalMonthlyCost();

            // Assert
            Money expectedTotal = rentPrice.add(maintenanceFee);
            assertThat(totalMonthly).isEqualTo(expectedTotal);
        }

        @Test
        @DisplayName("Should calculate total monthly cost without maintenance fee")
        void should_CalculateTotalMonthlyCost_WithoutMaintenanceFee() {
            // Arrange
            Property propertyWithoutFee = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount // No maintenance fee
            );

            // Act
            Money totalMonthly = propertyWithoutFee.getTotalMonthlyCost();

            // Assert
            assertThat(totalMonthly).isEqualTo(rentPrice);
        }
    }

    @Nested
    @DisplayName("Property Features Management")
    class PropertyFeaturesManagementTests {

        private Property property;

        @BeforeEach
        void setUp() {
            property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );
        }

        @Test
        @DisplayName("Should add option successfully")
        void should_AddOption_When_ValidOptionProvided() {
            // Act
            property.addOption("Air Conditioning");

            // Assert
            assertThat(property.getOptions()).contains("Air Conditioning");
        }

        @Test
        @DisplayName("Should not add duplicate options")
        void should_NotAddDuplicateOption_When_OptionAlreadyExists() {
            // Arrange
            property.addOption("Air Conditioning");

            // Act
            property.addOption("Air Conditioning");

            // Assert
            assertThat(property.getOptions()).hasSize(1);
            assertThat(property.getOptions()).contains("Air Conditioning");
        }

        @Test
        @DisplayName("Should remove option successfully")
        void should_RemoveOption_When_OptionExists() {
            // Arrange
            property.addOption("Air Conditioning");

            // Act
            property.removeOption("Air Conditioning");

            // Assert
            assertThat(property.getOptions()).doesNotContain("Air Conditioning");
        }

        @Test
        @DisplayName("Should add image URL successfully")
        void should_AddImageUrl_When_ValidUrlProvided() {
            // Arrange
            String imageUrl = "https://example.com/image1.jpg";

            // Act
            property.addImageUrl(imageUrl);

            // Assert
            assertThat(property.getImageUrls()).contains(imageUrl);
        }

        @Test
        @DisplayName("Should update amenities successfully")
        void should_UpdateAmenities_When_ValidValuesProvided() {
            // Act
            property.updateAmenities(true, false, true, false);

            // Assert
            assertThat(property.getParkingAvailable()).isTrue();
            assertThat(property.getPetAllowed()).isFalse();
            assertThat(property.getFurnished()).isTrue();
            assertThat(property.getShortTermAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("Property Business Rules")
    class PropertyBusinessRulesTests {

        private Property property;

        @BeforeEach
        void setUp() {
            property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );
        }

        @Test
        @DisplayName("Should check ownership correctly")
        void should_CheckOwnership_Correctly() {
            // Assert
            assertThat(property.isOwnedBy(ownerId)).isTrue();
            assertThat(property.isOwnedBy(new UserId(UUID.randomUUID().toString()))).isFalse();
        }

        @Test
        @DisplayName("Should check availability for rent correctly")
        void should_CheckAvailabilityForRent_Correctly() {
            // Assert - Initially not available (PENDING_APPROVAL)
            assertThat(property.isAvailableForRent()).isFalse();

            // Act & Assert - Available after activation
            property.activate();
            assertThat(property.isAvailableForRent()).isTrue();

            // Act & Assert - Not available when rented
            property.markAsRented();
            assertThat(property.isAvailableForRent()).isFalse();
        }

        @Test
        @DisplayName("Should match criteria correctly")
        void should_MatchCriteria_When_CriteriaAreMet() {
            // Arrange
            property.activate();
            PropertySpecs searchSpecs = new PropertySpecs(2, 1, 0); // Same or less than property
            Money maxBudget = Money.of(new BigDecimal("1000.00")); // Higher than rent

            // Act
            boolean matches = property.matchesCriteria(searchSpecs, maxBudget);

            // Assert
            assertThat(matches).isTrue();
        }

        @Test
        @DisplayName("Should not match criteria when requirements not met")
        void should_NotMatchCriteria_When_RequirementsNotMet() {
            // Arrange
            property.activate();
            PropertySpecs searchSpecs = new PropertySpecs(3, 2, 0); // More than property has
            Money maxBudget = Money.of(new BigDecimal("400.00")); // Less than rent

            // Act
            boolean matches = property.matchesCriteria(searchSpecs, maxBudget);

            // Assert
            assertThat(matches).isFalse();
        }

        @Test
        @DisplayName("Should prevent modification when property is rented")
        void should_PreventModification_When_PropertyIsRented() {
            // Arrange
            property.activate();
            property.markAsRented();

            // Act & Assert
            assertThatThrownBy(() -> property.updateDetails("New Title", "New Description", specs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify property in status: RENTED");
        }

        @Test
        @DisplayName("Should validate property for activation")
        void should_ValidateForActivation_When_RequiredFieldsMissing() {
            // This test checks the validation logic indirectly through the approval process
            // The Property entity should validate all required fields are present before activation
            
            // Arrange - Create property with all required fields (should pass validation)
            Property validProperty = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );

            // Act & Assert - Should activate successfully
            assertThatNoException().isThrownBy(() -> validProperty.activate());
            assertThat(validProperty.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Property Invariants")
    class PropertyInvariantsTests {

        @Test
        @DisplayName("Should maintain data consistency after updates")
        void should_MaintainDataConsistency_AfterUpdates() {
            // Arrange
            Property property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );

            LocalDateTime creationTime = property.getCreatedAt();

            // Act - Multiple updates
            property.updateDetails("Updated Title", "Updated Description", 
                new PropertySpecs(3, 2, 1));
            property.updatePricing(Money.of(new BigDecimal("700.00")), 
                Money.of(new BigDecimal("2800.00")));
            property.addOption("Balcony");

            // Assert - Invariants maintained
            assertThat(property.getId()).isEqualTo(propertyId); // ID never changes
            assertThat(property.getOwnerId()).isEqualTo(ownerId); // Owner never changes
            assertThat(property.getCreatedAt()).isEqualTo(creationTime); // Creation time never changes
            assertThat(property.getUpdatedAt()).isAfter(creationTime); // Updated time advances
            assertThat(property.getTitle()).isEqualTo("Updated Title"); // Updates applied
        }

        @Test
        @DisplayName("Should return defensive copies of collections")
        void should_ReturnDefensiveCopies_OfCollections() {
            // Arrange
            Property property = Property.create(
                propertyId, ownerId, validTitle, validDescription,
                propertyType, rentalType, address, specs,
                rentPrice, depositAmount, maintenanceFee
            );
            property.addOption("Parking");

            // Act
            var options = property.getOptions();
            var imageUrls = property.getImageUrls();

            // Assert - Modifications to returned lists don't affect the property
            options.add("Should not be added to property");
            imageUrls.add("Should not be added to property");

            assertThat(property.getOptions()).hasSize(1);
            assertThat(property.getOptions()).contains("Parking");
            assertThat(property.getOptions()).doesNotContain("Should not be added to property");
            assertThat(property.getImageUrls()).isEmpty();
        }
    }
}