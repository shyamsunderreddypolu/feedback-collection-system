package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto getUserById(Long userId);
    List<UserResponseDto> getAllActiveUsers();
    void deactivateUser(Long userId);
}
