package com.flc.model;

/**
 * Custom enum representing the days of the week on which the leisure centre operates.
 * Only Saturday and Sunday are valid operating days.
 *
 * @author FLC Development Team
 */
public enum DayOfWeek {

    /** Saturday */
    SATURDAY("Saturday"),
    /** Sunday */
    SUNDAY("Sunday");

    private final String displayName;

    /**
     * Constructs a DayOfWeek with a display name.
     *
     * @param displayName the human-readable name
     */
    DayOfWeek(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name of this day.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
