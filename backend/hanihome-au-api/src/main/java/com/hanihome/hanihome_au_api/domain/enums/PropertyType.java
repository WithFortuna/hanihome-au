package com.hanihome.hanihome_au_api.domain.enums;

public enum PropertyType {
    APARTMENT("아파트"),
    VILLA("빌라"),
    STUDIO("원룸"),
    TWO_ROOM("투룸"),
    THREE_ROOM("쓰리룸"),
    OFFICETEL("오피스텔"),
    HOUSE("단독주택");

    private final String displayName;

    PropertyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}