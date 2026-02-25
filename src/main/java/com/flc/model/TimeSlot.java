package com.flc.model;

/**
 * Enum representing the available time slots for lessons.
 * The leisure centre offers three slots per day: Morning, Afternoon, and Evening.
 *
 * @author FLC Development Team
 */
public enum TimeSlot {

    /** Morning time slot */
    MORNING("Morning"),
    /** Afternoon time slot */
    AFTERNOON("Afternoon"),
    /** Evening time slot */
    EVENING("Evening");

    private final String displayName;

    /**
     * Constructs a TimeSlot with a display name.
     *
     * @param displayName the human-readable name
     */
    TimeSlot(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the human-readable display name of this time slot.
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
