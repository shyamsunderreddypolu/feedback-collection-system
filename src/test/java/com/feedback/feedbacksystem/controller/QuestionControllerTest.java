package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CreateQuestionRequestDto;
import com.feedback.feedbacksystem.dto.QuestionOptionResponseDto;
import com.feedback.feedbacksystem.dto.QuestionResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.DuplicateResourceException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.QuestionType;
import com.feedback.feedbacksystem.service.QuestionService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @Test
    @DisplayName("POST /api/questions accepts the contract names formId and required")
    void addQuestionBindsContractFieldNames() throws Exception {
        when(questionService.addQuestionToForm(any(CreateQuestionRequestDto.class)))
                .thenReturn(questionDto(5L, "How do you rate the faculty?", QuestionType.RATING, List.of()));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "questionText": "How do you rate the faculty?",
                                  "questionType": "RATING",
                                  "displayOrder": 1,
                                  "required": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.message").value("Question added successfully."));

        ArgumentCaptor<CreateQuestionRequestDto> captor =
                ArgumentCaptor.forClass(CreateQuestionRequestDto.class);
        verify(questionService).addQuestionToForm(captor.capture());

        assertThat(captor.getValue().getFeedbackFormId()).isEqualTo(1L);
        assertThat(captor.getValue().isMandatory()).isTrue();
        assertThat(captor.getValue().getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/questions carries the options of a choice question through")
    void addChoiceQuestionPassesOptions() throws Exception {
        when(questionService.addQuestionToForm(any(CreateQuestionRequestDto.class)))
                .thenReturn(questionDto(6L, "Teaching Method", QuestionType.RADIO,
                        List.of("Excellent", "Good", "Average", "Poor")));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "questionText": "Teaching Method",
                                  "questionType": "RADIO",
                                  "options": ["Excellent", "Good", "Average", "Poor"],
                                  "displayOrder": 2,
                                  "required": true
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateQuestionRequestDto> captor =
                ArgumentCaptor.forClass(CreateQuestionRequestDto.class);
        verify(questionService).addQuestionToForm(captor.capture());
        assertThat(captor.getValue().getOptions()).containsExactly("Excellent", "Good", "Average", "Poor");
    }

    @Test
    @DisplayName("POST /api/questions rejects a body with no form reference")
    void addQuestionRequiresFormId() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionText":"Suggestions","questionType":"TEXT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.feedbackFormId").exists());
    }

    @Test
    @DisplayName("POST /api/questions rejects blank question text")
    void addQuestionRequiresText() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"questionText":"   ","questionType":"TEXT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.questionText").exists());
    }

    @Test
    @DisplayName("POST /api/questions rejects an unknown question type")
    void addQuestionRejectsUnknownType() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"questionText":"Rate it","questionType":"SLIDER"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/questions returns 404 when the form does not exist")
    void addQuestionReturnsNotFound() throws Exception {
        when(questionService.addQuestionToForm(any(CreateQuestionRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("FeedbackForm", 99L));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":99,"questionText":"Rate it","questionType":"RATING"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/questions returns 400 once the form has left DRAFT")
    void addQuestionRejectsPublishedForm() throws Exception {
        when(questionService.addQuestionToForm(any(CreateQuestionRequestDto.class)))
                .thenThrow(new BusinessRuleViolationException(
                        "Questions can only be added while a form is DRAFT"));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"questionText":"Rate it","questionType":"RATING"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Questions can only be added while a form is DRAFT"));
    }

    @Test
    @DisplayName("POST /api/questions returns 400 for a choice question with too few options")
    void addQuestionRejectsSingleOption() throws Exception {
        when(questionService.addQuestionToForm(any(CreateQuestionRequestDto.class)))
                .thenThrow(new BusinessRuleViolationException("RADIO questions require at least 2 options"));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"questionText":"Method","questionType":"RADIO","options":["Good"]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("RADIO questions require at least 2 options"));
    }

    @Test
    @DisplayName("POST /api/questions returns 409 for a display order already in use")
    void addQuestionRejectsDuplicateDisplayOrder() throws Exception {
        when(questionService.addQuestionToForm(any(CreateQuestionRequestDto.class)))
                .thenThrow(new DuplicateResourceException(
                        "Form 1 already has a question at display order 2"));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"formId":1,"questionText":"Rate it","questionType":"RATING","displayOrder":2}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/questions/form/{formId} returns questions in display order")
    void getQuestionsByFormReturnsOrderedList() throws Exception {
        when(questionService.getQuestionsByFormId(1L)).thenReturn(List.of(
                questionDto(1L, "Rate Faculty", QuestionType.RATING, List.of()),
                questionDto(2L, "Suggestions", QuestionType.TEXT, List.of())));

        mockMvc.perform(get("/api/questions/form/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].questionText").value("Rate Faculty"))
                .andExpect(jsonPath("$[0].questionType").value("RATING"))
                .andExpect(jsonPath("$[1].questionText").value("Suggestions"));
    }

    @Test
    @DisplayName("GET /api/questions/form/{formId} exposes the flag as required, not mandatory")
    void getQuestionsUsesContractFieldName() throws Exception {
        when(questionService.getQuestionsByFormId(1L)).thenReturn(List.of(
                questionDto(1L, "Rate Faculty", QuestionType.RADIO, List.of("Good", "Poor"))));

        mockMvc.perform(get("/api/questions/form/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].required").value(true))
                .andExpect(jsonPath("$[0].mandatory").doesNotExist())
                .andExpect(jsonPath("$[0].options[0].optionValue").value("Good"));
    }

    @Test
    @DisplayName("GET /api/questions/form/{formId} returns 404 for an unknown form")
    void getQuestionsByFormReturnsNotFound() throws Exception {
        when(questionService.getQuestionsByFormId(99L))
                .thenThrow(new ResourceNotFoundException("FeedbackForm", 99L));

        mockMvc.perform(get("/api/questions/form/99"))
                .andExpect(status().isNotFound());
    }

    private QuestionResponseDto questionDto(Long id, String text, QuestionType type, List<String> options) {
        List<QuestionOptionResponseDto> optionDtos = options.stream()
                .map(value -> QuestionOptionResponseDto.builder()
                        .id((long) (options.indexOf(value) + 1))
                        .optionValue(value)
                        .displayOrder(options.indexOf(value) + 1)
                        .isActive(true)
                        .build())
                .toList();

        return QuestionResponseDto.builder()
                .id(id)
                .questionText(text)
                .questionType(type)
                .mandatory(true)
                .displayOrder(1)
                .options(optionDtos)
                .build();
    }
}
