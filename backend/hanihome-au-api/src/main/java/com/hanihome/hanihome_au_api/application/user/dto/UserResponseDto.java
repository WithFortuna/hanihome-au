package com.hanihome.hanihome_au_api.application.user.dto;

import java.time.LocalDateTime;

public class UserResponseDto {
    private final Long id;
    private final String email;
    private final String name;
    private final String phoneNumber;
    private final String role;
    private final boolean emailVerified;
    private final boolean phoneVerified;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLoginAt;

    public UserResponseDto(Long id, String email, String name, String phoneNumber, String role,
                          boolean emailVerified, boolean phoneVerified, LocalDateTime createdAt, 
                          LocalDateTime lastLoginAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean isPhoneVerified() { return phoneVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
}