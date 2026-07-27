package com.feedback.feedbacksystem.exception;

/**
 * Thrown when a record would collide with one that already exists, so the
 * caller gets a readable message instead of a database constraint violation.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
