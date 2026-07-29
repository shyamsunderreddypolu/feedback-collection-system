package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.FacultyProfileDto;
import com.feedback.feedbacksystem.dto.StudentProfileDto;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.FacultyProfile;
import com.feedback.feedbacksystem.model.StudentProfile;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.FacultyProfileRepository;
import com.feedback.feedbacksystem.repository.StudentProfileRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private FacultyProfileRepository facultyProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private User testUser;
    private StudentProfile studentProfile;
    private FacultyProfile facultyProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Test User").email("test@college.edu").build();

        studentProfile = StudentProfile.builder()
                .id(100L)
                .user(testUser)
                .rollNumber("21CS001")
                .academicYear("2025-2026")
                .semester(5)
                .section("A")
                .batch("2021-2025")
                .build();

        facultyProfile = FacultyProfile.builder()
                .id(200L)
                .user(testUser)
                .employeeId("FAC101")
                .designation("Assistant Professor")
                .joiningDate(LocalDate.of(2020, 8, 1))
                .build();
    }

    @Test
    void getStudentProfileByUserIdReturnsDto() {
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));

        StudentProfileDto dto = profileService.getStudentProfileByUserId(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getRollNumber()).isEqualTo("21CS001");
        assertThat(dto.getSemester()).isEqualTo(5);
    }

    @Test
    void getFacultyProfileByUserIdReturnsDto() {
        when(facultyProfileRepository.findByUserId(1L)).thenReturn(Optional.of(facultyProfile));

        FacultyProfileDto dto = profileService.getFacultyProfileByUserId(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getEmployeeId()).isEqualTo("FAC101");
        assertThat(dto.getDesignation()).isEqualTo("Assistant Professor");
    }

    @Test
    void updateStudentProfileUpdatesAndSaves() {
        StudentProfileDto updateDto = StudentProfileDto.builder()
                .rollNumber("21CS002")
                .semester(6)
                .section("B")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(studentProfile));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProfileDto result = profileService.updateStudentProfile(1L, updateDto);

        assertThat(result.getRollNumber()).isEqualTo("21CS002");
        assertThat(result.getSemester()).isEqualTo(6);
        assertThat(result.getSection()).isEqualTo("B");
    }

    @Test
    void getProfileThrowsExceptionWhenNotFound() {
        when(studentProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getStudentProfileByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
