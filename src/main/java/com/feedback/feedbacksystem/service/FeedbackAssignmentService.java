package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CreateFeedbackAssignmentDto;
import com.feedback.feedbacksystem.dto.FeedbackAssignmentResponseDto;

/**
 * Targeting of feedback forms at a department, semester, section and batch.
 */
public interface FeedbackAssignmentService {

    /**
     * Assigns a form to one target audience. A null courseId means the assignment
     * is a department wide survey rather than a course specific one.
     *
     * @throws com.feedback.feedbacksystem.exception.ResourceNotFoundException      if the form, department or course does not exist
     * @throws com.feedback.feedbacksystem.exception.BusinessRuleViolationException if a required targeting field is missing
     * @throws com.feedback.feedbacksystem.exception.DuplicateResourceException     if the same target has already been assigned
     */
    FeedbackAssignmentResponseDto assignForm(CreateFeedbackAssignmentDto request);
}
