package com.feedback.feedbacksystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileDto {
    private Long id;
    private Long userId;
    private String rollNumber;
    private String academicYear;
    private int semester;
    private String section;
    private String batch;
}
