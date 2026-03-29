package com.flc.model;

/**
 * Enum representing the status of a lesson booking for a member.
 *
 * @author FLC Development Team
 */
public enum BookingStatus {
    /** Member has booked the lesson but hasn't attended yet. */
    BOOKED("Booked"),
    /** Booking has been moved to a different lesson but not yet attended. */
    CHANGED("Changed"),
    /** Member has attended the lesson. */
    ATTENDED("Attended"),
    /** Booking was cancelled. */
    CANCELLED("Cancelled");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
