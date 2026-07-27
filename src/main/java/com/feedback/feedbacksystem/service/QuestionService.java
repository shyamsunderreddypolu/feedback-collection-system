package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CreateQuestionRequestDto;
import com.feedback.feedbacksystem.dto.QuestionResponseDto;

import java.util.List;

/**
 * Question and answer-option management for a feedback form.
 */
public interface QuestionService {

    /**
     * Adds a question (and its options, for choice based types) to a DRAFT form.
     *
     * @throws com.feedback.feedbacksystem.exception.ResourceNotFoundException      if the form does not exist or is deleted
     * @throws com.feedback.feedbacksystem.exception.BusinessRuleViolationException if the form is no longer DRAFT, or the
     *                                                                             options supplied do not match the question type
     * @throws com.feedback.feedbacksystem.exception.DuplicateResourceException     if displayOrder is already taken on the form
     */
    QuestionResponseDto addQuestionToForm(CreateQuestionRequestDto request);

    /**
     * Questions of a form in display order, each with its active and inactive options.
     *
     * @throws com.feedback.feedbacksystem.exception.ResourceNotFoundException if the form does not exist or is deleted
     */
    List<QuestionResponseDto> getQuestionsByFormId(Long formId);
}
