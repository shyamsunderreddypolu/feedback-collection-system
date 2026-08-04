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
public class FacultyAnalyticsDto {

    private Long facultyId;
    private String facultyName;
    private String employeeId;
    private String designation;
    private long totalCoursesTaught;
    private long totalResponsesReceived;
    private Double overallAverageRating;
    private List<CourseAnalyticsDto> courseSummaries;
}
