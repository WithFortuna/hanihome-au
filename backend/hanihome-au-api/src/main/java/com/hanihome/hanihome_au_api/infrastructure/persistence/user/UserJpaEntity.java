package com.hanihome.hanihome_au_api.infrastructure.persistence.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @Column(unique = true, nullable = false, length = 255)
    private String email;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 500)
    private String bio;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 255)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRoleEnum role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private OAuthProviderEnum oauthProvider;
    
    @Column(name = "oauth_provider_id", nullable = false, length = 255)
    private String oauthProviderId;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserJpaEntity() {}

    public enum UserRoleEnum {
        TENANT, LANDLORD, AGENT, ADMIN
    }
    
    public enum OAuthProviderEnum {
        GOOGLE, KAKAO, APPLE
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public UserRoleEnum getRole() { return role; }
    public void setRole(UserRoleEnum role) { this.role = role; }
    
    public OAuthProviderEnum getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(OAuthProviderEnum oauthProvider) { this.oauthProvider = oauthProvider; }
    
    public String getOauthProviderId() { return oauthProviderId; }
    public void setOauthProviderId(String oauthProviderId) { this.oauthProviderId = oauthProviderId; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}