package com.flc.exception;

/**
 * Exception thrown when an invalid rating is provided for a review.
 * Valid ratings are between 1 and 5 (inclusive).
 *
 * @author FLC Development Team
 */
public class InvalidRatingException extends Exception {

    /**
     * Constructs an InvalidRatingException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidRatingException(String message) {
        super(message);
    }
}
