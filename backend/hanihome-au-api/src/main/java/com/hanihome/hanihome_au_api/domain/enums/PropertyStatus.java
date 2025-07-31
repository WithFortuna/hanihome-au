package com.hanihome.hanihome_au_api.domain.enums;

public enum PropertyStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    PENDING_APPROVAL("승인대기"),
    REJECTED("거절됨"),
    COMPLETED("거래완료"),
    SUSPENDED("정지됨");

    private final String displayName;

    PropertyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}