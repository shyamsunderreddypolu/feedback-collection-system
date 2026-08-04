package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CreateFeedbackFormRequestDto;
import com.feedback.feedbacksystem.dto.FeedbackFormResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.FeedbackForm;
import com.feedback.feedbacksystem.model.FormStatus;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.FeedbackAssignmentRepository;
import com.feedback.feedbacksystem.repository.FeedbackFormRepository;
import com.feedback.feedbacksystem.repository.QuestionRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackFormServiceImplTest {

    @Mock
    private FeedbackFormRepository feedbackFormRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private FeedbackAssignmentRepository feedbackAssignmentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackFormServiceImpl service;

    private User creator;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(7L).name("Dr. Raj Kumar").build();
        start = LocalDateTime.now().plusDays(1);
        end = start.plusDays(14);
    }

    private CreateFeedbackFormRequestDto request() {
        return CreateFeedbackFormRequestDto.builder()
                .title("Semester 6 Course Feedback")
                .description("End of semester review")
                .category("COURSE")
                .startDate(start)
                .endDate(end)
                .build();
    }

    private FeedbackForm form(FormStatus status) {
        return FeedbackForm.builder()
                .id(1L)
                .title("Semester 6 Course Feedback")
                .category("COURSE")
                .startDate(start)
                .endDate(end)
                .status(status)
                .creator(creator)
                .build();
    }

    @Test
    @DisplayName("createForm persists a DRAFT form linked to the creator")
    void createFormStartsAsDraft() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(creator));
        when(feedbackFormRepository.save(any(FeedbackForm.class))).thenAnswer(invocation -> {
            FeedbackForm saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(questionRepository.countByFeedbackFormId(1L)).thenReturn(0L);
        when(feedbackAssignmentRepository.countByFeedbackFormId(1L)).thenReturn(0L);

        FeedbackFormResponseDto response = service.createForm(request(), 7L);

        ArgumentCaptor<FeedbackForm> captor = ArgumentCaptor.forClass(FeedbackForm.class);
        verify(feedbackFormRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(FormStatus.DRAFT);
        assertThat(captor.getValue().getCreator()).isEqualTo(creator);
        assertThat(captor.getValue().getCategory()).isEqualTo("COURSE");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(FormStatus.DRAFT);
        assertThat(response.getCreatorName()).isEqualTo("Dr. Raj Kumar");
        assertThat(response.getTotalQuestions()).isZero();
    }

    @Test
    @DisplayName("createForm rejects an endDate that is not after startDate")
    void createFormRejectsInvalidWindow() {
        CreateFeedbackFormRequestDto request = request();
        request.setEndDate(start);

        assertThatThrownBy(() -> service.createForm(request, 7L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("endDate must be after startDate");

        verify(feedbackFormRepository, never()).save(any());
    }

    @Test
    @DisplayName("createForm rejects an unknown creator")
    void createFormRejectsUnknownCreator() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createForm(request(), 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    @DisplayName("createForm falls back to the GENERAL category when none is supplied")
    void createFormDefaultsCategory() {
        CreateFeedbackFormRequestDto request = request();
        request.setCategory("  ");

        when(userRepository.findById(7L)).thenReturn(Optional.of(creator));
        when(feedbackFormRepository.save(any(FeedbackForm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.createForm(request, 7L).getCategory()).isEqualTo("GENERAL");
    }

    @Test
    @DisplayName("getFormById hides soft deleted forms")
    void getFormByIdHidesDeleted() {
        FeedbackForm deleted = form(FormStatus.ACTIVE);
        deleted.setDeleted(true);
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> service.getFormById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getActiveForms queries the open response window and drops soft deleted ones")
    void getActiveFormsExcludesDeleted() {
        FeedbackForm live = form(FormStatus.ACTIVE);
        FeedbackForm deleted = form(FormStatus.ACTIVE);
        deleted.setId(2L);
        deleted.setDeleted(true);
        when(feedbackFormRepository.findByStatusAndStartDateBeforeAndEndDateAfter(
                eq(FormStatus.ACTIVE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(live, deleted));
        when(questionRepository.countByFeedbackFormId(1L)).thenReturn(3L);
        when(feedbackAssignmentRepository.countByFeedbackFormId(1L)).thenReturn(2L);

        LocalDateTime before = LocalDateTime.now();
        List<FeedbackFormResponseDto> active = service.getActiveForms();

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getId()).isEqualTo(1L);
        assertThat(active.get(0).getTotalQuestions()).isEqualTo(3L);
        assertThat(active.get(0).getTotalAssignments()).isEqualTo(2L);

        // The window is anchored on the current time rather than left unbounded.
        ArgumentCaptor<LocalDateTime> startBound = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endBound = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(feedbackFormRepository).findByStatusAndStartDateBeforeAndEndDateAfter(
                eq(FormStatus.ACTIVE), startBound.capture(), endBound.capture());
        assertThat(startBound.getValue()).isBetween(before, LocalDateTime.now());
        assertThat(endBound.getValue()).isEqualTo(startBound.getValue());
    }

    @Test
    @DisplayName("publishForm moves a DRAFT form with questions to ACTIVE")
    void publishFormActivatesDraft() {
        FeedbackForm draft = form(FormStatus.DRAFT);
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(draft));
        when(questionRepository.countByFeedbackFormId(1L)).thenReturn(5L);

        service.publishForm(1L);

        assertThat(draft.getStatus()).isEqualTo(FormStatus.ACTIVE);
        verify(feedbackFormRepository).save(draft);
    }

    @Test
    @DisplayName("publishForm refuses a form with no questions")
    void publishFormRequiresQuestions() {
        FeedbackForm draft = form(FormStatus.DRAFT);
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(draft));
        when(questionRepository.countByFeedbackFormId(1L)).thenReturn(0L);

        assertThatThrownBy(() -> service.publishForm(1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("at least one question");

        assertThat(draft.getStatus()).isEqualTo(FormStatus.DRAFT);
        verify(feedbackFormRepository, never()).save(any());
    }

    @Test
    @DisplayName("publishForm refuses a form that has already left DRAFT")
    void publishFormRejectsNonDraft() {
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(form(FormStatus.CLOSED)));

        assertThatThrownBy(() -> service.publishForm(1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Only DRAFT forms can be published");
    }
}
