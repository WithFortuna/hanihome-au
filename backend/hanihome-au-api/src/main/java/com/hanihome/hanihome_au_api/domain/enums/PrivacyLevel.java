package com.hanihome.hanihome_au_api.domain.enums;

/**
 * Privacy levels for user profile information
 */
public enum PrivacyLevel {
    PUBLIC("전체 공개"),
    MEMBERS_ONLY("회원만"),
    PRIVATE("비공개");

    private final String description;

    PrivacyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}