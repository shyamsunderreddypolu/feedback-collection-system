package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CreateFeedbackAssignmentDto;
import com.feedback.feedbacksystem.dto.FeedbackAssignmentResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.DuplicateResourceException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import com.feedback.feedbacksystem.security.service.CustomUserDetailsService;
import com.feedback.feedbacksystem.service.FeedbackAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
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

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

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
    }

    @Test
    @DisplayName("missing target attributes returns 400")
    void assignFormValidatesPayload() throws Exception {
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "departmentId": 2,
                                  "semester": 5,
                                  "batch": "2023-2027",
                                  "academicYear": "2026-2027"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.section").exists());
    }

    @Test
    @DisplayName("academic year in short format returns 400")
    void assignFormRejectsShortAcademicYear() throws Exception {
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "departmentId": 2,
                                  "semester": 5,
                                  "section": "A",
                                  "batch": "2023-2027",
                                  "academicYear": "2026-27"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.academicYear").exists());
    }

    @Test
    @DisplayName("batch with two digit years returns 400")
    void assignFormRejectsShortBatch() throws Exception {
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "departmentId": 2,
                                  "semester": 5,
                                  "section": "A",
                                  "batch": "23-27",
                                  "academicYear": "2026-2027"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.batch").exists());
    }

    @Test
    @DisplayName("unknown form or department returns 404")
    void assignFormReturnsNotFound() throws Exception {
        when(feedbackAssignmentService.assignForm(any()))
                .thenThrow(new ResourceNotFoundException("Form", "id", 999L));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Form not found with id: '999'"));
    }

    @Test
    @DisplayName("assigning a published form returns 400")
    void assignFormRejectsNonDraftForm() throws Exception {
        when(feedbackAssignmentService.assignForm(any()))
                .thenThrow(new BusinessRuleViolationException("Assignments can only be made while the form is a draft."));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Assignments can only be made while the form is a draft."));
    }

    @Test
    @DisplayName("assigning the same target twice returns 409")
    void assignFormRejectsDuplicateTarget() throws Exception {
        when(feedbackAssignmentService.assignForm(any()))
                .thenThrow(new DuplicateResourceException("Target audience is already assigned to this feedback form."));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Target audience is already assigned to this feedback form."));
    }

    private FeedbackAssignmentResponseDto assignmentDto(Long id) {
        return FeedbackAssignmentResponseDto.builder()
                .id(id)
                .formId(1L)
                .departmentId(2L)
                .courseId(1L)
                .semester(5)
                .section("A")
                .batch("2023-2027")
                .academicYear("2026-2027")
                .build();
    }

    private String validPayload() {
        return """
                {
                  "formId": 1,
                  "departmentId": 2,
                  "semester": 5,
                  "section": "A",
                  "batch": "2023-2027",
                  "academicYear": "2026-2027"
                }
                """;
    }
}
