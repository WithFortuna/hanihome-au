package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.entity.FCMToken;
import com.hanihome.hanihome_au_api.domain.entity.Viewing;
import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 알림 관련 엔티티들의 TestDataFactory
 */
public class NotificationTestDataFactory extends BaseTestDataFactory<FCMToken> {

    @Override
    public FCMToken createDefault() {
        return FCMToken.builder()
            .id(faker.internet().uuid())
            .userId(faker.internet().uuid())
            .token("default-fcm-token-" + faker.internet().uuid())
            .deviceId("device-" + faker.internet().uuid())
            .platform("android")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Override
    public FCMToken createMinimal() {
        return FCMToken.builder()
            .id(faker.internet().uuid())
            .userId(faker.internet().uuid())
            .token("minimal-token")
            .platform("android")
            .isActive(true)
            .build();
    }

    @Override
    public FCMToken createMaximal() {
        return FCMToken.builder()
            .id(faker.internet().uuid())
            .userId(faker.internet().uuid())
            .token("maximal-fcm-token-with-long-identifier-" + faker.internet().uuid())
            .deviceId("device-with-detailed-info-" + faker.internet().uuid())
            .platform("ios")
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .lastUsed(LocalDateTime.now().minusHours(1))
            .build();
    }

    @Override
    public FCMToken createInvalid() {
        return FCMToken.builder()
            .id(null) // Invalid: null ID
            .userId("") // Invalid: empty user ID
            .token("") // Invalid: empty token
            .platform("invalid-platform") // Invalid platform
            .isActive(null) // Invalid: null active status
            .build();
    }

    @Override
    public FCMToken createRandom() {
        String[] platforms = {"android", "ios", "web"};
        
        return FCMToken.builder()
            .id(faker.internet().uuid())
            .userId(faker.internet().uuid())
            .token("fcm-token-" + faker.internet().uuid())
            .deviceId("device-" + faker.internet().uuid())
            .platform(platforms[faker.random().nextInt(platforms.length)])
            .isActive(faker.bool().bool())
            .createdAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30)))
            .lastUsed(faker.bool().bool() ? 
                LocalDateTime.now().minusHours(faker.number().numberBetween(1, 72)) : null)
            .build();
    }

    @Override
    public List<FCMToken> createBulk(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createRandom())
            .toList();
    }

    // Platform-specific factory methods
    public FCMToken createAndroidToken() {
        return createDefault().toBuilder()
            .platform("android")
            .deviceId("android-" + faker.internet().uuid())
            .build();
    }

    public FCMToken createiOSToken() {
        return createDefault().toBuilder()
            .platform("ios")
            .deviceId("ios-" + faker.internet().uuid())
            .build();
    }

    public FCMToken createWebToken() {
        return createDefault().toBuilder()
            .platform("web")
            .deviceId("web-" + faker.internet().uuid())
            .build();
    }

    // Status-specific factory methods
    public FCMToken createActiveToken() {
        return createDefault().toBuilder()
            .isActive(true)
            .lastUsed(LocalDateTime.now().minusMinutes(faker.number().numberBetween(1, 60)))
            .build();
    }

    public FCMToken createInactiveToken() {
        return createDefault().toBuilder()
            .isActive(false)
            .lastUsed(LocalDateTime.now().minusDays(faker.number().numberBetween(7, 30)))
            .build();
    }

    public FCMToken createExpiredToken() {
        return createDefault().toBuilder()
            .isActive(false)
            .createdAt(LocalDateTime.now().minusDays(90))
            .lastUsed(LocalDateTime.now().minusDays(60))
            .build();
    }

    // User-specific factory methods
    public FCMToken createTokenForUser(String userId) {
        return createDefault().toBuilder()
            .userId(userId)
            .build();
    }

    public List<FCMToken> createTokensForUser(String userId, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createTokenForUser(userId))
            .toList();
    }

    // Multi-device scenarios
    public List<FCMToken> createMultiDeviceTokensForUser(String userId) {
        return List.of(
            createAndroidToken().toBuilder().userId(userId).build(),
            createiOSToken().toBuilder().userId(userId).build(),
            createWebToken().toBuilder().userId(userId).build()
        );
    }

    // Viewing entity factory methods (as they're related to notifications)
    public static class ViewingFactory extends BaseTestDataFactory<Viewing> {

        @Override
        public Viewing createDefault() {
            return Viewing.builder()
                .id(faker.internet().uuid())
                .propertyId(faker.internet().uuid())
                .tenantId(faker.internet().uuid())
                .landlordId(faker.internet().uuid())
                .scheduledDateTime(LocalDateTime.now().plusDays(2))
                .duration(30)
                .status(ViewingStatus.SCHEDULED)
                .notes("Default viewing appointment")
                .createdAt(LocalDateTime.now())
                .build();
        }

        @Override
        public Viewing createMinimal() {
            return Viewing.builder()
                .id(faker.internet().uuid())
                .propertyId(faker.internet().uuid())
                .tenantId(faker.internet().uuid())
                .scheduledDateTime(LocalDateTime.now().plusHours(24))
                .status(ViewingStatus.SCHEDULED)
                .build();
        }

        @Override
        public Viewing createMaximal() {
            return Viewing.builder()
                .id(faker.internet().uuid())
                .propertyId(faker.internet().uuid())
                .tenantId(faker.internet().uuid())
                .landlordId(faker.internet().uuid())
                .agentId(faker.number().numberBetween(1L, 1000L))
                .scheduledDateTime(LocalDateTime.now().plusDays(7))
                .duration(60)
                .status(ViewingStatus.COMPLETED)
                .notes("Comprehensive viewing with detailed property tour and Q&A session")
                .feedback("Excellent property condition, very impressed with the layout")
                .createdAt(LocalDateTime.now().minusDays(8))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();
        }

        @Override
        public Viewing createInvalid() {
            return Viewing.builder()
                .id(null) // Invalid: null ID
                .propertyId("") // Invalid: empty property ID
                .tenantId(null) // Invalid: null tenant ID
                .scheduledDateTime(LocalDateTime.now().minusDays(1)) // Invalid: past date
                .duration(-30) // Invalid: negative duration
                .status(null) // Invalid: null status
                .build();
        }

        @Override
        public Viewing createRandom() {
            ViewingStatus[] statuses = ViewingStatus.values();
            
            return Viewing.builder()
                .id(faker.internet().uuid())
                .propertyId(faker.internet().uuid())
                .tenantId(faker.internet().uuid())
                .landlordId(faker.internet().uuid())
                .scheduledDateTime(LocalDateTime.now().plusDays(faker.number().numberBetween(1, 30)))
                .duration(faker.number().numberBetween(15, 120))
                .status(statuses[faker.random().nextInt(statuses.length)])
                .notes(faker.lorem().sentence())
                .createdAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)))
                .build();
        }

        @Override
        public List<Viewing> createBulk(int count) {
            return IntStream.range(0, count)
                .mapToObj(i -> createRandom())
                .toList();
        }

        // Status-specific factory methods
        public Viewing createScheduled() {
            return createDefault();
        }

        public Viewing createCompleted() {
            return createDefault().toBuilder()
                .status(ViewingStatus.COMPLETED)
                .completedAt(LocalDateTime.now().minusHours(2))
                .feedback("Property viewed successfully")
                .build();
        }

        public Viewing createCancelled() {
            return createDefault().toBuilder()
                .status(ViewingStatus.CANCELLED)
                .notes("Cancelled due to schedule conflict")
                .updatedAt(LocalDateTime.now())
                .build();
        }

        public Viewing createNoShow() {
            return createDefault().toBuilder()
                .status(ViewingStatus.NO_SHOW)
                .scheduledDateTime(LocalDateTime.now().minusHours(2))
                .notes("Tenant did not show up for appointment")
                .updatedAt(LocalDateTime.now())
                .build();
        }
    }
}