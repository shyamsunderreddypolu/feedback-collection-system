package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.ApiMessageResponse;
import com.feedback.feedbacksystem.dto.CreateFeedbackAssignmentDto;
import com.feedback.feedbacksystem.dto.FeedbackAssignmentResponseDto;
import com.feedback.feedbacksystem.service.FeedbackAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Targeting endpoints: which students a feedback form is aimed at.
 *
 * <p>A target is a department, semester, section and batch, optionally narrowed to one
 * course. Assigning the same target twice is rejected as a conflict.
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class FeedbackAssignmentController {

    private final FeedbackAssignmentService feedbackAssignmentService;

    @PostMapping
    public ResponseEntity<ApiMessageResponse> assignForm(
            @Valid @RequestBody CreateFeedbackAssignmentDto request) {

        FeedbackAssignmentResponseDto created = feedbackAssignmentService.assignForm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiMessageResponse.of(created.getId(), "Feedback form assigned successfully."));
    }
}
