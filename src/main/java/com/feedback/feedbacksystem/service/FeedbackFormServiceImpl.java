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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackFormServiceImpl implements FeedbackFormService {

    private final FeedbackFormRepository feedbackFormRepository;
    private final QuestionRepository questionRepository;
    private final FeedbackAssignmentRepository feedbackAssignmentRepository;
    private final UserRepository userRepository;

    @Override
    public FeedbackFormResponseDto createForm(CreateFeedbackFormRequestDto request, Long creatorId) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessRuleViolationException("startDate and endDate are required");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessRuleViolationException(
                    "endDate must be after startDate (startDate=" + request.getStartDate()
                            + ", endDate=" + request.getEndDate() + ")");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", creatorId));

        FeedbackForm form = FeedbackForm.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(FormStatus.DRAFT)
                .creator(creator)
                .build();

        // Leave the entity's GENERAL default in place when no category is supplied.
        if (StringUtils.hasText(request.getCategory())) {
            form.setCategory(request.getCategory());
        }

        return toResponseDto(feedbackFormRepository.save(form));
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackFormResponseDto getFormById(Long formId) {
        return toResponseDto(findLiveForm(formId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackFormResponseDto> getActiveForms() {
        // Active means both published and inside its response window, otherwise forms whose
        // end date has passed would still be offered to students.
        LocalDateTime now = LocalDateTime.now();
        return feedbackFormRepository
                .findByStatusAndStartDateBeforeAndEndDateAfter(FormStatus.ACTIVE, now, now).stream()
                .filter(form -> !form.isDeleted())
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public void publishForm(Long formId) {
        FeedbackForm form = findLiveForm(formId);

        if (form.getStatus() != FormStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Only DRAFT forms can be published, but form " + formId + " is " + form.getStatus());
        }
        if (questionRepository.countByFeedbackFormId(formId) == 0) {
            throw new BusinessRuleViolationException(
                    "Form " + formId + " must have at least one question before it can be published");
        }

        form.setStatus(FormStatus.ACTIVE);
        feedbackFormRepository.save(form);
    }

    private FeedbackForm findLiveForm(Long formId) {
        FeedbackForm form = feedbackFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackForm", formId));
        if (form.isDeleted()) {
            throw new ResourceNotFoundException("FeedbackForm", formId);
        }
        return form;
    }

    private FeedbackFormResponseDto toResponseDto(FeedbackForm form) {
        return FeedbackFormResponseDto.builder()
                .id(form.getId())
                .title(form.getTitle())
                .description(form.getDescription())
                .category(form.getCategory())
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .status(form.getStatus())
                .creatorName(form.getCreator() == null ? null : form.getCreator().getName())
                .totalQuestions(questionRepository.countByFeedbackFormId(form.getId()))
                .totalAssignments(feedbackAssignmentRepository.countByFeedbackFormId(form.getId()))
                .build();
    }
}
