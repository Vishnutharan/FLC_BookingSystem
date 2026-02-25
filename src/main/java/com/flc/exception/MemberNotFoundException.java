package com.flc.exception;

/**
 * Exception thrown when a member lookup fails to find a matching member.
 *
 * @author FLC Development Team
 */
public class MemberNotFoundException extends Exception {

    /**
     * Constructs a MemberNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public MemberNotFoundException(String message) {
        super(message);
    }
}
