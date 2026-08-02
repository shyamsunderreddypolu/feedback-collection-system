package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.ApiMessageResponse;
import com.feedback.feedbacksystem.dto.CreateFeedbackFormRequestDto;
import com.feedback.feedbacksystem.dto.FeedbackFormResponseDto;
import com.feedback.feedbacksystem.service.FeedbackFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feedback form endpoints for the survey builder.
 *
 * <p>Handlers stay thin on purpose: validate, delegate to the service, map the result to a
 * status code. Every business rule and every failure lives in {@link FeedbackFormService},
 * and the exceptions it throws are translated by the global exception handler.
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FeedbackFormController {

    private final FeedbackFormService feedbackFormService;

    /**
     * Creates a form in DRAFT status.
     *
     * <p>TODO: the creator is read from a header until the authentication module lands,
     * after which it should come from the authenticated principal.
     */
    @PostMapping
    public ResponseEntity<FeedbackFormResponseDto> createForm(
            @Valid @RequestBody CreateFeedbackFormRequestDto request,
            @RequestHeader("X-User-Id") Long creatorId) {

        FeedbackFormResponseDto created = feedbackFormService.createForm(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Forms open for responses right now. Declared before the {@code /{id}} mapping for
     * readability; Spring already prefers the literal path over the template.
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
    public ApiMessageResponse publishForm(@PathVariable Long id) {
        feedbackFormService.publishForm(id);
        return ApiMessageResponse.of(id, "Feedback form published successfully.");
    }
}
