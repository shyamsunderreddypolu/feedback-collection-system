package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.FeedbackSubmissionResponseDto;
import com.feedback.feedbacksystem.dto.SubmitFeedbackResponseDto;
import com.feedback.feedbacksystem.service.FeedbackSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback-submissions")
@RequiredArgsConstructor
public class FeedbackSubmissionController {

    private final FeedbackSubmissionService feedbackSubmissionService;

    @PostMapping("/submit")
    public ResponseEntity<FeedbackSubmissionResponseDto> submitFeedback(
            @Valid @RequestBody SubmitFeedbackResponseDto request,
            @RequestParam Long submitterId) {
        FeedbackSubmissionResponseDto response = feedbackSubmissionService.submitFeedback(request, submitterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> hasStudentSubmitted(@RequestParam Long formId, @RequestParam Long studentId) {
        boolean submitted = feedbackSubmissionService.hasStudentSubmitted(formId, studentId);
        return ResponseEntity.ok(submitted);
    }
}
