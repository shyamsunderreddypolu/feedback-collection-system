package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.ApiMessageResponse;
import com.feedback.feedbacksystem.dto.CreateFeedbackFormRequestDto;
import com.feedback.feedbacksystem.dto.FeedbackFormResponseDto;
import com.feedback.feedbacksystem.security.service.UserPrincipal;
import com.feedback.feedbacksystem.service.FeedbackFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feedback form endpoints for the survey builder.
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FeedbackFormController {

    private final FeedbackFormService feedbackFormService;

    /**
     * Creates a form in DRAFT status.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeedbackFormResponseDto> createForm(
            @Valid @RequestBody CreateFeedbackFormRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long creatorId = (principal != null) ? principal.getId() : 1L;
        FeedbackFormResponseDto created = feedbackFormService.createForm(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Forms open for responses right now.
     */
    @GetMapping("/active")
    public List<FeedbackFormResponseDto> getActiveForms() {
        return feedbackFormService.getActiveForms();
    }

    @GetMapping("/{id}")
    public FeedbackFormResponseDto getFormById(@PathVariable Long id) {
        return feedbackFormService.getFormById(id);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiMessageResponse publishForm(@PathVariable Long id) {
        feedbackFormService.publishForm(id);
        return ApiMessageResponse.of(id, "Feedback form published successfully.");
    }
}
