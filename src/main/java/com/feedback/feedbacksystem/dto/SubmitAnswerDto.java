package com.feedback.feedbacksystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAnswerDto {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @Min(value = 1, message = "Rating value must be at least 1")
    @Max(value = 5, message = "Rating value cannot exceed 5")
    private Integer ratingValue;

    private String textValue;

    private Long selectedOptionId;
}
