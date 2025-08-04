package com.hanihome.hanihome_au_api.domain.enums;

public enum ViewingStatus {
    REQUESTED("Viewing requested by tenant"),
    CONFIRMED("Viewing confirmed by landlord/agent"),
    CANCELLED("Viewing cancelled"),
    COMPLETED("Viewing completed"),
    NO_SHOW("Tenant did not show up for viewing");

    private final String description;

    ViewingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == REQUESTED || this == CONFIRMED;
    }

    public boolean isFinal() {
        return this == CANCELLED || this == COMPLETED || this == NO_SHOW;
    }
}