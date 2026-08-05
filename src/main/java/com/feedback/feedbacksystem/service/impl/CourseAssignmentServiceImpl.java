package com.feedback.feedbacksystem.service.impl;

import com.feedback.feedbacksystem.dto.CourseAssignmentResponseDto;
import com.feedback.feedbacksystem.dto.CreateCourseAssignmentDto;
import com.feedback.feedbacksystem.model.AssignmentStatus;
import com.feedback.feedbacksystem.model.Course;
import com.feedback.feedbacksystem.model.CourseAssignment;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.CourseAssignmentRepository;
import com.feedback.feedbacksystem.repository.CourseRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import com.feedback.feedbacksystem.service.CourseAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseAssignmentServiceImpl implements CourseAssignmentService {

    private final CourseAssignmentRepository courseAssignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public CourseAssignmentResponseDto assignFacultyToCourse(CreateCourseAssignmentDto request) {
        User faculty = userRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new IllegalArgumentException("Faculty user not found with id: " + request.getFacultyId()));

        if (faculty.getRole() == null || !"ROLE_FACULTY".equalsIgnoreCase(faculty.getRole().getName())) {
            throw new IllegalArgumentException("Target user must have ROLE_FACULTY assigned");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + request.getCourseId()));

        List<CourseAssignment> existingAssignments = courseAssignmentRepository.findByCourseId(request.getCourseId());
        boolean isDuplicate = existingAssignments.stream().anyMatch(a ->
                a.getFaculty() != null && a.getFaculty().getId().equals(request.getFacultyId()) &&
                a.getAcademicYear() != null && a.getAcademicYear().equalsIgnoreCase(request.getAcademicYear()) &&
                a.getSemester() == request.getSemester() &&
                a.getSection() != null && a.getSection().equalsIgnoreCase(request.getSection())
        );

        if (isDuplicate) {
            throw new IllegalArgumentException("Duplicate assignment: Faculty is already assigned to this course section for the specified academic year");
        }

        CourseAssignment assignment = CourseAssignment.builder()
                .course(course)
                .faculty(faculty)
                .academicYear(request.getAcademicYear())
                .semester(request.getSemester())
                .section(request.getSection())
                .status(AssignmentStatus.ACTIVE)
                .build();

        CourseAssignment savedAssignment = courseAssignmentRepository.save(assignment);
        return mapToDto(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseAssignmentResponseDto> getAssignmentsByFaculty(Long facultyId) {
        if (!userRepository.existsById(facultyId)) {
            throw new IllegalArgumentException("Faculty user not found with id: " + facultyId);
        }

        List<CourseAssignment> assignments = courseAssignmentRepository.findByFacultyId(facultyId);
        return assignments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CourseAssignmentResponseDto mapToDto(CourseAssignment assignment) {
        return CourseAssignmentResponseDto.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourse() != null ? assignment.getCourse().getId() : null)
                .courseName(assignment.getCourse() != null ? assignment.getCourse().getName() : null)
                .courseCode(assignment.getCourse() != null ? assignment.getCourse().getCode() : null)
                .facultyId(assignment.getFaculty() != null ? assignment.getFaculty().getId() : null)
                .facultyName(assignment.getFaculty() != null ? assignment.getFaculty().getName() : null)
                .academicYear(assignment.getAcademicYear())
                .semester(assignment.getSemester())
                .section(assignment.getSection())
                .status(assignment.getStatus() != null ? assignment.getStatus().name() : null)
                .build();
    }
}
