package com.hanihome.hanihome_au_api.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    // Property management permissions
    PROPERTY_CREATE("property:create", "Create property listings"),
    PROPERTY_READ("property:read", "View property listings"),
    PROPERTY_UPDATE("property:update", "Update property listings"),
    PROPERTY_DELETE("property:delete", "Delete property listings"),
    PROPERTY_APPROVE("property:approve", "Approve property listings"),
    
    // User management permissions
    USER_READ("user:read", "View user information"),
    USER_UPDATE("user:update", "Update user information"),
    USER_DELETE("user:delete", "Delete user accounts"),
    USER_ROLE_CHANGE("user:role:change", "Change user roles"),
    
    // Application management permissions
    APPLICATION_CREATE("application:create", "Submit rental applications"),
    APPLICATION_READ("application:read", "View rental applications"),
    APPLICATION_UPDATE("application:update", "Update rental applications"),
    APPLICATION_APPROVE("application:approve", "Approve/reject applications"),
    
    // Review permissions
    REVIEW_CREATE("review:create", "Create reviews"),
    REVIEW_READ("review:read", "View reviews"),
    REVIEW_UPDATE("review:update", "Update own reviews"),
    REVIEW_DELETE("review:delete", "Delete reviews (own or any)"),
    REVIEW_MODERATE("review:moderate", "Moderate reviews"),
    
    // Financial permissions
    PAYMENT_CREATE("payment:create", "Process payments"),
    PAYMENT_READ("payment:read", "View payment information"),
    PAYMENT_REFUND("payment:refund", "Process refunds"),
    
    // Administrative permissions
    ADMIN_DASHBOARD("admin:dashboard", "Access admin dashboard"),
    ADMIN_REPORTS("admin:reports", "Generate reports"),
    ADMIN_SETTINGS("admin:settings", "Modify system settings"),
    ADMIN_USERS("admin:users", "Full user management"),
    
    // Agent-specific permissions
    AGENT_COMMISSION("agent:commission", "Manage commission settings"),
    AGENT_CLIENTS("agent:clients", "Manage client relationships"),
    
    // Landlord-specific permissions
    LANDLORD_TENANTS("landlord:tenants", "Manage tenant relationships"),
    LANDLORD_INCOME("landlord:income", "View rental income reports"),
    
    // System permissions
    SYSTEM_HEALTH("system:health", "View system health"),
    SYSTEM_LOGS("system:logs", "Access system logs");

    private final String permission;
    private final String description;

    public static Permission fromString(String permission) {
        for (Permission p : values()) {
            if (p.permission.equals(permission)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + permission);
    }
}