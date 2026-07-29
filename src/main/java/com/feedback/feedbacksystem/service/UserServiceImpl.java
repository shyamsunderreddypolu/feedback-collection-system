package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.UserResponseDto;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", userId);
        }
        return toUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.isActive() && !user.isDeleted())
                .map(this::toUserResponseDto)
                .toList();
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", userId);
        }

        user.setActive(false);
        user.setDeleted(true);
        userRepository.save(user);
    }

    private UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getCode() : null)
                .active(user.isActive())
                .build();
    }
}
