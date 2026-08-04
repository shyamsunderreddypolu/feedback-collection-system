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
public class CourseAnalyticsDto {

    private Long courseId;
    private String courseName;
    private String courseCode;
    private long totalFormsAssigned;
    private long totalResponses;
    private Double averageCourseRating;
    private List<FormAnalyticsSummaryDto> formSummaries;
}
