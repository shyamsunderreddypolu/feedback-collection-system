package com.feedback.feedbacksystem.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateCourseRequestDtoValidation() {
        CreateCourseRequestDto valid = CreateCourseRequestDto.builder()
                .name("Database Systems")
                .code("CS201")
                .departmentId(1L)
                .build();
        assertThat(validator.validate(valid)).isEmpty();

        CreateCourseRequestDto invalid = CreateCourseRequestDto.builder()
                .name("")
                .code("   ")
                .departmentId(null)
                .build();
        Set<ConstraintViolation<CreateCourseRequestDto>> violations = validator.validate(invalid);
        assertThat(violations).hasSize(3);
    }

    @Test
    void testCourseResponseDto() {
        CourseResponseDto dto = CourseResponseDto.builder()
                .id(10L)
                .name("Data Structures")
                .code("CS102")
                .departmentName("Computer Science")
                .departmentCode("CSE")
                .active(true)
                .build();

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Data Structures");
        assertThat(dto.getCode()).isEqualTo("CS102");
        assertThat(dto.getDepartmentName()).isEqualTo("Computer Science");
        assertThat(dto.getDepartmentCode()).isEqualTo("CSE");
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void testCreateCourseAssignmentDtoValidation() {
        CreateCourseAssignmentDto valid = CreateCourseAssignmentDto.builder()
                .courseId(1L)
                .facultyId(2L)
                .academicYear("2025-2026")
                .semester(5)
                .section("A")
                .build();
        assertThat(validator.validate(valid)).isEmpty();

        CreateCourseAssignmentDto invalid = CreateCourseAssignmentDto.builder()
                .courseId(null)
                .facultyId(null)
                .academicYear("2025/2026") // Invalid pattern
                .semester(9) // Exceeds max 8
                .section("")
                .build();
        Set<ConstraintViolation<CreateCourseAssignmentDto>> violations = validator.validate(invalid);
        assertThat(violations).hasSize(5);
    }

    @Test
    void testCourseAssignmentResponseDto() {
        CourseAssignmentResponseDto dto = CourseAssignmentResponseDto.builder()
                .id(1L)
                .courseId(2L)
                .courseName("Operating Systems")
                .courseCode("CS301")
                .facultyId(3L)
                .facultyName("Dr. Alan Turing")
                .academicYear("2025-2026")
                .semester(4)
                .section("B")
                .status("ACTIVE")
                .build();

        assertThat(dto.getCourseName()).isEqualTo("Operating Systems");
        assertThat(dto.getFacultyName()).isEqualTo("Dr. Alan Turing");
    }

    @Test
    void testSubmitAnswerDtoValidation() {
        SubmitAnswerDto valid = SubmitAnswerDto.builder()
                .questionId(10L)
                .ratingValue(5)
                .textValue("Great course!")
                .selectedOptionId(2L)
                .build();
        assertThat(validator.validate(valid)).isEmpty();

        SubmitAnswerDto invalid = SubmitAnswerDto.builder()
                .questionId(null)
                .ratingValue(6) // Exceeds max 5
                .build();
        Set<ConstraintViolation<SubmitAnswerDto>> violations = validator.validate(invalid);
        assertThat(violations).hasSize(2);
    }

    @Test
    void testSubmitFeedbackResponseDtoValidation() {
        SubmitAnswerDto answer = SubmitAnswerDto.builder()
                .questionId(1L)
                .ratingValue(4)
                .build();

        SubmitFeedbackResponseDto valid = SubmitFeedbackResponseDto.builder()
                .feedbackFormId(100L)
                .answers(List.of(answer))
                .build();
        assertThat(validator.validate(valid)).isEmpty();

        SubmitFeedbackResponseDto invalid = SubmitFeedbackResponseDto.builder()
                .feedbackFormId(null)
                .answers(Collections.emptyList())
                .build();
        Set<ConstraintViolation<SubmitFeedbackResponseDto>> violations = validator.validate(invalid);
        assertThat(violations).hasSize(2);
    }

    @Test
    void testFeedbackSubmissionResponseDto() {
        LocalDateTime now = LocalDateTime.now();
        FeedbackSubmissionResponseDto dto = FeedbackSubmissionResponseDto.builder()
                .responseId(50L)
                .feedbackFormId(100L)
                .formTitle("Mid-Semester Feedback")
                .submitterName("Jane Doe")
                .submittedAt(now)
                .totalAnswersCount(5)
                .build();

        assertThat(dto.getResponseId()).isEqualTo(50L);
        assertThat(dto.getFormTitle()).isEqualTo("Mid-Semester Feedback");
        assertThat(dto.getSubmittedAt()).isEqualTo(now);
        assertThat(dto.getTotalAnswersCount()).isEqualTo(5);
    }
}
