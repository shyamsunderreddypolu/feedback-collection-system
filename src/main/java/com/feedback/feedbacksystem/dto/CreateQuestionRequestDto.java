package com.feedback.feedbacksystem.dto;

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
    private Long feedbackFormId;

    @NotBlank
    private String questionText;

    @NotNull
    private QuestionType questionType;

    private boolean isMandatory;

    private int displayOrder;

    private List<String> options;
}
