package com.feedback.feedbacksystem.exception;

/**
 * Thrown when a request is well formed but breaks a survey engine rule,
 * e.g. publishing a form that has no questions.
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
