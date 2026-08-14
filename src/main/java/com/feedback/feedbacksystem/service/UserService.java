package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto getUserById(Long userId);
    List<UserResponseDto> getAllActiveUsers();
    List<UserResponseDto> getUsersByRole(String roleName);
    void deactivateUser(Long userId);
}
