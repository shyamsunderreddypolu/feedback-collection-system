package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CourseAssignmentResponseDto;
import com.feedback.feedbacksystem.dto.CreateCourseAssignmentDto;
import com.feedback.feedbacksystem.service.CourseAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-assignments")
@RequiredArgsConstructor
public class CourseAssignmentController {

    private final CourseAssignmentService courseAssignmentService;

    @PostMapping
    public ResponseEntity<CourseAssignmentResponseDto> assignFacultyToCourse(@Valid @RequestBody CreateCourseAssignmentDto request) {
        CourseAssignmentResponseDto assigned = courseAssignmentService.assignFacultyToCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<CourseAssignmentResponseDto>> getAssignmentsByFaculty(@PathVariable Long facultyId) {
        List<CourseAssignmentResponseDto> assignments = courseAssignmentService.getAssignmentsByFaculty(facultyId);
        return ResponseEntity.ok(assignments);
    }
}
