package com.feedback.feedbacksystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackAssignmentResponseDto {

    private Long id;
    private Long feedbackFormId;
    private String formTitle;
    private String departmentName;
    private String courseName;
    private String courseCode;
    private int semester;
    private String section;
    private String batch;
    private String academicYear;
}
