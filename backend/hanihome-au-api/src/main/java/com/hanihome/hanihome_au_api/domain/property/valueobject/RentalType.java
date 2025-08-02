package com.hanihome.hanihome_au_api.domain.property.valueobject;

public enum RentalType {
    LONG_TERM("장기임대", 12),
    SHORT_TERM("단기임대", 1),
    MONTHLY("월세", 1),
    WEEKLY("주간임대", 0),
    DAILY("일일임대", 0);

    private final String displayName;
    private final int minimumMonths;

    RentalType(String displayName, int minimumMonths) {
        this.displayName = displayName;
        this.minimumMonths = minimumMonths;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinimumMonths() {
        return minimumMonths;
    }

    public boolean isLongTerm() {
        return minimumMonths >= 12;
    }
}