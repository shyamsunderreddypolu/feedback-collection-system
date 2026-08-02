package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CreateFeedbackAssignmentDto;
import com.feedback.feedbacksystem.dto.FeedbackAssignmentResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.DuplicateResourceException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.service.FeedbackAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedbackAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class FeedbackAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedbackAssignmentService feedbackAssignmentService;

    @Test
    @DisplayName("POST /api/assignments targets a form at an audience")
    void assignFormReturnsCreated() throws Exception {
        when(feedbackAssignmentService.assignForm(any(CreateFeedbackAssignmentDto.class)))
                .thenReturn(assignmentDto(3L));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "departmentId": 2,
                                  "courseId": 1,
                                  "semester": 5,
                                  "section": "A",
                                  "batch": "2023-2027",
                                  "academicYear": "2026-2027"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.message").value("Feedback form assigned successfully."));

        ArgumentCaptor<CreateFeedbackAssignmentDto> captor =
                ArgumentCaptor.forClass(CreateFeedbackAssignmentDto.class);
        verify(feedbackAssignmentService).assignForm(captor.capture());

        assertThat(captor.getValue().getFeedbackFormId()).isEqualTo(1L);
        assertThat(captor.getValue().getDepartmentId()).isEqualTo(2L);
        assertThat(captor.getValue().getSemester()).isEqualTo(5);
        assertThat(captor.getValue().getSection()).isEqualTo("A");
    }

    @Test
    @DisplayName("POST /api/assignments allows a department wide target with no course")
    void assignFormAllowsNullCourse() throws Exception {
        when(feedbackAssignmentService.assignForm(any(CreateFeedbackAssignmentDto.class)))
                .thenReturn(assignmentDto(4L));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"departmentId":2,"semester":5,"section":"A",
                                 "batch":"2023-2027","academicYear":"2026-2027"}
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateFeedbackAssignmentDto> captor =
                ArgumentCaptor.forClass(CreateFeedbackAssignmentDto.class);
        verify(feedbackAssignmentService).assignForm(captor.capture());
        assertThat(captor.getValue().getCourseId()).isNull();
    }

    @Test
    @DisplayName("POST /api/assignments rejects a body with no form or department")
    void assignFormRequiresFormAndDepartment() throws Exception {
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"semester":5,"section":"A","batch":"2023-2027"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.feedbackFormId").exists())
                .andExpect(jsonPath("$.fieldErrors.departmentId").exists());
    }

    @Test
    @DisplayName("POST /api/assignments rejects a semester outside the allowed range")
    void assignFormRejectsSemesterOutOfRange() throws Exception {
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"departmentId":2,"semester":9,"section":"A",
                                 "batch":"2023-2027","academicYear":"2026-2027"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.semester").exists());
    }

    @Test
    @DisplayName("POST /api/assignments surfaces a missing targeting field as 400")
    void assignFormRejectsMissingTargetingField() throws Exception {
        when(feedbackAssignmentService.assignForm(any(CreateFeedbackAssignmentDto.class)))
                .thenThrow(new BusinessRuleViolationException(
                        "academicYear is required to target an assignment"));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"departmentId":2,"semester":5,"section":"A","batch":"2023-2027"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("academicYear is required to target an assignment"));
    }

    @Test
    @DisplayName("POST /api/assignments returns 404 when the form or department is unknown")
    void assignFormReturnsNotFound() throws Exception {
        when(feedbackAssignmentService.assignForm(any(CreateFeedbackAssignmentDto.class)))
                .thenThrow(new ResourceNotFoundException("Department", 99L));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"departmentId":99,"semester":5,"section":"A",
                                 "batch":"2023-2027","academicYear":"2026-2027"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/assignments returns 409 for a target already assigned")
    void assignFormReturnsConflict() throws Exception {
        when(feedbackAssignmentService.assignForm(any(CreateFeedbackAssignmentDto.class)))
                .thenThrow(new DuplicateResourceException("Form 1 is already assigned to that target"));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"departmentId":2,"semester":5,"section":"A",
                                 "batch":"2023-2027","academicYear":"2026-2027"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Form 1 is already assigned to that target"));
    }

    private FeedbackAssignmentResponseDto assignmentDto(Long id) {
        return FeedbackAssignmentResponseDto.builder()
                .id(id)
                .feedbackFormId(1L)
                .formTitle("Faculty Feedback - CSE")
                .departmentName("Computer Science and Engineering")
                .semester(5)
                .section("A")
                .batch("2023-2027")
                .academicYear("2026-2027")
                .build();
    }
}
