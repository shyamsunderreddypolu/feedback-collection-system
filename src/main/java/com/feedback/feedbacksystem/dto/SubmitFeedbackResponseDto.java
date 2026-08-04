package com.feedback.feedbacksystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitFeedbackResponseDto {

    @NotNull(message = "Feedback form ID is required")
    private Long feedbackFormId;

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<SubmitAnswerDto> answers;
}
