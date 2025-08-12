package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.entity.*;
import com.hanihome.hanihome_au_api.domain.enums.*;
import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Address;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.infrastructure.persistence.property.PropertyJpaEntity;
import com.hanihome.hanihome_au_api.infrastructure.persistence.user.UserJpaEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 테스트 데이터 생성을 위한 팩토리 클래스
 */
public class TestDataFactory {

    // User 관련 테스트 데이터
    public static class Users {
        
        public static User.UserBuilder defaultUser() {
            return User.builder()
                .id(UUID.randomUUID().toString())
                .email(new Email("test@example.com"))
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("0412345678")
                .isActive(true)
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now());
        }

        public static User createValidUser() {
            return defaultUser().build();
        }

        public static User createAdminUser() {
            return defaultUser()
                .email(new Email("admin@hanihome.com"))
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .build();
        }

        public static UserJpaEntity createUserJpaEntity() {
            UserJpaEntity entity = new UserJpaEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setEmail("test@example.com");
            entity.setFirstName("John");
            entity.setLastName("Doe");
            entity.setPhoneNumber("0412345678");
            entity.setIsActive(true);
            entity.setRole(UserRole.USER);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            return entity;
        }
    }

    // Property 관련 테스트 데이터
    public static class Properties {

        public static Property.PropertyBuilder defaultProperty() {
            return Property.builder()
                .id(UUID.randomUUID().toString())
                .title("Test Property")
                .description("A beautiful test property")
                .propertyType(PropertyType.APARTMENT)
                .rentalType(RentalType.LONG_TERM)
                .price(Money.of(new BigDecimal("500.00")))
                .bedrooms(2)
                .bathrooms(1)
                .parkingSpaces(1)
                .address(createDefaultAddress())
                .isAvailable(true)
                .status(PropertyStatus.ACTIVE)
                .ownerId("owner-123")
                .createdAt(LocalDateTime.now());
        }

        public static Property createValidProperty() {
            return defaultProperty().build();
        }

        public static Property createLuxuryProperty() {
            return defaultProperty()
                .title("Luxury Penthouse")
                .description("Stunning luxury penthouse with harbor views")
                .propertyType(PropertyType.PENTHOUSE)
                .price(Money.of(new BigDecimal("2000.00")))
                .bedrooms(3)
                .bathrooms(2)
                .parkingSpaces(2)
                .build();
        }

        public static Property createUnavailableProperty() {
            return defaultProperty()
                .isAvailable(false)
                .status(PropertyStatus.RENTED)
                .build();
        }

        public static PropertyJpaEntity createPropertyJpaEntity() {
            PropertyJpaEntity entity = new PropertyJpaEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setTitle("Test Property");
            entity.setDescription("A beautiful test property");
            entity.setPropertyType(PropertyType.APARTMENT);
            entity.setRentalType(RentalType.LONG_TERM);
            entity.setPriceAmount(new BigDecimal("500.00"));
            entity.setPriceCurrency("AUD");
            entity.setBedrooms(2);
            entity.setBathrooms(1);
            entity.setParkingSpaces(1);
            entity.setStreetAddress("123 Test Street");
            entity.setSuburb("Test Suburb");
            entity.setState("NSW");
            entity.setPostcode("2000");
            entity.setCountry("Australia");
            entity.setLatitude(-33.8688);
            entity.setLongitude(151.2093);
            entity.setIsAvailable(true);
            entity.setStatus(PropertyStatus.ACTIVE);
            entity.setOwnerId("owner-123");
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            return entity;
        }
    }

    // Transaction 관련 테스트 데이터
    public static class Transactions {

        public static Transaction.TransactionBuilder defaultTransaction() {
            return Transaction.builder()
                .id(UUID.randomUUID().toString())
                .propertyId("property-123")
                .tenantId("tenant-123")
                .landlordId("landlord-123")
                .status(TransactionStatus.PENDING)
                .startDate(LocalDateTime.now().plusDays(7))
                .endDate(LocalDateTime.now().plusDays(37))
                .monthlyRent(Money.of(new BigDecimal("500.00")))
                .bondAmount(Money.of(new BigDecimal("2000.00")))
                .createdAt(LocalDateTime.now());
        }

        public static Transaction createValidTransaction() {
            return defaultTransaction().build();
        }

        public static Transaction createCompletedTransaction() {
            return defaultTransaction()
                .status(TransactionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();
        }
    }

    // FCMToken 관련 테스트 데이터
    public static class FCMTokens {

        public static FCMToken.FCMTokenBuilder defaultFCMToken() {
            return FCMToken.builder()
                .id(UUID.randomUUID().toString())
                .userId("user-123")
                .token("test-fcm-token-" + UUID.randomUUID().toString())
                .deviceId("device-123")
                .platform("android")
                .isActive(true)
                .createdAt(LocalDateTime.now());
        }

        public static FCMToken createValidFCMToken() {
            return defaultFCMToken().build();
        }

        public static FCMToken createiOSToken() {
            return defaultFCMToken()
                .platform("ios")
                .deviceId("ios-device-123")
                .build();
        }
    }

    // PropertyFavorite 관련 테스트 데이터
    public static class PropertyFavorites {

        public static PropertyFavorite.PropertyFavoriteBuilder defaultPropertyFavorite() {
            return PropertyFavorite.builder()
                .id(UUID.randomUUID().toString())
                .userId("user-123")
                .propertyId("property-123")
                .createdAt(LocalDateTime.now());
        }

        public static PropertyFavorite createValidPropertyFavorite() {
            return defaultPropertyFavorite().build();
        }
    }

    // SearchHistory 관련 테스트 데이터
    public static class SearchHistories {

        public static SearchHistory.SearchHistoryBuilder defaultSearchHistory() {
            return SearchHistory.builder()
                .id(UUID.randomUUID().toString())
                .userId("user-123")
                .searchQuery("2 bedroom apartment")
                .suburb("Sydney")
                .state("NSW")
                .minPrice(Money.of(new BigDecimal("400.00")))
                .maxPrice(Money.of(new BigDecimal("800.00")))
                .propertyType(PropertyType.APARTMENT)
                .bedrooms(2)
                .resultsCount(15)
                .createdAt(LocalDateTime.now());
        }

        public static SearchHistory createValidSearchHistory() {
            return defaultSearchHistory().build();
        }
    }

    // Viewing 관련 테스트 데이터
    public static class Viewings {

        public static Viewing.ViewingBuilder defaultViewing() {
            return Viewing.builder()
                .id(UUID.randomUUID().toString())
                .propertyId("property-123")
                .tenantId("tenant-123")
                .landlordId("landlord-123")
                .scheduledDateTime(LocalDateTime.now().plusDays(2))
                .duration(30)
                .status(ViewingStatus.SCHEDULED)
                .notes("Standard viewing request")
                .createdAt(LocalDateTime.now());
        }

        public static Viewing createValidViewing() {
            return defaultViewing().build();
        }

        public static Viewing createCompletedViewing() {
            return defaultViewing()
                .status(ViewingStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .feedback("Good property, very clean")
                .build();
        }
    }

    // 공통 값 객체들
    public static Address createDefaultAddress() {
        return new Address(
            "123 Test Street",
            "Sydney",
            "NSW", 
            "Australia",
            "2000",
            -33.8688,
            151.2093
        );
    }

    public static Money createDefaultPrice() {
        return Money.of(new BigDecimal("500.00"));
    }

    public static Email createDefaultEmail() {
        return new Email("test@example.com");
    }

    // 대량 데이터 생성 유틸리티
    public static class Bulk {

        public static java.util.List<Property> createProperties(int count) {
            return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> Properties.defaultProperty()
                    .title("Test Property " + (i + 1))
                    .price(Money.of(new BigDecimal(400 + (i * 50))))
                    .build())
                .toList();
        }

        public static java.util.List<User> createUsers(int count) {
            return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> Users.defaultUser()
                    .email(new Email("user" + (i + 1) + "@example.com"))
                    .firstName("User" + (i + 1))
                    .build())
                .toList();
        }
    }

    // 빌더 패턴을 위한 헬퍼 메소드들
    public static <T> T buildWithDefaults(java.util.function.Supplier<T> builder) {
        return builder.get();
    }
    
    // 랜덤 데이터 생성 유틸리티
    public static class Random {
        private static final java.util.Random random = new java.util.Random();

        public static String randomString() {
            return UUID.randomUUID().toString().substring(0, 8);
        }

        public static String randomEmail() {
            return randomString() + "@example.com";
        }

        public static BigDecimal randomPrice() {
            return new BigDecimal(300 + random.nextInt(2000));
        }

        public static int randomBedrooms() {
            return 1 + random.nextInt(5);
        }

        public static PropertyType randomPropertyType() {
            PropertyType[] types = PropertyType.values();
            return types[random.nextInt(types.length)];
        }

        public static UserRole randomUserRole() {
            UserRole[] roles = UserRole.values();
            return roles[random.nextInt(roles.length)];
        }
    }
}