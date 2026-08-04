package com.feedback.feedbacksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRatingSummaryDto {

    private Long questionId;
    private String questionText;
    private Double averageRating;
    private long totalRatings;
}
