package com.hanihome.hanihome_au_api.domain.user.valueobject;

import java.util.Set;

public enum UserRole {
    TENANT("임차인", Set.of("PROPERTY_READ", "APPLICATION_CREATE", "REVIEW_CREATE")),
    LANDLORD("임대인", Set.of("PROPERTY_CREATE", "PROPERTY_UPDATE", "APPLICATION_APPROVE")),
    AGENT("중개인", Set.of("PROPERTY_CREATE", "PROPERTY_UPDATE", "PROPERTY_APPROVE", "CLIENT_MANAGE")),
    ADMIN("관리자", Set.of("ALL_PERMISSIONS"));

    private final String displayName;
    private final Set<String> permissions;

    UserRole(String displayName, Set<String> permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains("ALL_PERMISSIONS") || permissions.contains(permission);
    }

    public boolean isHigherThan(UserRole other) {
        return this.ordinal() > other.ordinal();
    }
}