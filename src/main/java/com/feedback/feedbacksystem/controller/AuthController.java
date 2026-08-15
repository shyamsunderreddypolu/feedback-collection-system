package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.AuthResponseDto;
import com.feedback.feedbacksystem.dto.LoginRequestDto;
import com.feedback.feedbacksystem.dto.RegisterUserRequestDto;
import com.feedback.feedbacksystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        AuthResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody RegisterUserRequestDto registerUserRequestDto) {
        AuthResponseDto response = authService.registerUser(registerUserRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
