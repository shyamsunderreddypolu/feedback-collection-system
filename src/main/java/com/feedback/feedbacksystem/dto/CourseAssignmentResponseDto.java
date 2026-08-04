package com.feedback.feedbacksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAssignmentResponseDto {

    private Long id;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Long facultyId;
    private String facultyName;
    private String academicYear;
    private Integer semester;
    private String section;
    private String status;
}
