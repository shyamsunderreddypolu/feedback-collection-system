package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.FeedbackSubmissionResponseDto;
import com.feedback.feedbacksystem.dto.SubmitFeedbackResponseDto;
import com.feedback.feedbacksystem.service.FeedbackSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class FeedbackSubmissionController {

    private final FeedbackSubmissionService feedbackSubmissionService;

    @PostMapping
    public ResponseEntity<FeedbackSubmissionResponseDto> submitFeedback(
            @Valid @RequestBody SubmitFeedbackResponseDto request,
            @RequestParam Long submitterId) {
        FeedbackSubmissionResponseDto response = feedbackSubmissionService.submitFeedback(request, submitterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/assignment/{assignmentId}/status")
    public ResponseEntity<Map<String, Object>> checkAssignmentSubmissionStatus(
            @PathVariable Long assignmentId,
            @RequestParam Long studentId) {
        boolean submitted = feedbackSubmissionService.hasStudentSubmittedForAssignment(assignmentId, studentId);
        return ResponseEntity.ok(Map.of("submitted", submitted, "assignmentId", assignmentId, "studentId", studentId));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<FeedbackSubmissionResponseDto>> getStudentHistory(@RequestParam Long studentId) {
        List<FeedbackSubmissionResponseDto> history = feedbackSubmissionService.getStudentSubmissionHistory(studentId);
        return ResponseEntity.ok(history);
    }
}
