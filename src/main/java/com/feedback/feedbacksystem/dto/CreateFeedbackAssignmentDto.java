package com.feedback.feedbacksystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFeedbackAssignmentDto {

    @NotNull
    @JsonProperty("formId")
    private Long feedbackFormId;

    @NotNull
    private Long departmentId;

    private Long courseId;

    @Min(1)
    @Max(8)
    private int semester;

    private String section;

    private String batch;

    private String academicYear;
}
