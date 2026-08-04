package com.feedback.feedbacksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackSubmissionResponseDto {

    private Long responseId;
    private Long feedbackFormId;
    private String formTitle;
    private String submitterName;
    private LocalDateTime submittedAt;
    private Integer totalAnswersCount;
}
