package com.hanihome.hanihome_au_api.testutil;

import com.hanihome.hanihome_au_api.domain.entity.FCMToken;
import com.hanihome.hanihome_au_api.domain.entity.Viewing;
import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;

import java.time.LocalDateTime;

/**
 * 알림 관련 엔티티들을 위한 테스트 데이터 빌더
 * 플루언트 인터페이스를 통해 직관적인 테스트 데이터 생성을 지원
 */
public class NotificationTestDataBuilder {
    
    private String id;
    private String userId;
    private String token;
    private String deviceId;
    private String platform;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsed;
    
    public NotificationTestDataBuilder() {
        // 기본값 설정
        this.id = BaseTestDataFactory.faker.internet().uuid();
        this.userId = BaseTestDataFactory.faker.internet().uuid();
        this.token = "fcm-token-" + BaseTestDataFactory.faker.internet().uuid();
        this.deviceId = "device-" + BaseTestDataFactory.faker.internet().uuid();
        this.platform = "android";
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public NotificationTestDataBuilder withId(String id) {
        this.id = id;
        return this;
    }
    
    public NotificationTestDataBuilder withRandomId() {
        this.id = BaseTestDataFactory.faker.internet().uuid();
        return this;
    }
    
    public NotificationTestDataBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }
    
    public NotificationTestDataBuilder withRandomUserId() {
        this.userId = BaseTestDataFactory.faker.internet().uuid();
        return this;
    }
    
    public NotificationTestDataBuilder withToken(String token) {
        this.token = token;
        return this;
    }
    
    public NotificationTestDataBuilder withRandomToken() {
        this.token = "fcm-token-" + BaseTestDataFactory.faker.internet().uuid();
        return this;
    }
    
    public NotificationTestDataBuilder withDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }
    
    public NotificationTestDataBuilder withRandomDeviceId() {
        this.deviceId = "device-" + BaseTestDataFactory.faker.internet().uuid();
        return this;
    }
    
    public NotificationTestDataBuilder withPlatform(String platform) {
        this.platform = platform;
        return this;
    }
    
    public NotificationTestDataBuilder withRandomPlatform() {
        String[] platforms = {"android", "ios", "web"};
        this.platform = platforms[BaseTestDataFactory.faker.random().nextInt(platforms.length)];
        return this;
    }
    
    public NotificationTestDataBuilder withActive(boolean active) {
        this.isActive = active;
        return this;
    }
    
    public NotificationTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public NotificationTestDataBuilder withCreatedAtDaysAgo(int days) {
        this.createdAt = LocalDateTime.now().minusDays(days);
        return this;
    }
    
    public NotificationTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public NotificationTestDataBuilder withLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
        return this;
    }
    
    public NotificationTestDataBuilder withLastUsedHoursAgo(int hours) {
        this.lastUsed = LocalDateTime.now().minusHours(hours);
        return this;
    }
    
    public NotificationTestDataBuilder withRecentlyUsed() {
        this.lastUsed = LocalDateTime.now().minusMinutes(BaseTestDataFactory.faker.number().numberBetween(1, 60));
        return this;
    }
    
    // Platform-specific convenience methods
    public NotificationTestDataBuilder asAndroidToken() {
        return withPlatform("android")
            .withDeviceId("android-" + BaseTestDataFactory.faker.internet().uuid())
            .withToken("fcm-android-" + BaseTestDataFactory.faker.internet().uuid());
    }
    
    public NotificationTestDataBuilder asiOSToken() {
        return withPlatform("ios")
            .withDeviceId("ios-" + BaseTestDataFactory.faker.internet().uuid())
            .withToken("fcm-ios-" + BaseTestDataFactory.faker.internet().uuid());
    }
    
    public NotificationTestDataBuilder asWebToken() {
        return withPlatform("web")
            .withDeviceId("web-" + BaseTestDataFactory.faker.internet().uuid())
            .withToken("fcm-web-" + BaseTestDataFactory.faker.internet().uuid());
    }
    
    // Status-specific convenience methods
    public NotificationTestDataBuilder asActive() {
        return withActive(true)
            .withRecentlyUsed();
    }
    
    public NotificationTestDataBuilder asInactive() {
        return withActive(false)
            .withLastUsedHoursAgo(BaseTestDataFactory.faker.number().numberBetween(168, 8760)); // 1주-1년 전
    }
    
    public NotificationTestDataBuilder asExpired() {
        return withActive(false)
            .withCreatedAtDaysAgo(90)
            .withLastUsedHoursAgo(1440); // 60일 전
    }
    
    // Testing-specific convenience methods
    public NotificationTestDataBuilder forUser(String userId) {
        return withUserId(userId);
    }
    
    public NotificationTestDataBuilder forIntegrationTesting() {
        return withId("integration-test-token-" + System.currentTimeMillis())
            .withToken("integration-fcm-token-" + System.currentTimeMillis())
            .withDeviceId("integration-device-" + System.currentTimeMillis())
            .withPlatform("android")
            .asActive();
    }
    
    // Random data generation
    public NotificationTestDataBuilder allRandom() {
        return withRandomId()
            .withRandomUserId()
            .withRandomToken()
            .withRandomDeviceId()
            .withRandomPlatform()
            .withActive(BaseTestDataFactory.faker.bool().bool())
            .withCreatedAtDaysAgo(BaseTestDataFactory.faker.number().numberBetween(1, 365))
            .withLastUsedHoursAgo(BaseTestDataFactory.faker.number().numberBetween(1, 8760));
    }
    
    public FCMToken build() {
        return FCMToken.builder()
            .id(id)
            .userId(userId)
            .token(token)
            .deviceId(deviceId)
            .platform(platform)
            .isActive(isActive)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .lastUsed(lastUsed)
            .build();
    }
    
    // Static factory methods
    public static NotificationTestDataBuilder anAndroidToken() {
        return new NotificationTestDataBuilder().asAndroidToken();
    }
    
    public static NotificationTestDataBuilder aniOSToken() {
        return new NotificationTestDataBuilder().asiOSToken();
    }
    
    public static NotificationTestDataBuilder aWebToken() {
        return new NotificationTestDataBuilder().asWebToken();
    }
    
    public static NotificationTestDataBuilder anActiveToken() {
        return new NotificationTestDataBuilder().asActive();
    }
    
    public static NotificationTestDataBuilder anInactiveToken() {
        return new NotificationTestDataBuilder().asInactive();
    }
    
    public static NotificationTestDataBuilder aRandomToken() {
        return new NotificationTestDataBuilder().allRandom();
    }
    
    /**
     * Viewing 엔티티를 위한 내부 빌더 클래스
     */
    public static class ViewingBuilder {
        private String id;
        private String propertyId;
        private String tenantId;
        private String landlordId;
        private Long agentId;
        private LocalDateTime scheduledDateTime;
        private Integer duration;
        private ViewingStatus status;
        private String notes;
        private String feedback;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime completedAt;
        
        public ViewingBuilder() {
            // 기본값 설정
            this.id = BaseTestDataFactory.faker.internet().uuid();
            this.propertyId = BaseTestDataFactory.faker.internet().uuid();
            this.tenantId = BaseTestDataFactory.faker.internet().uuid();
            this.landlordId = BaseTestDataFactory.faker.internet().uuid();
            this.scheduledDateTime = LocalDateTime.now().plusDays(2);
            this.duration = 30;
            this.status = ViewingStatus.SCHEDULED;
            this.notes = "Default viewing appointment";
            this.createdAt = LocalDateTime.now();
        }
        
        public ViewingBuilder withId(String id) {
            this.id = id;
            return this;
        }
        
        public ViewingBuilder withRandomId() {
            this.id = BaseTestDataFactory.faker.internet().uuid();
            return this;
        }
        
        public ViewingBuilder withPropertyId(String propertyId) {
            this.propertyId = propertyId;
            return this;
        }
        
        public ViewingBuilder withTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public ViewingBuilder withLandlordId(String landlordId) {
            this.landlordId = landlordId;
            return this;
        }
        
        public ViewingBuilder withAgentId(Long agentId) {
            this.agentId = agentId;
            return this;
        }
        
        public ViewingBuilder withScheduledDateTime(LocalDateTime scheduledDateTime) {
            this.scheduledDateTime = scheduledDateTime;
            return this;
        }
        
        public ViewingBuilder withScheduledInDays(int days) {
            this.scheduledDateTime = LocalDateTime.now().plusDays(days);
            return this;
        }
        
        public ViewingBuilder withScheduledInHours(int hours) {
            this.scheduledDateTime = LocalDateTime.now().plusHours(hours);
            return this;
        }
        
        public ViewingBuilder withDuration(int duration) {
            this.duration = duration;
            return this;
        }
        
        public ViewingBuilder withRandomDuration() {
            this.duration = BaseTestDataFactory.faker.number().numberBetween(15, 120);
            return this;
        }
        
        public ViewingBuilder withStatus(ViewingStatus status) {
            this.status = status;
            return this;
        }
        
        public ViewingBuilder withRandomStatus() {
            ViewingStatus[] statuses = ViewingStatus.values();
            this.status = statuses[BaseTestDataFactory.faker.random().nextInt(statuses.length)];
            return this;
        }
        
        public ViewingBuilder withNotes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public ViewingBuilder withRandomNotes() {
            this.notes = BaseTestDataFactory.faker.lorem().sentence();
            return this;
        }
        
        public ViewingBuilder withFeedback(String feedback) {
            this.feedback = feedback;
            return this;
        }
        
        public ViewingBuilder withRandomFeedback() {
            this.feedback = BaseTestDataFactory.faker.lorem().sentence();
            return this;
        }
        
        public ViewingBuilder withCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }
        
        // Status-specific convenience methods
        public ViewingBuilder asScheduled() {
            return withStatus(ViewingStatus.SCHEDULED)
                .withScheduledInDays(BaseTestDataFactory.faker.number().numberBetween(1, 30));
        }
        
        public ViewingBuilder asCompleted() {
            return withStatus(ViewingStatus.COMPLETED)
                .withScheduledInHours(-BaseTestDataFactory.faker.number().numberBetween(2, 48))
                .withCompletedAt(LocalDateTime.now().minusHours(1))
                .withRandomFeedback();
        }
        
        public ViewingBuilder asCancelled() {
            return withStatus(ViewingStatus.CANCELLED)
                .withNotes("Cancelled due to schedule conflict")
                .withUpdatedAt(LocalDateTime.now());
        }
        
        public ViewingBuilder asNoShow() {
            return withStatus(ViewingStatus.NO_SHOW)
                .withScheduledInHours(-BaseTestDataFactory.faker.number().numberBetween(2, 24))
                .withNotes("Tenant did not show up for appointment")
                .withUpdatedAt(LocalDateTime.now());
        }
        
        // Random data generation
        public ViewingBuilder allRandom() {
            return withRandomId()
                .withPropertyId(BaseTestDataFactory.faker.internet().uuid())
                .withTenantId(BaseTestDataFactory.faker.internet().uuid())
                .withLandlordId(BaseTestDataFactory.faker.internet().uuid())
                .withAgentId(BaseTestDataFactory.faker.number().numberBetween(1L, 1000L))
                .withScheduledInDays(BaseTestDataFactory.faker.number().numberBetween(1, 30))
                .withRandomDuration()
                .withRandomStatus()
                .withRandomNotes();
        }
        
        public Viewing build() {
            return Viewing.builder()
                .id(id)
                .propertyId(propertyId)
                .tenantId(tenantId)
                .landlordId(landlordId)
                .agentId(agentId)
                .scheduledDateTime(scheduledDateTime)
                .duration(duration)
                .status(status)
                .notes(notes)
                .feedback(feedback)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .completedAt(completedAt)
                .build();
        }
        
        // Static factory methods
        public static ViewingBuilder aScheduledViewing() {
            return new ViewingBuilder().asScheduled();
        }
        
        public static ViewingBuilder aCompletedViewing() {
            return new ViewingBuilder().asCompleted();
        }
        
        public static ViewingBuilder aCancelledViewing() {
            return new ViewingBuilder().asCancelled();
        }
        
        public static ViewingBuilder aNoShowViewing() {
            return new ViewingBuilder().asNoShow();
        }
        
        public static ViewingBuilder aRandomViewing() {
            return new ViewingBuilder().allRandom();
        }
    }
}