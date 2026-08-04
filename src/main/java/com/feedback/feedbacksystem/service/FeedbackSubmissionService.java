package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.FeedbackSubmissionResponseDto;
import com.feedback.feedbacksystem.dto.SubmitFeedbackResponseDto;

import java.util.List;

public interface FeedbackSubmissionService {

    FeedbackSubmissionResponseDto submitFeedback(SubmitFeedbackResponseDto request, Long submitterId);

    boolean hasStudentSubmitted(Long formId, Long studentId);

    boolean hasStudentSubmittedForAssignment(Long assignmentId, Long studentId);

    List<FeedbackSubmissionResponseDto> getStudentSubmissionHistory(Long studentId);
}
