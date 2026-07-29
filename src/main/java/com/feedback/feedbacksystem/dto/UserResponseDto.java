package com.feedback.feedbacksystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String roleName;
    private String departmentName;
    private String departmentCode;
    private boolean active;
}
