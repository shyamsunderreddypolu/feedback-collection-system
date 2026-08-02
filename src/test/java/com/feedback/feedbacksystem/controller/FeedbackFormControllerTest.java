package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CreateFeedbackFormRequestDto;
import com.feedback.feedbacksystem.dto.FeedbackFormResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.FormStatus;
import com.feedback.feedbacksystem.service.FeedbackFormService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for {@link FeedbackFormController}.
 *
 * <p>The service is mocked, so these cover only what the controller owns: routing, request
 * binding, validation, and the status code each service failure is translated into.
 * Security filters are switched off because authentication is not part of this module yet.
 */
@WebMvcTest(FeedbackFormController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class FeedbackFormControllerTest {

    private static final String USER_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedbackFormService feedbackFormService;

    // ---------------------------------------------------------------- create

    @Test
    @DisplayName("POST /api/forms returns 201 with the created form")
    void createFormReturnsCreated() throws Exception {
        when(feedbackFormService.createForm(any(CreateFeedbackFormRequestDto.class), eq(7L)))
                .thenReturn(formDto(1L, "Faculty Feedback - CSE", FormStatus.DRAFT));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Faculty Feedback - CSE",
                                  "description": "Semester End Faculty Feedback",
                                  "startDate": "2026-08-01",
                                  "endDate": "2026-08-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Faculty Feedback - CSE"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("POST /api/forms reads a plain date as the start of that day")
    void createFormAcceptsPlainDate() throws Exception {
        when(feedbackFormService.createForm(any(CreateFeedbackFormRequestDto.class), eq(7L)))
                .thenReturn(formDto(1L, "Faculty Feedback", FormStatus.DRAFT));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Faculty Feedback","startDate":"2026-08-01","endDate":"2026-08-10T17:30:00"}
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateFeedbackFormRequestDto> captor =
                ArgumentCaptor.forClass(CreateFeedbackFormRequestDto.class);
        verify(feedbackFormService).createForm(captor.capture(), eq(7L));

        assertThat(captor.getValue().getStartDate()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0));
        assertThat(captor.getValue().getEndDate()).isEqualTo(LocalDateTime.of(2026, 8, 10, 17, 30));
    }

    @Test
    @DisplayName("POST /api/forms accepts a body with no category")
    void createFormAcceptsMissingCategory() throws Exception {
        when(feedbackFormService.createForm(any(CreateFeedbackFormRequestDto.class), eq(7L)))
                .thenReturn(formDto(1L, "Faculty Feedback", FormStatus.DRAFT));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Faculty Feedback","startDate":"2026-08-01","endDate":"2026-08-10"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/forms rejects a blank title with field level detail")
    void createFormRejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"  ","startDate":"2026-08-01","endDate":"2026-08-10"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    @DisplayName("POST /api/forms rejects missing dates")
    void createFormRejectsMissingDates() throws Exception {
        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Faculty Feedback"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.startDate").exists())
                .andExpect(jsonPath("$.fieldErrors.endDate").exists());
    }

    @Test
    @DisplayName("POST /api/forms rejects a date it cannot parse")
    void createFormRejectsUnparseableDate() throws Exception {
        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Faculty Feedback","startDate":"01-08-2026","endDate":"2026-08-10"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/forms without the creator header is a bad request")
    void createFormRequiresCreatorHeader() throws Exception {
        mockMvc.perform(post("/api/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Faculty Feedback","startDate":"2026-08-01","endDate":"2026-08-10"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required header is missing: X-User-Id"));
    }

    @Test
    @DisplayName("POST /api/forms surfaces an end date that is not after the start date as 400")
    void createFormRejectsInvalidRange() throws Exception {
        when(feedbackFormService.createForm(any(CreateFeedbackFormRequestDto.class), eq(7L)))
                .thenThrow(new BusinessRuleViolationException("endDate must be after startDate"));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Faculty Feedback","startDate":"2026-08-10","endDate":"2026-08-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("endDate must be after startDate"));
    }

    @Test
    @DisplayName("POST /api/forms surfaces an unknown creator as 404")
    void createFormRejectsUnknownCreator() throws Exception {
        when(feedbackFormService.createForm(any(CreateFeedbackFormRequestDto.class), eq(99L)))
                .thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Faculty Feedback","startDate":"2026-08-01","endDate":"2026-08-10"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ------------------------------------------------------------------ read

    @Test
    @DisplayName("GET /api/forms/{id} returns the form")
    void getFormByIdReturnsForm() throws Exception {
        when(feedbackFormService.getFormById(1L))
                .thenReturn(formDto(1L, "Faculty Feedback", FormStatus.DRAFT));

        mockMvc.perform(get("/api/forms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalQuestions").value(8));
    }

    @Test
    @DisplayName("GET /api/forms/{id} returns 404 for an unknown form")
    void getFormByIdReturnsNotFound() throws Exception {
        when(feedbackFormService.getFormById(42L))
                .thenThrow(new ResourceNotFoundException("FeedbackForm", 42L));

        mockMvc.perform(get("/api/forms/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/forms/42"));
    }

    @Test
    @DisplayName("GET /api/forms/{id} rejects a non numeric id")
    void getFormByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/forms/not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/forms/active returns the open forms")
    void getActiveFormsReturnsList() throws Exception {
        when(feedbackFormService.getActiveForms()).thenReturn(List.of(
                formDto(1L, "Faculty Feedback", FormStatus.ACTIVE),
                formDto(2L, "Course Feedback", FormStatus.ACTIVE)));

        mockMvc.perform(get("/api/forms/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Faculty Feedback"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /api/forms/active returns an empty array rather than 404")
    void getActiveFormsReturnsEmptyArray() throws Exception {
        when(feedbackFormService.getActiveForms()).thenReturn(List.of());

        mockMvc.perform(get("/api/forms/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --------------------------------------------------------------- publish

    @Test
    @DisplayName("PUT /api/forms/{id}/publish confirms the publish")
    void publishFormReturnsMessage() throws Exception {
        doNothing().when(feedbackFormService).publishForm(1L);

        mockMvc.perform(put("/api/forms/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.message").value("Feedback form published successfully."));

        verify(feedbackFormService).publishForm(1L);
    }

    @Test
    @DisplayName("PUT /api/forms/{id}/publish returns 404 for an unknown form")
    void publishFormReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("FeedbackForm", 42L))
                .when(feedbackFormService).publishForm(42L);

        mockMvc.perform(put("/api/forms/42/publish"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/forms/{id}/publish returns 400 when the form already left DRAFT")
    void publishFormRejectsAlreadyPublished() throws Exception {
        doThrow(new BusinessRuleViolationException("Only DRAFT forms can be published"))
                .when(feedbackFormService).publishForm(1L);

        mockMvc.perform(put("/api/forms/1/publish"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only DRAFT forms can be published"));
    }

    @Test
    @DisplayName("PUT /api/forms/{id}/publish returns 400 when the form has no questions")
    void publishFormRejectsFormWithoutQuestions() throws Exception {
        doThrow(new BusinessRuleViolationException("Form 1 must have at least one question"))
                .when(feedbackFormService).publishForm(1L);

        mockMvc.perform(put("/api/forms/1/publish"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Form 1 must have at least one question"));
    }

    private FeedbackFormResponseDto formDto(Long id, String title, FormStatus status) {
        return FeedbackFormResponseDto.builder()
                .id(id)
                .title(title)
                .description("Semester Feedback")
                .category("GENERAL")
                .startDate(LocalDateTime.of(2026, 8, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 8, 10, 0, 0))
                .status(status)
                .creatorName("Admin")
                .totalQuestions(8)
                .totalAssignments(2)
                .build();
    }
}
