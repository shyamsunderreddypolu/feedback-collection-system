package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.FeedbackSubmissionResponseDto;
import com.feedback.feedbacksystem.dto.SubmitFeedbackResponseDto;

public interface FeedbackSubmissionService {

    FeedbackSubmissionResponseDto submitFeedback(SubmitFeedbackResponseDto request, Long submitterId);

    boolean hasStudentSubmitted(Long formId, Long studentId);
}
