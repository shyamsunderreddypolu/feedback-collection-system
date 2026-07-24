package com.feedback.feedbacksystem.dto;

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
    private boolean isMandatory;
    private int displayOrder;
    private List<QuestionOptionResponseDto> options;
}
