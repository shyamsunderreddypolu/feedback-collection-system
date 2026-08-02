package com.feedback.feedbacksystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Acknowledgement for endpoints whose contract is a confirmation rather than a resource,
 * such as publishing a form. Keeps the message out of the resource DTOs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMessageResponse {

    private Long id;
    private String message;

    public static ApiMessageResponse of(String message) {
        return ApiMessageResponse.builder().message(message).build();
    }

    public static ApiMessageResponse of(Long id, String message) {
        return ApiMessageResponse.builder().id(id).message(message).build();
    }
}
