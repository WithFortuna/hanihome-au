package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import com.hanihome.hanihome_au_api.domain.enums.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "login_attempts", nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.TENANT;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_provider_id", nullable = false, length = 255)
    private String oauthProviderId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserPrivacySettings privacySettings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserPreferredRegion> preferredRegions = new ArrayList<>();

    // Helper methods
    public boolean isAgent() {
        return role == UserRole.AGENT;
    }

    public boolean isLandlord() {
        return role == UserRole.LANDLORD;
    }

    public boolean isTenant() {
        return role == UserRole.TENANT;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateProfile(String name, String phone, String bio, String address) {
        this.name = name != null ? name : this.name;
        this.phone = phone != null ? phone : this.phone;
        this.bio = bio != null ? bio : this.bio;
        this.address = address != null ? address : this.address;
    }

    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled != null && twoFactorEnabled;
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public void incrementLoginAttempts() {
        this.loginAttempts = (this.loginAttempts != null ? this.loginAttempts : 0) + 1;
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }

    public void lockAccount(int lockoutMinutes) {
        this.lockedUntil = LocalDateTime.now().plusMinutes(lockoutMinutes);
    }

    public void verifyEmail() {
        this.isEmailVerified = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    // Privacy and region management methods
    public void initializePrivacySettings() {
        if (this.privacySettings == null) {
            this.privacySettings = UserPrivacySettings.builder()
                    .user(this)
                    .build();
        }
    }

    public void addPreferredRegion(UserPreferredRegion region) {
        if (this.preferredRegions == null) {
            this.preferredRegions = new ArrayList<>();
        }
        region.setUser(this);
        this.preferredRegions.add(region);
    }

    public void removePreferredRegion(UserPreferredRegion region) {
        if (this.preferredRegions != null) {
            this.preferredRegions.remove(region);
            region.setUser(null);
        }
    }

    public List<UserPreferredRegion> getActiveRegions() {
        if (this.preferredRegions == null) {
            return new ArrayList<>();
        }
        return this.preferredRegions.stream()
                .filter(region -> region.getIsActive() != null && region.getIsActive())
                .toList();
    }

    public boolean hasGdprConsent() {
        return this.privacySettings != null && 
               this.privacySettings.getGdprConsentGiven() != null && 
               this.privacySettings.getGdprConsentGiven();
    }
}