package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.AdminSummaryAnalyticsDto;
import com.feedback.feedbacksystem.dto.CourseAnalyticsDto;
import com.feedback.feedbacksystem.dto.FacultyAnalyticsDto;
import com.feedback.feedbacksystem.dto.FormAnalyticsSummaryDto;

public interface AnalyticsService {

    FormAnalyticsSummaryDto getFormAnalytics(Long formId);

    CourseAnalyticsDto getCourseAnalytics(Long courseId);

    FacultyAnalyticsDto getFacultyAnalytics(Long facultyId);

    AdminSummaryAnalyticsDto getAdminSummaryAnalytics();
}
