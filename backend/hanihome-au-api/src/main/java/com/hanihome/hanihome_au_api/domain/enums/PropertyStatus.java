package com.hanihome.hanihome_au_api.domain.enums;

import java.util.Set;

public enum PropertyStatus {
    DRAFT("임시저장"),
    PENDING_APPROVAL("승인대기"),
    ACTIVE("활성"),
    INACTIVE("비활성"),
    RENTED("임대완료"),
    COMPLETED("거래완료"),
    REJECTED("거절됨"),
    SUSPENDED("정지됨"),
    DELETED("삭제됨");

    private final String displayName;

    PropertyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if the current status can transition to the target status
     */
    public boolean canTransitionTo(PropertyStatus target) {
        if (target == null) return false;
        if (this == target) return true;
        
        return switch (this) {
            case DRAFT -> Set.of(PENDING_APPROVAL, DELETED).contains(target);
            case PENDING_APPROVAL -> Set.of(ACTIVE, REJECTED, DRAFT).contains(target);
            case ACTIVE -> Set.of(INACTIVE, RENTED, COMPLETED, SUSPENDED).contains(target);
            case INACTIVE -> Set.of(ACTIVE, SUSPENDED, DELETED).contains(target);
            case RENTED -> Set.of(COMPLETED, ACTIVE).contains(target);
            case REJECTED -> Set.of(DRAFT, PENDING_APPROVAL).contains(target);
            case SUSPENDED -> Set.of(ACTIVE, INACTIVE, DELETED).contains(target);
            case COMPLETED, DELETED -> false; // Terminal states
        };
    }

    /**
     * Checks if property in this status is available for rent
     */
    public boolean isAvailableForRent() {
        return this == ACTIVE;
    }

    /**
     * Checks if property can be modified in this status
     */
    public boolean canBeModified() {
        return this != RENTED && this != DELETED && this != COMPLETED;
    }

    /**
     * Checks if this is a terminal state (no further transitions allowed)
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == DELETED;
    }
}