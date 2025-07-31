package com.hanihome.hanihome_au_api.domain.enums;

public enum RentalType {
    MONTHLY("월세"),
    JEONSE("전세"),
    SALE("매매");

    private final String displayName;

    RentalType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}