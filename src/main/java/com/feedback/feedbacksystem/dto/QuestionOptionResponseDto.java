package com.feedback.feedbacksystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionResponseDto {

    private Long id;
    private String optionValue;
    private int displayOrder;
    private boolean isActive;
}
