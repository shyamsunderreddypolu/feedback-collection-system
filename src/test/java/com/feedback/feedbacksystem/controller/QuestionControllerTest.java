package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CreateQuestionRequestDto;
import com.feedback.feedbacksystem.dto.QuestionResponseDto;

import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.QuestionType;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import com.feedback.feedbacksystem.security.service.CustomUserDetailsService;
import com.feedback.feedbacksystem.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/questions adds a rating question")
    void addQuestionReturnsCreated() throws Exception {
        when(questionService.addQuestionToForm(any(CreateQuestionRequestDto.class)))
                .thenReturn(questionDto(5L, "How do you rate the course?"));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "questionText": "How do you rate the course?",
                                  "questionType": "RATING",
                                  "displayOrder": 1,
                                  "required": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.message").value("Question added successfully."));
    }

    @Test
    @DisplayName("POST /api/questions adds a choice question with options")
    void addQuestionWithOptionsReturnsCreated() throws Exception {
        when(questionService.addQuestionToForm(any()))
                .thenReturn(questionDto(6L, "Preferred teaching mode"));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "questionText": "Preferred teaching mode",
                                  "questionType": "RADIO",
                                  "displayOrder": 2,
                                  "required": true,
                                  "options": ["Online", "Offline", "Hybrid"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6));
    }

    @Test
    @DisplayName("a choice question with fewer than 2 options returns 400")
    void addQuestionValidatesChoiceOptionsCount() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "questionText": "Choice with one option",
                                  "questionType": "RADIO",
                                  "options": ["Only One"],
                                  "displayOrder": 1,
                                  "required": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.options").exists());
    }

    @Test
    @DisplayName("a choice question missing options altogether returns 400")
    void addQuestionValidatesChoiceOptionsRequired() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formId": 1,
                                  "questionText": "Choice without options",
                                  "questionType": "CHECKBOX",
                                  "displayOrder": 1,
                                  "required": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.options").exists());
    }

    @Test
    @DisplayName("missing formId or blank text returns 400")
    void addQuestionValidatesPayload() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionText": "   ",
                                  "questionType": "RATING"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.formId").exists())
                .andExpect(jsonPath("$.fieldErrors.questionText").exists());
    }

    @Test
    @DisplayName("unknown form returns 404")
    void addQuestionTranslatesNotFound() throws Exception {
        when(questionService.addQuestionToForm(any()))
                .thenThrow(new ResourceNotFoundException("FeedbackForm", "id", 99L));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingPayload(99L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("FeedbackForm not found with id: '99'"));
    }

    @Test
    @DisplayName("options given on a rating question returns 400 business rule")
    void addQuestionTranslatesBusinessRule() throws Exception {
        when(questionService.addQuestionToForm(any()))
                .thenThrow(new BusinessRuleViolationException("Options are only supported for choice questions."));

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingPayload(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Options are only supported for choice questions."));
    }

    @Test
    @DisplayName("GET /api/questions/form/{formId} returns ordered questions")
    void getQuestionsByFormReturnsList() throws Exception {
        when(questionService.getQuestionsByFormId(1L))
                .thenReturn(List.of(questionDto(10L, "First Question")));

        mockMvc.perform(get("/api/questions/form/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].questionText").value("First Question"));
    }

    @Test
    @DisplayName("GET /api/questions/form/{formId} uses contract field name for questions")
    void getQuestionsUsesContractFieldName() throws Exception {
        when(questionService.getQuestionsByFormId(1L))
                .thenReturn(List.of(questionDto(10L, "First Question")));

        mockMvc.perform(get("/api/questions/form/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionText").isEqualTo("First Question"));
    }

    private QuestionResponseDto questionDto(Long id, String text) {
        return QuestionResponseDto.builder()
                .id(id)
                .formId(1L)
                .questionText(text)
                .questionType(QuestionType.RATING)
                .displayOrder(1)
                .required(true)
                .build();
    }

    private String ratingPayload(Long formId) {
        return """
                {
                  "formId": %d,
                  "questionText": "Sample Question",
                  "questionType": "RATING",
                  "displayOrder": 1,
                  "required": true
                }
                """.formatted(formId);
    }
}
