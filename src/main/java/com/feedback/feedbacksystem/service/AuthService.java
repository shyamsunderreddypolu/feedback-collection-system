package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.AuthResponseDto;
import com.feedback.feedbacksystem.dto.LoginRequestDto;
import com.feedback.feedbacksystem.dto.RegisterUserRequestDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    AuthResponseDto registerUser(RegisterUserRequestDto registerUserRequestDto);
}
