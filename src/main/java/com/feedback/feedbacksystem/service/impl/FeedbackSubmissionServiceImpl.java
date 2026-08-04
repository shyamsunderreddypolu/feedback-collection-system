package com.feedback.feedbacksystem.service.impl;

import com.feedback.feedbacksystem.dto.FeedbackSubmissionResponseDto;
import com.feedback.feedbacksystem.dto.SubmitAnswerDto;
import com.feedback.feedbacksystem.dto.SubmitFeedbackResponseDto;
import com.feedback.feedbacksystem.model.*;
import com.feedback.feedbacksystem.repository.*;
import com.feedback.feedbacksystem.service.FeedbackSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackSubmissionServiceImpl implements FeedbackSubmissionService {

    private final FeedbackFormRepository feedbackFormRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ResponseRepository responseRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Override
    public FeedbackSubmissionResponseDto submitFeedback(SubmitFeedbackResponseDto request, Long submitterId) {
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new IllegalArgumentException("Submitter user not found with id: " + submitterId));

        FeedbackForm form = feedbackFormRepository.findById(request.getFeedbackFormId())
                .orElseThrow(() -> new IllegalArgumentException("Feedback form not found with id: " + request.getFeedbackFormId()));

        if (form.isDeleted() || form.getStatus() != FormStatus.ACTIVE) {
            throw new IllegalStateException("Feedback form is not currently active for submissions");
        }

        if (responseRepository.existsByFeedbackFormIdAndSubmitterId(request.getFeedbackFormId(), submitterId)) {
            throw new IllegalStateException("Student has already submitted feedback for this form");
        }

        List<Question> questions = questionRepository.findByFeedbackFormId(request.getFeedbackFormId());
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        Map<Long, SubmitAnswerDto> answerMap = request.getAnswers() != null ?
                request.getAnswers().stream()
                        .filter(a -> a.getQuestionId() != null)
                        .collect(Collectors.toMap(SubmitAnswerDto::getQuestionId, Function.identity(), (existing, replacement) -> replacement))
                : Map.of();

        // Validate mandatory questions and answer column format
        for (Question question : questions) {
            SubmitAnswerDto answerDto = answerMap.get(question.getId());
            boolean isAnswered = answerDto != null && (
                    answerDto.getRatingValue() != null ||
                    (answerDto.getTextValue() != null && !answerDto.getTextValue().trim().isEmpty()) ||
                    answerDto.getSelectedOptionId() != null
            );

            if (question.isMandatory() && !isAnswered) {
                throw new IllegalArgumentException("Mandatory question was not answered: " + question.getQuestionText());
            }

            if (answerDto != null && isAnswered) {
                validateAnswerValueType(question, answerDto);
            }
        }

        Response response = Response.builder()
                .feedbackForm(form)
                .submitter(submitter)
                .build();

        Response savedResponse = responseRepository.save(response);

        int answersCount = 0;
        if (request.getAnswers() != null) {
            for (SubmitAnswerDto ansDto : request.getAnswers()) {
                Question question = questionMap.get(ansDto.getQuestionId());
                if (question == null) continue;

                QuestionOption selectedOption = null;
                if (ansDto.getSelectedOptionId() != null) {
                    selectedOption = questionOptionRepository.findById(ansDto.getSelectedOptionId())
                            .orElse(null);
                }

                Answer answer = Answer.builder()
                        .response(savedResponse)
                        .question(question)
                        .ratingValue(ansDto.getRatingValue())
                        .textValue(ansDto.getTextValue())
                        .selectedOption(selectedOption)
                        .build();

                answerRepository.save(answer);
                answersCount++;
            }
        }

        return FeedbackSubmissionResponseDto.builder()
                .responseId(savedResponse.getId())
                .feedbackFormId(form.getId())
                .formTitle(form.getTitle())
                .submitterName(submitter.getName())
                .submittedAt(savedResponse.getSubmittedAt())
                .totalAnswersCount(answersCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStudentSubmitted(Long formId, Long studentId) {
        return responseRepository.existsByFeedbackFormIdAndSubmitterId(formId, studentId);
    }

    private void validateAnswerValueType(Question question, SubmitAnswerDto answerDto) {
        QuestionType type = question.getQuestionType();
        if (type == QuestionType.RATING) {
            if (answerDto.getRatingValue() == null || answerDto.getRatingValue() < 1 || answerDto.getRatingValue() > 5) {
                throw new IllegalArgumentException("Rating answer must be between 1 and 5 for question: " + question.getQuestionText());
            }
        } else if (type == QuestionType.TEXT || type == QuestionType.TEXTAREA) {
            if (answerDto.getTextValue() == null || answerDto.getTextValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Text answer cannot be empty for question: " + question.getQuestionText());
            }
        } else if (type == QuestionType.RADIO || type == QuestionType.CHECKBOX || type == QuestionType.DROPDOWN) {
            if (answerDto.getSelectedOptionId() == null) {
                throw new IllegalArgumentException("Selected option ID is required for choice question: " + question.getQuestionText());
            }
            QuestionOption option = questionOptionRepository.findById(answerDto.getSelectedOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("Option not found with id: " + answerDto.getSelectedOptionId()));
            if (!option.getQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException("Selected option does not belong to question: " + question.getQuestionText());
            }
        }
    }
}
