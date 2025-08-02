package com.hanihome.hanihome_au_api.domain.property.valueobject;

public enum PropertyType {
    APARTMENT("아파트"),
    HOUSE("단독주택"),
    TOWNHOUSE("타운하우스"),
    CONDO("콘도"),
    STUDIO("스튜디오"),
    ROOM("룸");

    private final String displayName;

    PropertyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}