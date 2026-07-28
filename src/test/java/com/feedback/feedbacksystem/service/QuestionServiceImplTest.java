package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CreateQuestionRequestDto;
import com.feedback.feedbacksystem.dto.QuestionResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.DuplicateResourceException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.FeedbackForm;
import com.feedback.feedbacksystem.model.FormStatus;
import com.feedback.feedbacksystem.model.Question;
import com.feedback.feedbacksystem.model.QuestionOption;
import com.feedback.feedbacksystem.model.QuestionType;
import com.feedback.feedbacksystem.repository.FeedbackFormRepository;
import com.feedback.feedbacksystem.repository.QuestionOptionRepository;
import com.feedback.feedbacksystem.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuestionOptionRepository questionOptionRepository;
    @Mock
    private FeedbackFormRepository feedbackFormRepository;

    @InjectMocks
    private QuestionServiceImpl service;

    private FeedbackForm draftForm;

    @BeforeEach
    void setUp() {
        draftForm = FeedbackForm.builder().id(1L).title("Course Feedback").status(FormStatus.DRAFT).build();
    }

    private CreateQuestionRequestDto request(QuestionType type, List<String> options) {
        return CreateQuestionRequestDto.builder()
                .feedbackFormId(1L)
                .questionText("How would you rate the teaching?")
                .questionType(type)
                .isMandatory(true)
                .options(options)
                .build();
    }

    private void stubDraftForm() {
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(draftForm));
    }

    private void stubQuestionSave() {
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
    }

    @Test
    @DisplayName("addQuestionToForm stores a RATING question with no options")
    void addsRatingQuestion() {
        stubDraftForm();
        stubQuestionSave();
        when(questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());

        QuestionResponseDto response = service.addQuestionToForm(request(QuestionType.RATING, null));

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getQuestionType()).isEqualTo(QuestionType.RATING);
        assertThat(response.isMandatory()).isTrue();
        assertThat(response.getOptions()).isEmpty();
        verify(questionOptionRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("addQuestionToForm stores RADIO options numbered from one")
    void addsRadioOptionsInOrder() {
        stubDraftForm();
        stubQuestionSave();
        when(questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());
        when(questionOptionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponseDto response =
                service.addQuestionToForm(request(QuestionType.RADIO, List.of("Excellent", " Good ", "Poor")));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QuestionOption>> captor = ArgumentCaptor.forClass(List.class);
        verify(questionOptionRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).extracting(QuestionOption::getOptionValue)
                .containsExactly("Excellent", "Good", "Poor");
        assertThat(captor.getValue()).extracting(QuestionOption::getDisplayOrder)
                .containsExactly(1, 2, 3);
        assertThat(captor.getValue()).allMatch(QuestionOption::isActive);
        assertThat(response.getOptions()).hasSize(3);
    }

    @Test
    @DisplayName("addQuestionToForm rejects a choice question with no options")
    void rejectsRadioWithoutOptions() {
        stubDraftForm();

        assertThatThrownBy(() -> service.addQuestionToForm(request(QuestionType.RADIO, List.of())))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("require at least one option");

        verify(questionRepository, never()).save(any());
    }

    @Test
    @DisplayName("addQuestionToForm rejects a choice question whose options are all blank")
    void rejectsRadioWithBlankOptions() {
        stubDraftForm();

        assertThatThrownBy(() -> service.addQuestionToForm(request(QuestionType.RADIO, List.of(" ", ""))))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("require at least one option");
    }

    @Test
    @DisplayName("addQuestionToForm rejects options on a free text question")
    void rejectsOptionsOnTextarea() {
        stubDraftForm();

        assertThatThrownBy(() -> service.addQuestionToForm(request(QuestionType.TEXTAREA, List.of("Yes"))))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("must not define options");
    }

    @Test
    @DisplayName("addQuestionToForm appends to the end when no display order is given")
    void appendsWhenDisplayOrderOmitted() {
        stubDraftForm();
        stubQuestionSave();
        when(questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(
                Question.builder().id(8L).displayOrder(1).build(),
                Question.builder().id(9L).displayOrder(2).build()));

        QuestionResponseDto response = service.addQuestionToForm(request(QuestionType.RATING, null));

        assertThat(response.getDisplayOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("addQuestionToForm rejects a display order already used on the form")
    void rejectsDuplicateDisplayOrder() {
        stubDraftForm();
        when(questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(Question.builder().id(8L).displayOrder(2).build()));

        CreateQuestionRequestDto request = request(QuestionType.RATING, null);
        request.setDisplayOrder(2);

        assertThatThrownBy(() -> service.addQuestionToForm(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("display order 2");

        verify(questionRepository, never()).save(any());
    }

    @Test
    @DisplayName("addQuestionToForm refuses to change a form that is no longer DRAFT")
    void rejectsPublishedForm() {
        draftForm.setStatus(FormStatus.ACTIVE);
        stubDraftForm();

        assertThatThrownBy(() -> service.addQuestionToForm(request(QuestionType.RATING, null)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("only be added while a form is DRAFT");
    }

    @Test
    @DisplayName("addQuestionToForm rejects an unknown form")
    void rejectsUnknownForm() {
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addQuestionToForm(request(QuestionType.RATING, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("FeedbackForm not found with id: 1");
    }

    @Test
    @DisplayName("getQuestionsByFormId returns questions in display order with their options")
    void listsQuestionsWithOptions() {
        stubDraftForm();
        Question rating = Question.builder().id(10L).displayOrder(1).questionType(QuestionType.RATING).build();
        Question radio = Question.builder().id(11L).displayOrder(2).questionType(QuestionType.RADIO).build();
        when(questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(rating, radio));
        when(questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(10L)).thenReturn(List.of());
        when(questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(11L)).thenReturn(List.of(
                QuestionOption.builder().id(50L).optionValue("Yes").displayOrder(1).build()));

        List<QuestionResponseDto> questions = service.getQuestionsByFormId(1L);

        assertThat(questions).extracting(QuestionResponseDto::getId).containsExactly(10L, 11L);
        assertThat(questions.get(0).getOptions()).isEmpty();
        assertThat(questions.get(1).getOptions()).extracting("optionValue").containsExactly("Yes");
    }
}
