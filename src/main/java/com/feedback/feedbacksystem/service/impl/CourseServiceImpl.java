package com.feedback.feedbacksystem.service.impl;

import com.feedback.feedbacksystem.dto.CourseResponseDto;
import com.feedback.feedbacksystem.dto.CreateCourseRequestDto;
import com.feedback.feedbacksystem.model.Course;
import com.feedback.feedbacksystem.model.Department;
import com.feedback.feedbacksystem.repository.CourseRepository;
import com.feedback.feedbacksystem.repository.DepartmentRepository;
import com.feedback.feedbacksystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public CourseResponseDto createCourse(CreateCourseRequestDto request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Course code already exists: " + request.getCode());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + request.getDepartmentId()));

        Course course = Course.builder()
                .name(request.getName())
                .code(request.getCode())
                .department(department)
                .active(true)
                .build();

        Course savedCourse = courseRepository.save(course);
        return mapToDto(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDto> getAllActiveCourses() {
        return courseRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDto getCourseByCode(String code) {
        Course course = courseRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with code: " + code));
        return mapToDto(course);
    }

    private CourseResponseDto mapToDto(Course course) {
        return CourseResponseDto.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .departmentName(course.getDepartment() != null ? course.getDepartment().getName() : null)
                .departmentCode(course.getDepartment() != null ? course.getDepartment().getCode() : null)
                .active(course.isActive())
                .build();
    }
}
