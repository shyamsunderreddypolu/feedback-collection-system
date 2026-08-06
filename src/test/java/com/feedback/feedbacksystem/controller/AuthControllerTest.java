package com.feedback.feedbacksystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.feedbacksystem.dto.AuthResponseDto;
import com.feedback.feedbacksystem.dto.LoginRequestDto;
import com.feedback.feedbacksystem.dto.RegisterUserRequestDto;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import com.feedback.feedbacksystem.security.service.CustomUserDetailsService;
import com.feedback.feedbacksystem.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loginReturnsTokenAndUserDetails() throws Exception {
        LoginRequestDto request = new LoginRequestDto("admin@test.com", "password123");
        AuthResponseDto response = AuthResponseDto.builder()
                .token("mock-jwt-token")
                .id(1L)
                .name("Admin User")
                .userEmail("admin@test.com")
                .role("ROLE_ADMIN")
                .build();

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void registerUserReturnsCreated() throws Exception {
        RegisterUserRequestDto request = RegisterUserRequestDto.builder()
                .name("New User")
                .email("new@test.com")
                .password("password123")
                .departmentCode("CSE")
                .roleName("ROLE_STUDENT")
                .build();

        AuthResponseDto response = AuthResponseDto.builder()
                .token("mock-jwt-token")
                .id(2L)
                .name("New User")
                .userEmail("new@test.com")
                .role("ROLE_STUDENT")
                .build();

        when(authService.registerUser(any(RegisterUserRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.userEmail").value("new@test.com"));
    }
}
