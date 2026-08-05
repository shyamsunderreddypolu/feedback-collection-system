package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.CourseResponseDto;
import com.feedback.feedbacksystem.dto.CreateCourseRequestDto;
import com.feedback.feedbacksystem.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CreateCourseRequestDto request) {
        CourseResponseDto created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CourseResponseDto>> getAllActiveCourses() {
        List<CourseResponseDto> courses = courseService.getAllActiveCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CourseResponseDto> getCourseByCode(@PathVariable String code) {
        CourseResponseDto course = courseService.getCourseByCode(code);
        return ResponseEntity.ok(course);
    }
}
