package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.ApiMessageResponse;
import com.feedback.feedbacksystem.dto.CreateFeedbackAssignmentDto;
import com.feedback.feedbacksystem.dto.FeedbackAssignmentResponseDto;
import com.feedback.feedbacksystem.service.FeedbackAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Targeting endpoints: which students a feedback form is aimed at.
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class FeedbackAssignmentController {

    private final FeedbackAssignmentService feedbackAssignmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiMessageResponse> assignForm(
            @Valid @RequestBody CreateFeedbackAssignmentDto request) {

        FeedbackAssignmentResponseDto created = feedbackAssignmentService.assignForm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiMessageResponse.of(created.getId(), "Feedback form assigned successfully."));
    }
}
