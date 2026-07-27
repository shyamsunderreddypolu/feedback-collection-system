package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CreateQuestionRequestDto;
import com.feedback.feedbacksystem.dto.QuestionOptionResponseDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    /** Types whose answer is picked from a fixed list, so they must carry options. */
    private static final Set<QuestionType> OPTION_BASED =
            EnumSet.of(QuestionType.RADIO, QuestionType.CHECKBOX, QuestionType.DROPDOWN);

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final FeedbackFormRepository feedbackFormRepository;

    @Override
    public QuestionResponseDto addQuestionToForm(CreateQuestionRequestDto request) {
        FeedbackForm form = findLiveForm(request.getFeedbackFormId());

        // Questions may only be edited while the form is still a draft, otherwise
        // responses already collected would no longer match the form they answered.
        if (form.getStatus() != FormStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Questions can only be added while a form is DRAFT, but form " + form.getId()
                            + " is " + form.getStatus());
        }

        List<String> options = validateOptions(request);
        int displayOrder = resolveDisplayOrder(request);

        Question question = questionRepository.save(Question.builder()
                .feedbackForm(form)
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .isMandatory(request.isMandatory())
                .displayOrder(displayOrder)
                .build());

        List<QuestionOption> savedOptions = saveOptions(question, options);
        return toResponseDto(question, savedOptions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponseDto> getQuestionsByFormId(Long formId) {
        findLiveForm(formId);

        return questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(formId).stream()
                .map(question -> toResponseDto(
                        question,
                        questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId())))
                .toList();
    }

    /**
     * Checks the option list against the question type and returns the trimmed values
     * to persist (empty for types that are answered freely or by rating).
     */
    private List<String> validateOptions(CreateQuestionRequestDto request) {
        QuestionType type = request.getQuestionType();
        if (type == null) {
            throw new BusinessRuleViolationException(
                    "questionType is required and must be one of " + EnumSet.allOf(QuestionType.class));
        }
        List<String> supplied = request.getOptions() == null ? List.of() : request.getOptions();

        if (!OPTION_BASED.contains(type)) {
            if (!supplied.isEmpty()) {
                throw new BusinessRuleViolationException(
                        type + " questions are answered directly and must not define options");
            }
            return List.of();
        }

        List<String> cleaned = supplied.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (cleaned.isEmpty()) {
            throw new BusinessRuleViolationException(type + " questions require at least one option");
        }
        return cleaned;
    }

    /**
     * Uses the requested position, or appends the question to the end of the form
     * when none was given. Display order is 1 based and unique per form.
     */
    private int resolveDisplayOrder(CreateQuestionRequestDto request) {
        List<Question> existing =
                questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(request.getFeedbackFormId());

        if (request.getDisplayOrder() < 1) {
            return existing.stream().mapToInt(Question::getDisplayOrder).max().orElse(0) + 1;
        }
        boolean taken = existing.stream()
                .anyMatch(question -> question.getDisplayOrder() == request.getDisplayOrder());
        if (taken) {
            throw new DuplicateResourceException("Form " + request.getFeedbackFormId()
                    + " already has a question at display order " + request.getDisplayOrder());
        }
        return request.getDisplayOrder();
    }

    private List<QuestionOption> saveOptions(Question question, List<String> optionValues) {
        List<QuestionOption> options = new ArrayList<>(optionValues.size());
        for (int i = 0; i < optionValues.size(); i++) {
            options.add(QuestionOption.builder()
                    .question(question)
                    .optionValue(optionValues.get(i))
                    .displayOrder(i + 1)
                    .build());
        }
        return options.isEmpty() ? options : questionOptionRepository.saveAll(options);
    }

    private FeedbackForm findLiveForm(Long formId) {
        FeedbackForm form = feedbackFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackForm", formId));
        if (form.isDeleted()) {
            throw new ResourceNotFoundException("FeedbackForm", formId);
        }
        return form;
    }

    private QuestionResponseDto toResponseDto(Question question, List<QuestionOption> options) {
        return QuestionResponseDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .isMandatory(question.isMandatory())
                .displayOrder(question.getDisplayOrder())
                .options(options.stream()
                        .map(option -> QuestionOptionResponseDto.builder()
                                .id(option.getId())
                                .optionValue(option.getOptionValue())
                                .displayOrder(option.getDisplayOrder())
                                .isActive(option.isActive())
                                .build())
                        .toList())
                .build();
    }
}
