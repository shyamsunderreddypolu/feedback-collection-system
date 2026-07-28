package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.AuthResponseDto;
import com.feedback.feedbacksystem.dto.LoginRequestDto;
import com.feedback.feedbacksystem.dto.RegisterUserRequestDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.DuplicateResourceException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.Department;
import com.feedback.feedbacksystem.model.Role;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.DepartmentRepository;
import com.feedback.feedbacksystem.repository.RoleRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail().trim(),
                        loginRequestDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequestDto.getEmail().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequestDto.getEmail()));

        String token = tokenProvider.generateTokenForUser(
                user.getEmail(),
                user.getId(),
                user.getRole() != null ? user.getRole().getName() : "ROLE_STUDENT"
        );

        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : "ROLE_STUDENT")
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getCode() : null)
                .build();
    }

    @Override
    public AuthResponseDto registerUser(RegisterUserRequestDto registerUserRequestDto) {
        String email = registerUserRequestDto.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered: " + email);
        }

        String targetRoleName = StringUtils.hasText(registerUserRequestDto.getRoleName())
                ? registerUserRequestDto.getRoleName().trim()
                : "ROLE_STUDENT";

        if (!targetRoleName.startsWith("ROLE_")) {
            targetRoleName = "ROLE_" + targetRoleName.toUpperCase();
        }

        Role role = roleRepository.findByName(targetRoleName)
                .orElseThrow(() -> new BusinessRuleViolationException("Role not found: " + registerUserRequestDto.getRoleName()));

        Department department = null;
        if (StringUtils.hasText(registerUserRequestDto.getDepartmentCode())) {
            department = departmentRepository.findByCode(registerUserRequestDto.getDepartmentCode().trim())
                    .orElseThrow(() -> new BusinessRuleViolationException("Department not found with code: " + registerUserRequestDto.getDepartmentCode()));
        }

        User user = User.builder()
                .name(registerUserRequestDto.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(registerUserRequestDto.getPassword()))
                .role(role)
                .department(department)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenForUser(
                savedUser.getEmail(),
                savedUser.getId(),
                role.getName()
        );

        return AuthResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(role.getName())
                .departmentCode(department != null ? department.getCode() : null)
                .build();
    }
}
