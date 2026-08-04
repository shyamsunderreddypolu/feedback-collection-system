package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CourseAssignmentResponseDto;
import com.feedback.feedbacksystem.dto.CreateCourseAssignmentDto;

import java.util.List;

public interface CourseAssignmentService {

    CourseAssignmentResponseDto assignFacultyToCourse(CreateCourseAssignmentDto request);

    List<CourseAssignmentResponseDto> getAssignmentsByFaculty(Long facultyId);
}
