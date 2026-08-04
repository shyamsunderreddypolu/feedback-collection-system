package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CourseAnalyticsDto;
import com.feedback.feedbacksystem.dto.FormAnalyticsSummaryDto;
import com.feedback.feedbacksystem.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/forms/{formId}")
    public ResponseEntity<FormAnalyticsSummaryDto> getFormAnalytics(@PathVariable Long formId) {
        FormAnalyticsSummaryDto analytics = analyticsService.getFormAnalytics(formId);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseAnalyticsDto> getCourseAnalytics(@PathVariable Long courseId) {
        CourseAnalyticsDto analytics = analyticsService.getCourseAnalytics(courseId);
        return ResponseEntity.ok(analytics);
    }
}
