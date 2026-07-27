package com.feedback.feedbacksystem.exception;

/**
 * Thrown when a referenced record does not exist (or has been soft deleted).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found with id: " + id);
    }
}
