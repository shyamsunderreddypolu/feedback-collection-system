package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CourseAnalyticsDto;
import com.feedback.feedbacksystem.dto.FormAnalyticsSummaryDto;

public interface AnalyticsService {

    FormAnalyticsSummaryDto getFormAnalytics(Long formId);

    CourseAnalyticsDto getCourseAnalytics(Long courseId);
}
