package com.flc.exception;

/**
 * Exception thrown when attempting to book a lesson that is already at maximum
 * capacity.
 *
 * @author FLC Development Team
 */
public class LessonFullException extends Exception {

    /**
     * Constructs a LessonFullException with the specified detail message.
     *
     * @param message the detail message
     */
    public LessonFullException(String message) {
        super(message);
    }
}
