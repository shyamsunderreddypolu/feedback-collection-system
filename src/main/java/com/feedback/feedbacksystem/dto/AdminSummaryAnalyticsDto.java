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
public class AdminSummaryAnalyticsDto {

    private long totalForms;
    private long totalActiveForms;
    private long totalResponses;
    private long totalStudents;
    private Double overallCompletionRate;
    private Double overallCollegeRating;
    private List<DepartmentPerformanceDto> departmentPerformanceList;
}
