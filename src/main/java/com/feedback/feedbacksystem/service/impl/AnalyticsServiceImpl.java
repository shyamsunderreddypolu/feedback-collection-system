package com.feedback.feedbacksystem.service.impl;

import com.feedback.feedbacksystem.dto.CourseAnalyticsDto;
import com.feedback.feedbacksystem.dto.FormAnalyticsSummaryDto;
import com.feedback.feedbacksystem.dto.QuestionRatingSummaryDto;
import com.feedback.feedbacksystem.model.*;
import com.feedback.feedbacksystem.repository.*;
import com.feedback.feedbacksystem.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final FeedbackFormRepository feedbackFormRepository;
    private final CourseRepository courseRepository;
    private final ResponseRepository responseRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final FeedbackAssignmentRepository feedbackAssignmentRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Override
    public FormAnalyticsSummaryDto getFormAnalytics(Long formId) {
        FeedbackForm form = feedbackFormRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback form not found with id: " + formId));

        if (form.isDeleted()) {
            throw new IllegalArgumentException("Feedback form with id " + formId + " has been deleted");
        }

        long totalResponses = responseRepository.countByFeedbackFormId(formId);

        List<FeedbackAssignment> assignments = feedbackAssignmentRepository.findByFeedbackFormId(formId);
        long totalTargetedStudents = calculateTotalTargetedStudents(assignments);

        Double completionRate = calculateCompletionRate(totalResponses, totalTargetedStudents);

        List<Question> ratingQuestions = questionRepository.findByFeedbackFormIdAndQuestionType(formId, QuestionType.RATING);
        List<QuestionRatingSummaryDto> questionRatings = new ArrayList<>();

        for (Question question : ratingQuestions) {
            Double avgRating = answerRepository.findAverageRatingByQuestionId(question.getId());
            long totalRatings = answerRepository.findByQuestionIdAndRatingValueIsNotNull(question.getId()).size();

            Double formattedAvg = roundToTwoDecimals(avgRating);

            questionRatings.add(QuestionRatingSummaryDto.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .averageRating(formattedAvg)
                    .totalRatings(totalRatings)
                    .build());
        }

        Double overallAverageRating = answerRepository.findAverageRatingByFeedbackFormId(formId);
        Double formattedOverallAvg = roundToTwoDecimals(overallAverageRating);

        return FormAnalyticsSummaryDto.builder()
                .formId(form.getId())
                .formTitle(form.getTitle())
                .category(form.getCategory())
                .totalResponses(totalResponses)
                .totalTargetedStudents(totalTargetedStudents)
                .completionRate(completionRate)
                .overallAverageRating(formattedOverallAvg)
                .questionRatings(questionRatings)
                .build();
    }

    @Override
    public CourseAnalyticsDto getCourseAnalytics(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        List<FeedbackAssignment> assignments = feedbackAssignmentRepository.findByCourseId(courseId);

        List<Long> formIds = assignments.stream()
                .map(FeedbackAssignment::getFeedbackForm)
                .filter(Objects::nonNull)
                .map(FeedbackForm::getId)
                .distinct()
                .collect(Collectors.toList());

        List<FormAnalyticsSummaryDto> formSummaries = new ArrayList<>();
        long totalResponsesAcrossForms = 0;

        for (Long formId : formIds) {
            try {
                FormAnalyticsSummaryDto formSummary = getFormAnalytics(formId);
                formSummaries.add(formSummary);
                totalResponsesAcrossForms += formSummary.getTotalResponses();
            } catch (Exception ignored) {
                // Skip deleted or invalid forms if any
            }
        }

        Double averageCourseRating = null;
        if (!formIds.isEmpty()) {
            Double rawCourseAvg = answerRepository.findAverageRatingByFeedbackFormIds(formIds);
            averageCourseRating = roundToTwoDecimals(rawCourseAvg);
        }

        return CourseAnalyticsDto.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .courseCode(course.getCode())
                .totalFormsAssigned((long) formSummaries.size())
                .totalResponses(totalResponsesAcrossForms)
                .averageCourseRating(averageCourseRating)
                .formSummaries(formSummaries)
                .build();
    }

    private long calculateTotalTargetedStudents(List<FeedbackAssignment> assignments) {
        long count = 0;
        for (FeedbackAssignment assignment : assignments) {
            Long deptId = assignment.getDepartment() != null ? assignment.getDepartment().getId() : null;
            if (deptId == null) continue;

            String section = assignment.getSection();
            if (section != null && !section.trim().isEmpty() && !"ALL".equalsIgnoreCase(section.trim())) {
                count += studentProfileRepository.countByUserDepartmentIdAndSemesterAndSectionAndBatch(
                        deptId, assignment.getSemester(), section.trim(), assignment.getBatch());
            } else {
                count += studentProfileRepository.countByUserDepartmentIdAndSemesterAndBatch(
                        deptId, assignment.getSemester(), assignment.getBatch());
            }
        }
        return count;
    }

    private Double calculateCompletionRate(long totalResponses, long totalTargetedStudents) {
        if (totalTargetedStudents > 0) {
            double rate = (totalResponses * 100.0) / totalTargetedStudents;
            return roundToTwoDecimals(rate);
        } else if (totalResponses > 0) {
            return 100.0;
        }
        return 0.0;
    }

    private Double roundToTwoDecimals(Double value) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
