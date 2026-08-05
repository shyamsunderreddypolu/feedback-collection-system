package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CourseResponseDto;
import com.feedback.feedbacksystem.dto.CreateCourseRequestDto;

import java.util.List;

public interface CourseService {

    CourseResponseDto createCourse(CreateCourseRequestDto request);

    List<CourseResponseDto> getAllActiveCourses();

    CourseResponseDto getCourseByCode(String code);
}
