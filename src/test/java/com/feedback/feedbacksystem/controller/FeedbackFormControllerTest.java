package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CreateFeedbackFormRequestDto;
import com.feedback.feedbacksystem.dto.FeedbackFormResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.FormStatus;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import com.feedback.feedbacksystem.security.service.CustomUserDetailsService;
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

@WebMvcTest(FeedbackFormController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class FeedbackFormControllerTest {

    private static final String USER_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedbackFormService feedbackFormService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ---------------------------------------------------------------- create

    @Test
    @DisplayName("POST /api/forms returns 201 with the created form")
    void createFormReturnsCreated() throws Exception {
        when(feedbackFormService.createForm(any(CreateFeedbackFormRequestDto.class), eq(7L)))
                .thenReturn(formDto(12L, FormStatus.DRAFT, "Faculty Feedback"));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Faculty Feedback",
                                  "description": "Semester end feedback",
                                  "startDate": "2026-08-01T00:00:00",
                                  "endDate": "2026-08-15T23:59:59"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.title").value("Faculty Feedback"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        ArgumentCaptor<CreateFeedbackFormRequestDto> captor =
                ArgumentCaptor.forClass(CreateFeedbackFormRequestDto.class);
        verify(feedbackFormService).createForm(captor.capture(), eq(7L));

        CreateFeedbackFormRequestDto sent = captor.getValue();
        assertThat(sent.getTitle()).isEqualTo("Faculty Feedback");
        assertThat(sent.getStartDate()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0, 0));
        assertThat(sent.getEndDate()).isEqualTo(LocalDateTime.of(2026, 8, 15, 23, 59, 59));
    }

    @Test
    @DisplayName("POST /api/forms parses short date formats seamlessly")
    void createFormParsesShortDates() throws Exception {
        when(feedbackFormService.createForm(any(), eq(1L)))
                .thenReturn(formDto(1L, FormStatus.DRAFT, "Short Date Form"));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Short Date Form",
                                  "startDate": "2026-08-01",
                                  "endDate": "2026-08-15"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateFeedbackFormRequestDto> captor =
                ArgumentCaptor.forClass(CreateFeedbackFormRequestDto.class);
        verify(feedbackFormService).createForm(captor.capture(), eq(1L));

        assertThat(captor.getValue().getStartDate()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0, 0));
        assertThat(captor.getValue().getEndDate()).isEqualTo(LocalDateTime.of(2026, 8, 15, 23, 59, 59));
    }

    @Test
    @DisplayName("missing title or dates returns 400 with field errors")
    void createFormValidatesPayload() throws Exception {
        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "missing start and end date"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.startDate").exists())
                .andExpect(jsonPath("$.fieldErrors.endDate").exists());
    }

    @Test
    @DisplayName("unparseable date returns 400 malformed request body")
    void createFormRejectsInvalidDateFormat() throws Exception {
        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Bad Date Form",
                                  "startDate": "01-08-2026",
                                  "endDate": "2026-08-15"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("Malformed request body")));
    }

    @Test
    @DisplayName("unknown creator returns 404")
    void createFormTranslatesMissingUserToNotFound() throws Exception {
        when(feedbackFormService.createForm(any(), eq(99L)))
                .thenThrow(new ResourceNotFoundException("User", "id", 99L));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Valid Form",
                                  "startDate": "2026-08-01",
                                  "endDate": "2026-08-15"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: '99'"));
    }

    @Test
    @DisplayName("end date before start date returns 400")
    void createFormTranslatesBusinessRuleToBadRequest() throws Exception {
        when(feedbackFormService.createForm(any(), eq(1L)))
                .thenThrow(new BusinessRuleViolationException("End date must be after start date."));

        mockMvc.perform(post("/api/forms")
                        .header(USER_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Backwards Dates",
                                  "startDate": "2026-08-15",
                                  "endDate": "2026-08-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date must be after start date."));
    }

    // ---------------------------------------------------------------- read

    @Test
    @DisplayName("GET /api/forms/active lists active forms")
    void getActiveFormsReturnsList() throws Exception {
        when(feedbackFormService.getActiveForms())
                .thenReturn(List.of(formDto(1L, FormStatus.ACTIVE, "Active One")));

        mockMvc.perform(get("/api/forms/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/forms/{id} returns the requested form")
    void getFormByIdReturnsForm() throws Exception {
        when(feedbackFormService.getFormById(5L))
                .thenReturn(formDto(5L, FormStatus.DRAFT, "Form Five"));

        mockMvc.perform(get("/api/forms/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Form Five"));
    }

    @Test
    @DisplayName("GET /api/forms/{id} for unknown id returns 404")
    void getFormByIdReturnsNotFound() throws Exception {
        when(feedbackFormService.getFormById(99L))
                .thenThrow(new ResourceNotFoundException("FeedbackForm", "id", 99L));

        mockMvc.perform(get("/api/forms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("FeedbackForm not found with id: '99'"));
    }

    @Test
    @DisplayName("GET /api/forms/abc returns 400 type mismatch")
    void getFormByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/forms/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'id' has an invalid value: abc"));
    }

    // ---------------------------------------------------------------- publish

    @Test
    @DisplayName("PUT /api/forms/{id}/publish returns success message")
    void publishFormReturnsOk() throws Exception {
        doNothing().when(feedbackFormService).publishForm(3L);

        mockMvc.perform(put("/api/forms/3/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.message").value("Feedback form published successfully."));

        verify(feedbackFormService).publishForm(3L);
    }

    @Test
    @DisplayName("publishing a form with no questions returns 400")
    void publishFormTranslatesBusinessRule() throws Exception {
        doThrow(new BusinessRuleViolationException("Cannot publish form with no questions."))
                .when(feedbackFormService).publishForm(3L);

        mockMvc.perform(put("/api/forms/3/publish"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot publish form with no questions."));
    }

    @Test
    @DisplayName("publishing an unknown form returns 404")
    void publishFormTranslatesNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("FeedbackForm", "id", 99L))
                .when(feedbackFormService).publishForm(99L);

        mockMvc.perform(put("/api/forms/99/publish"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("FeedbackForm not found with id: '99'"));
    }

    private FeedbackFormResponseDto formDto(Long id, FormStatus status, String title) {
        return FeedbackFormResponseDto.builder()
                .id(id)
                .title(title)
                .description("Sample Form")
                .status(status)
                .category("GENERAL")
                .creatorId(1L)
                .creatorName("Demo Admin")
                .totalQuestions(0)
                .startDate(LocalDateTime.of(2026, 8, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 8, 15, 23, 59, 59))
                .build();
    }
}
