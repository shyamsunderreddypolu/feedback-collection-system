package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.UserResponseDto;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.Department;
import com.feedback.feedbacksystem.model.Role;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder().id(1L).name("Computer Science").code("CSE").build();
        Role role = Role.builder().id(1).name("ROLE_STUDENT").build();

        testUser = User.builder()
                .id(10L)
                .name("John Doe")
                .email("john@college.edu")
                .department(dept)
                .role(role)
                .active(true)
                .isDeleted(false)
                .build();
    }

    @Test
    void getUserByIdReturnsUserDto() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

        UserResponseDto dto = userService.getUserById(10L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@college.edu");
        assertThat(dto.getRoleName()).isEqualTo("ROLE_STUDENT");
        assertThat(dto.getDepartmentCode()).isEqualTo("CSE");
    }

    @Test
    void getUserByIdThrowsResourceNotFoundExceptionWhenDeletedOrNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllActiveUsersReturnsFilteredList() {
        User inactiveUser = User.builder().id(11L).name("Jane").active(false).isDeleted(false).build();
        User deletedUser = User.builder().id(12L).name("Bob").active(true).isDeleted(true).build();

        when(userRepository.findAll()).thenReturn(List.of(testUser, inactiveUser, deletedUser));

        List<UserResponseDto> activeUsers = userService.getAllActiveUsers();

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void deactivateUserSetsActiveFalseAndDeletedTrue() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

        userService.deactivateUser(10L);

        assertThat(testUser.isActive()).isFalse();
        assertThat(testUser.isDeleted()).isTrue();
        verify(userRepository, times(1)).save(testUser);
    }
}
