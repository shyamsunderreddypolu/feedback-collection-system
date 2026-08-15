package com.feedback.feedbacksystem.dto;

import com.feedback.feedbacksystem.model.FormStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackFormResponseDto {

    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private FormStatus status;
    private String creatorName;
    private long totalQuestions;
    private long totalAssignments;
}
