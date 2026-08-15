package com.feedback.feedbacksystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feedback.feedbacksystem.model.QuestionType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponseDto {

    private Long id;
    private String questionText;
    private QuestionType questionType;
    /** Named mandatory rather than isMandatory for the reason given on CreateQuestionRequestDto. */
    @JsonProperty("required")
    private boolean mandatory;
    private int displayOrder;
    private List<QuestionOptionResponseDto> options;
}
