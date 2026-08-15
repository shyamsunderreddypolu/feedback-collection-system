package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CreateFeedbackFormRequestDto;
import com.feedback.feedbacksystem.dto.FeedbackFormResponseDto;

import java.util.List;

/**
 * Feedback form creation and publishing lifecycle.
 */
public interface FeedbackFormService {

    /**
     * Creates a form in DRAFT status owned by the given creator.
     *
     * @throws com.feedback.feedbacksystem.exception.ResourceNotFoundException      if the creator does not exist
     * @throws com.feedback.feedbacksystem.exception.BusinessRuleViolationException if endDate is not after startDate
     */
    FeedbackFormResponseDto createForm(CreateFeedbackFormRequestDto request, Long creatorId);

    /**
     * @throws com.feedback.feedbacksystem.exception.ResourceNotFoundException if the form does not exist or is deleted
     */
    FeedbackFormResponseDto getFormById(Long formId);

    /**
     * All forms currently in ACTIVE status, excluding soft deleted ones.
     */
    List<FeedbackFormResponseDto> getActiveForms();

    /**
     * Moves a form from DRAFT to ACTIVE.
     *
     * @throws com.feedback.feedbacksystem.exception.ResourceNotFoundException      if the form does not exist or is deleted
     * @throws com.feedback.feedbacksystem.exception.BusinessRuleViolationException if the form is not DRAFT or has no questions
     */
    void publishForm(Long formId);
}
