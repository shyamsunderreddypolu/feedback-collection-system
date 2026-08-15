package com.feedback.feedbacksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentPerformanceDto {

    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private long totalStudents;
    private long totalResponses;
    private Double completionRate;
    private Double averageRating;
}
