package com.hanihome.hanihome_au_api.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_tokens", indexes = {
        @Index(name = "idx_fcm_token_user_id", columnList = "user_id"),
        @Index(name = "idx_fcm_token_device_id", columnList = "device_id"),
        @Index(name = "idx_fcm_token_active", columnList = "active"),
        @Index(name = "idx_fcm_token_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FCMToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "device_type", length = 20)
    private String deviceType; // ANDROID, IOS, WEB

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public FCMToken(Long userId, String token, String deviceId, String deviceType, String appVersion) {
        this.userId = userId;
        this.token = token;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.appVersion = appVersion;
        this.active = true;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
        this.lastUsed = LocalDateTime.now();
    }

    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}