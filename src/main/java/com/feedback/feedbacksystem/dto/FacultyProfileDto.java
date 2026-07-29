package com.feedback.feedbacksystem.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyProfileDto {
    private Long id;
    private Long userId;
    private String employeeId;
    private String designation;
    private LocalDate joiningDate;
}
