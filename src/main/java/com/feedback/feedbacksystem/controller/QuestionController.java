package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.ApiMessageResponse;
import com.feedback.feedbacksystem.dto.CreateQuestionRequestDto;
import com.feedback.feedbacksystem.dto.QuestionResponseDto;
import com.feedback.feedbacksystem.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Question and option endpoints for the survey builder.
 *
 * <p>Questions belong to a form and may only be added while that form is still a draft;
 * both rules live in {@link QuestionService} rather than here.
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<ApiMessageResponse> addQuestion(
            @Valid @RequestBody CreateQuestionRequestDto request) {

        QuestionResponseDto created = questionService.addQuestionToForm(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiMessageResponse.of(created.getId(), "Question added successfully."));
    }

    /** Questions of a form in display order, each with its options. */
    @GetMapping("/form/{formId}")
    public List<QuestionResponseDto> getQuestionsByForm(@PathVariable Long formId) {
        return questionService.getQuestionsByFormId(formId);
    }
}
