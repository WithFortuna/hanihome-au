package com.hanihome.hanihome_au_api.unit.domain;

import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.service.PropertyDomainService;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertySpecs;
import com.hanihome.hanihome_au_api.domain.property.event.*;
import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.event.*;
import com.hanihome.hanihome_au_api.domain.notification.entity.Notification;
import com.hanihome.hanihome_au_api.domain.notification.valueobject.NotificationId;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.enums.*;
import com.hanihome.hanihome_au_api.testutil.TestDataFactory;
import com.hanihome.hanihome_au_api.testutil.PropertyTestDataFactory;
import com.hanihome.hanihome_au_api.testutil.UserTestDataFactory;
import com.hanihome.hanihome_au_api.testutil.NotificationTestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive Domain Layer Integration Test
 * 
 * This test class verifies the integration between all domain layer components:
 * - Entity interactions and domain event propagation
 * - Value object validation and business rules
 * - Domain service orchestration
 * - Complex business workflows
 * - Edge cases and error scenarios
 * 
 * Target: 95% code coverage across domain layer
 */
@DisplayName("Domain Layer Integration Tests")
class DomainLayerIntegrationTest {

    private PropertyDomainService propertyDomainService;
    private UserId ownerId;
    private UserId adminId;
    private UserId tenantId;

    @BeforeEach
    void setUp() {
        propertyDomainService = new PropertyDomainService();
        ownerId = new UserId(UUID.randomUUID().toString());
        adminId = new UserId(UUID.randomUUID().toString());
        tenantId = new UserId(UUID.randomUUID().toString());
    }

    @Nested
    @DisplayName("Complete Property Management Workflow")
    class CompletePropertyManagementWorkflowTests {

        @Test
        @DisplayName("Should execute complete property lifecycle from creation to rental")
        void should_ExecuteCompletePropertyLifecycle() {
            // Phase 1: Property Creation
            Property property = createTestProperty();
            
            // Verify initial state
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.PENDING_APPROVAL);
            assertThat(property.getDomainEvents()).hasSize(1);
            assertThat(property.getDomainEvents().get(0)).isInstanceOf(PropertyCreatedEvent.class);
            
            // Phase 2: Property Approval Process
            property.approve(adminId);
            
            // Verify approval
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(property.getApprovedBy()).isEqualTo(adminId);
            assertThat(property.getApprovedAt()).isNotNull();
            assertThat(property.isAvailableForRent()).isTrue();
            
            // Verify events
            assertThat(property.getDomainEvents()).hasSize(2);
            assertThat(property.getDomainEvents().get(1)).isInstanceOf(PropertyStatusChangedEvent.class);
            
            PropertyStatusChangedEvent statusEvent = (PropertyStatusChangedEvent) property.getDomainEvents().get(1);
            assertThat(statusEvent.getOldStatus()).isEqualTo(PropertyStatus.PENDING_APPROVAL);
            assertThat(statusEvent.getNewStatus()).isEqualTo(PropertyStatus.ACTIVE);
            
            // Phase 3: Property Enhancement
            property.addOption("Air Conditioning");
            property.addOption("Pool");
            property.addImageUrl("https://example.com/image1.jpg");
            property.updateAmenities(true, false, true, false);
            
            // Verify enhancements
            assertThat(property.getOptions()).containsExactlyInAnyOrder("Air Conditioning", "Pool");
            assertThat(property.getImageUrls()).contains("https://example.com/image1.jpg");
            assertThat(property.getParkingAvailable()).isTrue();
            assertThat(property.getFurnished()).isTrue();
            
            // Phase 4: Price Adjustment (Significant Change)
            Money newRentPrice = Money.of(new BigDecimal("800.00")); // 20% increase
            property.updatePricing(newRentPrice, property.getDepositAmount());
            
            // Verify price change event
            assertThat(property.getDomainEvents()).hasSize(3);
            assertThat(property.getDomainEvents().get(2)).isInstanceOf(PropertyPriceChangedEvent.class);
            
            // Phase 5: Property Rental
            property.markAsRented();
            
            // Verify rental state
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.RENTED);
            assertThat(property.isAvailableForRent()).isFalse();
            
            // Phase 6: Attempt to modify rented property (should fail)
            assertThatThrownBy(() -> property.updateDetails("New Title", "New Description", property.getSpecs()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot modify property in status: RENTED");
        }

        @Test
        @DisplayName("Should handle property rejection workflow")
        void should_HandlePropertyRejectionWorkflow() {
            // Arrange
            Property property = createTestProperty();
            String rejectionReason = "Property photos are unclear";
            
            // Act
            property.reject(rejectionReason);
            
            // Assert
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.INACTIVE);
            assertThat(property.isAvailableForRent()).isFalse();
            
            // Verify events
            assertThat(property.getDomainEvents()).hasSize(2);
            assertThat(property.getDomainEvents().get(1)).isInstanceOf(PropertyStatusChangedEvent.class);
        }

        @Test
        @DisplayName("Should handle property deactivation and reactivation")
        void should_HandlePropertyDeactivationAndReactivation() {
            // Arrange
            Property property = createTestProperty();
            property.approve(adminId);
            property.clearDomainEvents();
            
            // Act - Deactivate
            property.deactivate();
            
            // Assert
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.INACTIVE);
            assertThat(property.isAvailableForRent()).isFalse();
            
            // Act - Reactivate
            property.activate();
            
            // Assert
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(property.isAvailableForRent()).isTrue();
            
            // Verify multiple status change events
            assertThat(property.getDomainEvents()).hasSize(2);
            assertThat(property.getDomainEvents().stream()
                .allMatch(event -> event instanceof PropertyStatusChangedEvent)).isTrue();
        }

        private Property createTestProperty() {
            return PropertyTestDataFactory.createDefaultProperty()
                .withOwner(ownerId)
                .withTitle("Luxury Apartment in Sydney")
                .withDescription("Beautiful 2-bedroom apartment with harbor views")
                .withPropertyType(PropertyType.APARTMENT)
                .withRentalType(RentalType.LONG_TERM)
                .withSpecs(new PropertySpecs(2, 2, 1))
                .withRentPrice(Money.of(new BigDecimal("600.00")))
                .withDepositAmount(Money.of(new BigDecimal("2400.00")))
                .withMaintenanceFee(Money.of(new BigDecimal("100.00")))
                .build();
        }
    }

    @Nested
    @DisplayName("User Entity Integration Tests")
    class UserEntityIntegrationTests {

        @Test
        @DisplayName("Should execute complete user registration and verification workflow")
        void should_ExecuteCompleteUserWorkflow() {
            // Phase 1: User Registration
            User user = createTestUser();
            
            // Verify initial state
            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRegisteredEvent.class);
            
            // Phase 2: Email Verification
            user.verifyEmail();
            
            // Verify verification
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.getEmailVerifiedAt()).isNotNull();
            
            // Verify events
            assertThat(user.getDomainEvents()).hasSize(2);
            assertThat(user.getDomainEvents().get(1)).isInstanceOf(UserEmailVerifiedEvent.class);
            
            // Phase 3: Profile Update
            user.updateProfile("John", "Doe", "+61-400-123-456");
            
            // Verify profile update
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getPhoneNumber()).isEqualTo("+61-400-123-456");
            
            // Phase 4: Account Suspension
            user.suspend("Suspicious activity detected");
            
            // Verify suspension
            assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            assertThat(user.isActive()).isFalse();
            
            // Phase 5: Account Reactivation
            user.reactivate();
            
            // Verify reactivation
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should handle user role management")
        void should_HandleUserRoleManagement() {
            // Arrange
            User user = createTestUser();
            user.verifyEmail();
            
            // Act - Grant admin role
            user.grantRole(Role.ADMIN);
            
            // Assert
            assertThat(user.getRoles()).contains(Role.ADMIN);
            assertThat(user.hasRole(Role.ADMIN)).isTrue();
            assertThat(user.hasAnyRole(Set.of(Role.ADMIN, Role.MODERATOR))).isTrue();
            
            // Act - Revoke role
            user.revokeRole(Role.ADMIN);
            
            // Assert
            assertThat(user.getRoles()).doesNotContain(Role.ADMIN);
            assertThat(user.hasRole(Role.ADMIN)).isFalse();
        }

        private User createTestUser() {
            return UserTestDataFactory.createDefaultUser()
                .withEmail(new Email("john.doe@example.com"))
                .withFirstName("John")
                .withLastName("Doe")
                .withPhoneNumber("+61-400-123-456")
                .build();
        }
    }

    @Nested
    @DisplayName("Value Object Integration Tests")
    class ValueObjectIntegrationTests {

        @Test
        @DisplayName("Should validate money operations across different scenarios")
        void should_ValidateMoneyOperations() {
            // Arrange
            Money rent = Money.of(new BigDecimal("500.00"));
            Money deposit = Money.of(new BigDecimal("2000.00"));
            Money maintenance = Money.of(new BigDecimal("50.00"));
            
            // Test addition
            Money totalUpfront = rent.add(deposit);
            assertThat(totalUpfront.getAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));
            
            // Test monthly cost calculation
            Money monthlyTotal = rent.add(maintenance);
            assertThat(monthlyTotal.getAmount()).isEqualByComparingTo(new BigDecimal("550.00"));
            
            // Test percentage calculations
            Money increased = rent.multiply(new BigDecimal("1.2")); // 20% increase
            assertThat(increased.getAmount()).isEqualByComparingTo(new BigDecimal("600.00"));
            
            // Test currency validation
            Money usdPrice = new Money(new BigDecimal("500.00"), "USD");
            assertThatThrownBy(() -> rent.add(usdPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
        }

        @Test
        @DisplayName("Should validate address completeness and formatting")
        void should_ValidateAddressCompletenessAndFormatting() {
            // Arrange & Act
            Address address = TestDataFactory.createDefaultAddress();
            
            // Assert core properties
            assertThat(address.getStreet()).isNotBlank();
            assertThat(address.getCity()).isNotBlank();
            assertThat(address.getState()).isNotBlank();
            assertThat(address.getPostalCode()).isNotBlank();
            assertThat(address.getCountry()).isNotBlank();
            
            // Test formatted address
            String formatted = address.getFormattedAddress();
            assertThat(formatted).contains(address.getStreet());
            assertThat(formatted).contains(address.getCity());
            assertThat(formatted).contains(address.getState());
            assertThat(formatted).contains(address.getPostalCode());
            
            // Test coordinates if available
            if (address.getLatitude() != null && address.getLongitude() != null) {
                assertThat(address.getLatitude()).isBetween(-90.0, 90.0);
                assertThat(address.getLongitude()).isBetween(-180.0, 180.0);
            }
        }

        @Test
        @DisplayName("Should validate email format and normalization")
        void should_ValidateEmailFormatAndNormalization() {
            // Valid emails
            assertThatNoException().isThrownBy(() -> new Email("test@example.com"));
            assertThatNoException().isThrownBy(() -> new Email("user.name+tag@domain.co.uk"));
            
            // Invalid emails
            assertThatThrownBy(() -> new Email("invalid.email"))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Email("@domain.com"))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Email("user@"))
                .isInstanceOf(IllegalArgumentException.class);
            
            // Test normalization
            Email email1 = new Email("Test@Example.COM");
            Email email2 = new Email("test@example.com");
            assertThat(email1).isEqualTo(email2); // Should be normalized to lowercase
        }

        @Test
        @DisplayName("Should validate property specs constraints")
        void should_ValidatePropertySpecsConstraints() {
            // Valid specs
            assertThatNoException().isThrownBy(() -> new PropertySpecs(3, 2, 1));
            assertThatNoException().isThrownBy(() -> new PropertySpecs(1, 1, 0));
            
            // Invalid specs - negative values
            assertThatThrownBy(() -> new PropertySpecs(-1, 2, 1))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new PropertySpecs(3, -1, 1))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new PropertySpecs(3, 2, -1))
                .isInstanceOf(IllegalArgumentException.class);
            
            // Invalid specs - logical constraints
            assertThatThrownBy(() -> new PropertySpecs(0, 2, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bedrooms must be at least 1");
        }
    }

    @Nested
    @DisplayName("Domain Service Integration Tests")
    class DomainServiceIntegrationTests {

        @Test
        @DisplayName("Should validate property eligibility for rental")
        void should_ValidatePropertyEligibilityForRental() {
            // Arrange
            Property property = createTestProperty();
            
            // Test ineligible property (pending approval)
            boolean eligible = propertyDomainService.isEligibleForRental(property);
            assertThat(eligible).isFalse();
            
            // Test eligible property (active)
            property.approve(adminId);
            eligible = propertyDomainService.isEligibleForRental(property);
            assertThat(eligible).isTrue();
            
            // Test ineligible property (rented)
            property.markAsRented();
            eligible = propertyDomainService.isEligibleForRental(property);
            assertThat(eligible).isFalse();
        }

        @Test
        @DisplayName("Should calculate property score based on multiple factors")
        void should_CalculatePropertyScoreBasedOnMultipleFactors() {
            // Arrange
            Property basicProperty = createTestProperty();
            Property enhancedProperty = createTestProperty();
            
            // Enhance the second property
            enhancedProperty.addOption("Air Conditioning");
            enhancedProperty.addOption("Pool");
            enhancedProperty.addOption("Gym");
            enhancedProperty.addImageUrl("https://example.com/image1.jpg");
            enhancedProperty.addImageUrl("https://example.com/image2.jpg");
            enhancedProperty.updateAmenities(true, true, true, true);
            
            // Act
            int basicScore = propertyDomainService.calculatePropertyScore(basicProperty);
            int enhancedScore = propertyDomainService.calculatePropertyScore(enhancedProperty);
            
            // Assert
            assertThat(enhancedScore).isGreaterThan(basicScore);
            assertThat(basicScore).isPositive();
            assertThat(enhancedScore).isPositive();
        }

        @Test
        @DisplayName("Should determine optimal pricing recommendations")
        void should_DetermineOptimalPricingRecommendations() {
            // Arrange
            Property property = createTestProperty();
            List<Property> comparableProperties = List.of(
                createComparableProperty(new BigDecimal("580.00")),
                createComparableProperty(new BigDecimal("620.00")),
                createComparableProperty(new BigDecimal("590.00"))
            );
            
            // Act
            Money recommendedPrice = propertyDomainService.calculateOptimalRentPrice(property, comparableProperties);
            
            // Assert
            assertThat(recommendedPrice).isNotNull();
            assertThat(recommendedPrice.getAmount()).isPositive();
            // Should be within reasonable range of comparable properties
            assertThat(recommendedPrice.getAmount()).isBetween(new BigDecimal("550.00"), new BigDecimal("650.00"));
        }

        private Property createTestProperty() {
            return PropertyTestDataFactory.createDefaultProperty()
                .withOwner(ownerId)
                .withSpecs(new PropertySpecs(2, 2, 1))
                .withRentPrice(Money.of(new BigDecimal("600.00")))
                .build();
        }

        private Property createComparableProperty(BigDecimal rentAmount) {
            return PropertyTestDataFactory.createDefaultProperty()
                .withOwner(ownerId)
                .withSpecs(new PropertySpecs(2, 2, 1))
                .withRentPrice(Money.of(rentAmount))
                .build();
        }
    }

    @Nested
    @DisplayName("Domain Event Integration Tests")
    class DomainEventIntegrationTests {

        @Test
        @DisplayName("Should generate appropriate events for complex property workflow")
        void should_GenerateAppropriateEventsForComplexWorkflow() {
            // Arrange
            Property property = createTestProperty();
            
            // Act - Execute complex workflow
            property.approve(adminId); // Event 1: PropertyStatusChangedEvent
            property.updatePricing(Money.of(new BigDecimal("720.00")), property.getDepositAmount()); // Event 2: PropertyPriceChangedEvent (20% increase)
            property.markAsRented(); // Event 3: PropertyStatusChangedEvent
            
            // Assert - Verify event sequence
            assertThat(property.getDomainEvents()).hasSize(4); // Including creation event
            
            // Verify event types in order
            assertThat(property.getDomainEvents().get(0)).isInstanceOf(PropertyCreatedEvent.class);
            assertThat(property.getDomainEvents().get(1)).isInstanceOf(PropertyStatusChangedEvent.class);
            assertThat(property.getDomainEvents().get(2)).isInstanceOf(PropertyPriceChangedEvent.class);
            assertThat(property.getDomainEvents().get(3)).isInstanceOf(PropertyStatusChangedEvent.class);
            
            // Verify specific event details
            PropertyPriceChangedEvent priceEvent = (PropertyPriceChangedEvent) property.getDomainEvents().get(2);
            assertThat(priceEvent.getOldPrice()).isEqualTo(Money.of(new BigDecimal("600.00")));
            assertThat(priceEvent.getNewPrice()).isEqualTo(Money.of(new BigDecimal("720.00")));
        }

        @Test
        @DisplayName("Should handle event clearing and accumulation correctly")
        void should_HandleEventClearingAndAccumulationCorrectly() {
            // Arrange
            Property property = createTestProperty();
            assertThat(property.getDomainEvents()).hasSize(1);
            
            // Act - Clear events
            property.clearDomainEvents();
            assertThat(property.getDomainEvents()).isEmpty();
            
            // Act - Generate new events
            property.activate();
            property.deactivate();
            
            // Assert - Only new events present
            assertThat(property.getDomainEvents()).hasSize(2);
            assertThat(property.getDomainEvents().stream()
                .allMatch(event -> event instanceof PropertyStatusChangedEvent)).isTrue();
        }

        private Property createTestProperty() {
            return PropertyTestDataFactory.createDefaultProperty()
                .withOwner(ownerId)
                .withRentPrice(Money.of(new BigDecimal("600.00")))
                .build();
        }
    }

    @Nested
    @DisplayName("Cross-Entity Integration Tests")
    class CrossEntityIntegrationTests {

        @Test
        @DisplayName("Should validate user-property ownership relationships")
        void should_ValidateUserPropertyOwnershipRelationships() {
            // Arrange
            User owner = createTestUser();
            User otherUser = createAnotherTestUser();
            Property property = createTestProperty();
            
            // Assert ownership validation
            assertThat(property.isOwnedBy(owner.getId())).isTrue();
            assertThat(property.isOwnedBy(otherUser.getId())).isFalse();
            
            // Test ownership business rules
            assertThat(owner.canManageProperty(property.getId())).isTrue();
            assertThat(otherUser.canManageProperty(property.getId())).isFalse();
        }

        @Test
        @DisplayName("Should create notification for property events")
        void should_CreateNotificationForPropertyEvents() {
            // Arrange
            Property property = createTestProperty();
            User owner = createTestUser();
            
            // Act - Property approval triggers notification
            property.approve(adminId);
            
            // Create notification for the event
            Notification notification = NotificationTestDataFactory.createDefaultNotification()
                .withRecipient(owner.getId())
                .withTitle("Property Approved")
                .withMessage("Your property has been approved and is now active")
                .withType(NotificationType.PROPERTY_STATUS_CHANGED)
                .withRelatedEntityId(property.getId().getValue())
                .build();
            
            // Assert
            assertThat(notification.getRecipientId()).isEqualTo(owner.getId());
            assertThat(notification.getType()).isEqualTo(NotificationType.PROPERTY_STATUS_CHANGED);
            assertThat(notification.getRelatedEntityId()).isEqualTo(property.getId().getValue());
            assertThat(notification.isRead()).isFalse();
            
            // Test notification lifecycle
            notification.markAsRead();
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
        }

        private User createTestUser() {
            return UserTestDataFactory.createDefaultUser()
                .withId(ownerId)
                .withEmail(new Email("owner@example.com"))
                .build();
        }

        private User createAnotherTestUser() {
            return UserTestDataFactory.createDefaultUser()
                .withId(tenantId)
                .withEmail(new Email("tenant@example.com"))
                .build();
        }

        private Property createTestProperty() {
            return PropertyTestDataFactory.createDefaultProperty()
                .withOwner(ownerId)
                .build();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesAndErrorScenariosTests {

        @Test
        @DisplayName("Should handle concurrent property modifications gracefully")
        void should_HandleConcurrentPropertyModificationsGracefully() {
            // Arrange
            Property property = createTestProperty();
            property.activate();
            
            // Simulate concurrent modifications with version checking
            Long initialVersion = property.getVersion();
            
            // First modification
            property.updateDetails("Updated Title 1", "Updated Description 1", property.getSpecs());
            assertThat(property.getVersion()).isGreaterThan(initialVersion);
            
            // Second modification
            Long afterFirstUpdate = property.getVersion();
            property.updatePricing(Money.of(new BigDecimal("700.00")), property.getDepositAmount());
            assertThat(property.getVersion()).isGreaterThan(afterFirstUpdate);
        }

        @Test
        @DisplayName("Should handle extreme price scenarios")
        void should_HandleExtremePriceScenarios() {
            // Test minimum valid price
            Money minPrice = Money.of(new BigDecimal("0.01"));
            assertThatNoException().isThrownBy(() -> 
                PropertyTestDataFactory.createDefaultProperty()
                    .withRentPrice(minPrice)
                    .build()
            );
            
            // Test very high price
            Money highPrice = Money.of(new BigDecimal("999999.99"));
            assertThatNoException().isThrownBy(() -> 
                PropertyTestDataFactory.createDefaultProperty()
                    .withRentPrice(highPrice)
                    .build()
            );
            
            // Test zero price (should fail)
            Money zeroPrice = Money.of(BigDecimal.ZERO);
            assertThatThrownBy(() -> 
                PropertyTestDataFactory.createDefaultProperty()
                    .withRentPrice(zeroPrice)
                    .build()
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle invalid state transitions comprehensively")
        void should_HandleInvalidStateTransitionsComprehensively() {
            Property property = createTestProperty();
            
            // Test all invalid transitions from PENDING_APPROVAL
            assertThatThrownBy(() -> property.changeStatus(PropertyStatus.RENTED))
                .isInstanceOf(IllegalStateException.class);
            
            // Test transitions from ACTIVE
            property.activate();
            assertThatNoException().isThrownBy(() -> property.changeStatus(PropertyStatus.RENTED));
            
            // Reset and test INACTIVE transitions
            property = createTestProperty();
            property.deactivate();
            assertThatThrownBy(() -> property.changeStatus(PropertyStatus.RENTED))
                .isInstanceOf(IllegalStateException.class);
        }

        @ParameterizedTest
        @EnumSource(PropertyStatus.class)
        @DisplayName("Should validate business rules for each property status")
        void should_ValidateBusinessRulesForEachPropertyStatus(PropertyStatus status) {
            Property property = createTestProperty();
            
            // Force property to specific status for testing
            switch (status) {
                case PENDING_APPROVAL -> {
                    // Already in this state
                }
                case ACTIVE -> property.activate();
                case INACTIVE -> property.deactivate();
                case RENTED -> {
                    property.activate();
                    property.markAsRented();
                }
            }
            
            // Test availability for rent based on status
            boolean shouldBeAvailable = status == PropertyStatus.ACTIVE;
            assertThat(property.isAvailableForRent()).isEqualTo(shouldBeAvailable);
            
            // Test modification restrictions for rented properties
            if (status == PropertyStatus.RENTED) {
                assertThatThrownBy(() -> property.updateDetails("New Title", "New Description", property.getSpecs()))
                    .isInstanceOf(IllegalStateException.class);
            }
        }

        private Property createTestProperty() {
            return PropertyTestDataFactory.createDefaultProperty()
                .withOwner(ownerId)
                .build();
        }
    }

    @Nested
    @DisplayName("Performance and Scalability Tests")
    class PerformanceAndScalabilityTests {

        @Test
        @DisplayName("Should handle large number of property options efficiently")
        void should_HandleLargeNumberOfPropertyOptionsEfficiently() {
            // Arrange
            Property property = createTestProperty();
            
            // Act - Add many options
            for (int i = 0; i < 100; i++) {
                property.addOption("Option " + i);
            }
            
            // Assert - All options added correctly
            assertThat(property.getOptions()).hasSize(100);
            
            // Test option removal
            property.removeOption("Option 50");
            assertThat(property.getOptions()).hasSize(99);
            assertThat(property.getOptions()).doesNotContain("Option 50");
        }

        @Test
        @DisplayName("Should handle complex property specs validation efficiently")
        void should_HandleComplexPropertySpecsValidationEfficiently() {
            PropertySpecs smallSpecs = new PropertySpecs(1, 1, 0);
            PropertySpecs mediumSpecs = new PropertySpecs(2, 2, 1);
            PropertySpecs largeSpecs = new PropertySpecs(5, 3, 2);
            
            Property property = createTestProperty();
            property.activate();
            
            // Test matching with various criteria
            assertThat(property.matchesCriteria(smallSpecs, Money.of(new BigDecimal("1000.00")))).isTrue();
            assertThat(property.matchesCriteria(mediumSpecs, Money.of(new BigDecimal("1000.00")))).isTrue();
            assertThat(property.matchesCriteria(largeSpecs, Money.of(new BigDecimal("1000.00")))).isFalse();
            
            // Test budget constraints
            assertThat(property.matchesCriteria(smallSpecs, Money.of(new BigDecimal("500.00")))).isFalse();
        }

        private Property createTestProperty() {
            return PropertyTestDataFactory.createDefaultProperty()
                .withOwner(ownerId)
                .withSpecs(new PropertySpecs(2, 2, 1))
                .withRentPrice(Money.of(new BigDecimal("600.00")))
                .build();
        }
    }
}