package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.UserResponseDto;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import com.feedback.feedbacksystem.security.service.CustomUserDetailsService;
import com.feedback.feedbacksystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getUserByIdReturnsUser() throws Exception {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .roleName("ROLE_STUDENT")
                .active(true)
                .build();

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getAllActiveUsersReturnsList() throws Exception {
        UserResponseDto u1 = UserResponseDto.builder().id(1L).email("u1@test.com").active(true).build();
        UserResponseDto u2 = UserResponseDto.builder().id(2L).email("u2@test.com").active(true).build();

        when(userService.getAllActiveUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUsersByRoleReturnsList() throws Exception {
        UserResponseDto faculty = UserResponseDto.builder().id(3L).email("faculty@test.com").roleName("ROLE_FACULTY").build();

        when(userService.getUsersByRole("ROLE_FACULTY")).thenReturn(List.of(faculty));

        mockMvc.perform(get("/api/users/role/ROLE_FACULTY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleName").value("ROLE_FACULTY"));
    }

    @Test
    void deactivateUserReturnsOk() throws Exception {
        doNothing().when(userService).deactivateUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));
    }
}
