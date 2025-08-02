package com.hanihome.hanihome_au_api.domain.property.valueobject;

public enum PropertyStatus {
    DRAFT("임시저장"),
    PENDING_APPROVAL("승인대기"),
    ACTIVE("활성"),
    INACTIVE("비활성"),
    RENTED("임대완료"),
    SUSPENDED("일시정지"),
    DELETED("삭제됨");

    private final String displayName;

    PropertyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(PropertyStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == PENDING_APPROVAL || newStatus == DELETED;
            case PENDING_APPROVAL -> newStatus == ACTIVE || newStatus == INACTIVE || newStatus == DELETED;
            case ACTIVE -> newStatus == INACTIVE || newStatus == RENTED || newStatus == SUSPENDED || newStatus == DELETED;
            case INACTIVE -> newStatus == ACTIVE || newStatus == DELETED;
            case RENTED -> newStatus == ACTIVE || newStatus == INACTIVE;
            case SUSPENDED -> newStatus == ACTIVE || newStatus == INACTIVE || newStatus == DELETED;
            case DELETED -> false;
        };
    }

    public boolean isAvailableForRent() {
        return this == ACTIVE;
    }
}