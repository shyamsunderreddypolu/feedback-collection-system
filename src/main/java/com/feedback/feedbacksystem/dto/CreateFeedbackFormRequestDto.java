package com.feedback.feedbacksystem.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.feedback.feedbacksystem.config.FlexibleLocalDateTimeDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFeedbackFormRequestDto {

    @NotBlank
    private String title;

    private String description;

    /** Optional; the entity falls back to GENERAL when this is left out. */
    private String category;

    @NotNull
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @NotNull
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime endDate;
}
