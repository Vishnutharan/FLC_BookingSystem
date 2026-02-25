package com.flc.exception;

/**
 * Exception thrown when a member attempts to book a lesson that conflicts
 * with an existing booking (same day, week, and time slot).
 *
 * @author FLC Development Team
 */
public class TimeConflictException extends Exception {

    /**
     * Constructs a TimeConflictException with the specified detail message.
     *
     * @param message the detail message
     */
    public TimeConflictException(String message) {
        super(message);
    }
}
