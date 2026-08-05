package com.feedback.feedbacksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormAnalyticsSummaryDto {

    private Long formId;
    private String formTitle;
    private String category;
    private long totalResponses;
    private long totalTargetedStudents;
    private Double completionRate;
    private Double overallAverageRating;
    private List<QuestionRatingSummaryDto> questionRatings;
}
