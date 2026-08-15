package com.feedback.feedbacksystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feedback.feedbacksystem.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionRequestDto {

    @NotNull
    @JsonProperty("formId")
    private Long feedbackFormId;

    @NotBlank
    private String questionText;

    @NotNull
    private QuestionType questionType;

    /**
     * Named {@code mandatory} rather than {@code isMandatory} so that the field and the
     * generated {@code isMandatory()} getter resolve to a single Jackson property, which
     * {@code @JsonProperty} can then expose under the contract name.
     */
    @JsonProperty("required")
    private boolean mandatory;

    private int displayOrder;

    private List<String> options;
}
