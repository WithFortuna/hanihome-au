package com.hanihome.hanihome_au_api.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    TENANT("임차인", "ROLE_TENANT", "property search, rental applications, tenant services", Set.of(
        Permission.PROPERTY_READ,
        Permission.APPLICATION_CREATE,
        Permission.APPLICATION_READ,
        Permission.APPLICATION_UPDATE,
        Permission.REVIEW_CREATE,
        Permission.REVIEW_READ,
        Permission.REVIEW_UPDATE,
        Permission.PAYMENT_CREATE,
        Permission.PAYMENT_READ,
        Permission.USER_READ,
        Permission.USER_UPDATE
    )),
    
    LANDLORD("임대인", "ROLE_LANDLORD", "property listing, tenant management, rental income tracking", Set.of(
        Permission.PROPERTY_CREATE,
        Permission.PROPERTY_READ,
        Permission.PROPERTY_UPDATE,
        Permission.PROPERTY_DELETE,
        Permission.APPLICATION_READ,
        Permission.APPLICATION_APPROVE,
        Permission.REVIEW_READ,
        Permission.REVIEW_MODERATE,
        Permission.PAYMENT_READ,
        Permission.PAYMENT_REFUND,
        Permission.LANDLORD_TENANTS,
        Permission.LANDLORD_INCOME,
        Permission.USER_READ,
        Permission.USER_UPDATE
    )),
    
    AGENT("중개인", "ROLE_AGENT", "property brokerage, client management, commission tracking", Set.of(
        Permission.PROPERTY_CREATE,
        Permission.PROPERTY_READ,
        Permission.PROPERTY_UPDATE,
        Permission.PROPERTY_DELETE,
        Permission.PROPERTY_APPROVE,
        Permission.APPLICATION_READ,
        Permission.APPLICATION_APPROVE,
        Permission.REVIEW_READ,
        Permission.REVIEW_MODERATE,
        Permission.PAYMENT_CREATE,
        Permission.PAYMENT_READ,
        Permission.PAYMENT_REFUND,
        Permission.AGENT_COMMISSION,
        Permission.AGENT_CLIENTS,
        Permission.USER_READ,
        Permission.USER_UPDATE
    )),
    
    ADMIN("관리자", "ROLE_ADMIN", "system administration, user management, platform oversight", Set.of(
        Permission.PROPERTY_CREATE, Permission.PROPERTY_READ, Permission.PROPERTY_UPDATE, Permission.PROPERTY_DELETE, Permission.PROPERTY_APPROVE,
        Permission.USER_READ, Permission.USER_UPDATE, Permission.USER_DELETE, Permission.USER_ROLE_CHANGE,
        Permission.APPLICATION_CREATE, Permission.APPLICATION_READ, Permission.APPLICATION_UPDATE, Permission.APPLICATION_APPROVE,
        Permission.REVIEW_CREATE, Permission.REVIEW_READ, Permission.REVIEW_UPDATE, Permission.REVIEW_DELETE, Permission.REVIEW_MODERATE,
        Permission.PAYMENT_CREATE, Permission.PAYMENT_READ, Permission.PAYMENT_REFUND,
        Permission.ADMIN_DASHBOARD, Permission.ADMIN_REPORTS, Permission.ADMIN_SETTINGS, Permission.ADMIN_USERS,
        Permission.AGENT_COMMISSION, Permission.AGENT_CLIENTS,
        Permission.LANDLORD_TENANTS, Permission.LANDLORD_INCOME,
        Permission.SYSTEM_HEALTH, Permission.SYSTEM_LOGS
    ));

    private final String displayName;
    private final String authority;
    private final String description;
    private final Set<Permission> permissions;

    public List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = permissions.stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
            .collect(Collectors.toList());
        
        // Add role authority
        authorities.add(new SimpleGrantedAuthority(this.authority));
        
        return authorities;
    }

    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission);
    }

    public boolean hasAuthority(String authority) {
        return this.authority.equals(authority);
    }

    public boolean isHigherThan(UserRole other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean canAccess(String resource) {
        return switch (this) {
            case ADMIN -> true; // Admin can access everything
            case AGENT -> resource.startsWith("agent") || resource.startsWith("public");
            case LANDLORD -> resource.startsWith("landlord") || resource.startsWith("public");
            case TENANT -> resource.startsWith("tenant") || resource.startsWith("public");
        };
    }

    public boolean canAccessProperty(Long propertyId, Long userId) {
        return switch (this) {
            case ADMIN, AGENT -> true;
            case LANDLORD -> hasPermission(Permission.PROPERTY_READ);
            case TENANT -> hasPermission(Permission.PROPERTY_READ);
        };
    }

    public boolean canManageUsers() {
        return hasPermission(Permission.USER_DELETE) || hasPermission(Permission.ADMIN_USERS);
    }

    public boolean canManageProperties() {
        return hasPermission(Permission.PROPERTY_CREATE) && hasPermission(Permission.PROPERTY_UPDATE);
    }

    public boolean canProcessPayments() {
        return hasPermission(Permission.PAYMENT_CREATE) || hasPermission(Permission.PAYMENT_REFUND);
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : values()) {
            if (userRole.authority.equals(role) || userRole.name().equals(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}