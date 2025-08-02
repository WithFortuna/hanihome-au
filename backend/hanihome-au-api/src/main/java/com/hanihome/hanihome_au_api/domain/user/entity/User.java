package com.hanihome.hanihome_au_api.domain.user.entity;

import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;
import com.hanihome.hanihome_au_api.domain.user.event.UserRegisteredEvent;
import com.hanihome.hanihome_au_api.domain.user.event.UserRoleChangedEvent;
import com.hanihome.hanihome_au_api.domain.user.exception.UserException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private UserId id;
    private Email email;
    private String name;
    private String phoneNumber;
    private UserRole role;
    private boolean emailVerified;
    private boolean phoneVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    private List<Object> domainEvents = new ArrayList<>();

    protected User() {}

    private User(UserId id, Email email, String name, UserRole role) {
        this.id = id;
        this.email = email;
        this.name = validateName(name);
        this.role = role;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        this.domainEvents.add(new UserRegisteredEvent(id, email, name, role));
    }

    public static User create(UserId id, Email email, String name, UserRole role) {
        if (id == null) throw new UserException("User ID cannot be null");
        if (email == null) throw new UserException("Email cannot be null");
        if (role == null) throw new UserException("User role cannot be null");
        
        return new User(id, email, name, role);
    }

    public void updateProfile(String name, String phoneNumber) {
        this.name = validateName(name);
        this.phoneNumber = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeRole(UserRole newRole) {
        if (newRole == null) {
            throw new UserException("New role cannot be null");
        }
        
        if (this.role != newRole) {
            UserRole oldRole = this.role;
            this.role = newRole;
            this.updatedAt = LocalDateTime.now();
            
            this.domainEvents.add(new UserRoleChangedEvent(this.id, oldRole, newRole));
        }
    }

    public void verifyEmail() {
        if (!this.emailVerified) {
            this.emailVerified = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void verifyPhone() {
        if (!this.phoneVerified) {
            this.phoneVerified = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isFullyVerified() {
        return emailVerified && phoneVerified;
    }

    public boolean hasPermission(String permission) {
        return role.hasPermission(permission);
    }

    public boolean canManageProperty() {
        return role == UserRole.LANDLORD || role == UserRole.AGENT || role == UserRole.ADMIN;
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new UserException("Name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new UserException("Name cannot exceed 100 characters");
        }
        return name.trim();
    }

    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // Getters
    public UserId getId() { return id; }
    public Email getEmail() { return email; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public UserRole getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean isPhoneVerified() { return phoneVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
}