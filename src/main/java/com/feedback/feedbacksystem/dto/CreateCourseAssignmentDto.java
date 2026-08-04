package com.feedback.feedbacksystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCourseAssignmentDto {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Faculty ID is required")
    private Long facultyId;

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "Academic year must follow YYYY-YYYY format")
    private String academicYear;

    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot exceed 8")
    private Integer semester;

    @NotBlank(message = "Section is required")
    private String section;
}
