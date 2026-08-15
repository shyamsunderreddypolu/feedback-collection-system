package com.feedback.feedbacksystem.security;

import com.feedback.feedbacksystem.dto.AuthResponseDto;
import com.feedback.feedbacksystem.dto.LoginRequestDto;
import com.feedback.feedbacksystem.dto.RegisterUserRequestDto;
import com.feedback.feedbacksystem.model.Department;
import com.feedback.feedbacksystem.model.Role;
import com.feedback.feedbacksystem.repository.DepartmentRepository;
import com.feedback.feedbacksystem.repository.RoleRepository;
import com.feedback.feedbacksystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder().name("Computer Science").code("CSE").build()
                ));

        roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_FACULTY").build()));

        roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
    }

    @Test
    void testRegisterUserAndLoginSuccess() {
        RegisterUserRequestDto registerRequest = RegisterUserRequestDto.builder()
                .name("Alex Student")
                .email("alex.student@college.edu")
                .password("securePassword123")
                .departmentCode("CSE")
                .roleName("ROLE_STUDENT")
                .build();

        AuthResponseDto registerResponse = authService.registerUser(registerRequest);

        assertThat(registerResponse).isNotNull();
        assertThat(registerResponse.getToken()).isNotBlank();
        assertThat(registerResponse.getEmail()).isEqualTo("alex.student@college.edu");
        assertThat(registerResponse.getRole()).isEqualTo("ROLE_STUDENT");
        assertThat(registerResponse.getDepartmentCode()).isEqualTo("CSE");

        // Now test Login
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email("alex.student@college.edu")
                .password("securePassword123")
                .build();

        AuthResponseDto loginResponse = authService.login(loginRequest);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(loginResponse.getEmail()).isEqualTo("alex.student@college.edu");
    }

    @Test
    void testLoginWithInvalidPasswordThrowsException() {
        RegisterUserRequestDto registerRequest = RegisterUserRequestDto.builder()
                .name("Sam Student")
                .email("sam.student@college.edu")
                .password("correctPassword")
                .departmentCode("CSE")
                .roleName("ROLE_STUDENT")
                .build();

        authService.registerUser(registerRequest);

        LoginRequestDto invalidLogin = LoginRequestDto.builder()
                .email("sam.student@college.edu")
                .password("wrongPassword")
                .build();

        assertThatThrownBy(() -> authService.login(invalidLogin))
                .isInstanceOf(BadCredentialsException.class);
    }
}
