package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CreateFeedbackAssignmentDto;
import com.feedback.feedbacksystem.dto.FeedbackAssignmentResponseDto;
import com.feedback.feedbacksystem.exception.BusinessRuleViolationException;
import com.feedback.feedbacksystem.exception.DuplicateResourceException;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.Course;
import com.feedback.feedbacksystem.model.Department;
import com.feedback.feedbacksystem.model.FeedbackAssignment;
import com.feedback.feedbacksystem.model.FeedbackForm;
import com.feedback.feedbacksystem.repository.CourseRepository;
import com.feedback.feedbacksystem.repository.DepartmentRepository;
import com.feedback.feedbacksystem.repository.FeedbackAssignmentRepository;
import com.feedback.feedbacksystem.repository.FeedbackFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackAssignmentServiceImpl implements FeedbackAssignmentService {

    private final FeedbackAssignmentRepository feedbackAssignmentRepository;
    private final FeedbackFormRepository feedbackFormRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    @Override
    public FeedbackAssignmentResponseDto assignForm(CreateFeedbackAssignmentDto request) {
        requireText(request.getSection(), "section");
        requireText(request.getBatch(), "batch");
        requireText(request.getAcademicYear(), "academicYear");

        FeedbackForm form = feedbackFormRepository.findById(request.getFeedbackFormId())
                .orElseThrow(() -> new ResourceNotFoundException("FeedbackForm", request.getFeedbackFormId()));
        if (form.isDeleted()) {
            throw new ResourceNotFoundException("FeedbackForm", request.getFeedbackFormId());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));
        }

        requireNotAlreadyAssigned(request);

        FeedbackAssignment assignment = feedbackAssignmentRepository.save(FeedbackAssignment.builder()
                .feedbackForm(form)
                .department(department)
                .course(course)
                .semester(request.getSemester())
                .section(request.getSection().trim())
                .batch(request.getBatch().trim())
                .academicYear(request.getAcademicYear().trim())
                .build());

        return toResponseDto(assignment, form, department, course);
    }

    /**
     * Mirrors the uk_feedback_assignment unique constraint in Java so callers get a
     * readable error. Compared in memory because a derived query cannot match a
     * null courseId, which is legal for department wide assignments.
     */
    private void requireNotAlreadyAssigned(CreateFeedbackAssignmentDto request) {
        boolean exists = feedbackAssignmentRepository.findByFeedbackFormId(request.getFeedbackFormId()).stream()
                .anyMatch(existing -> Objects.equals(existing.getDepartment().getId(), request.getDepartmentId())
                        && Objects.equals(courseIdOf(existing), request.getCourseId())
                        && existing.getSemester() == request.getSemester()
                        && equalsTrimmed(existing.getSection(), request.getSection())
                        && equalsTrimmed(existing.getBatch(), request.getBatch())
                        && equalsTrimmed(existing.getAcademicYear(), request.getAcademicYear()));

        if (exists) {
            throw new DuplicateResourceException("Form " + request.getFeedbackFormId()
                    + " is already assigned to department " + request.getDepartmentId()
                    + ", course " + request.getCourseId()
                    + ", semester " + request.getSemester()
                    + ", section " + request.getSection()
                    + ", batch " + request.getBatch()
                    + ", academic year " + request.getAcademicYear());
        }
    }

    /** Reads the FK off the lazy proxy without loading the course row. */
    private Long courseIdOf(FeedbackAssignment assignment) {
        return assignment.getCourse() == null ? null : assignment.getCourse().getId();
    }

    private boolean equalsTrimmed(String stored, String requested) {
        return stored != null && requested != null && stored.equalsIgnoreCase(requested.trim());
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessRuleViolationException(fieldName + " is required to target an assignment");
        }
    }

    private FeedbackAssignmentResponseDto toResponseDto(FeedbackAssignment assignment,
                                                        FeedbackForm form,
                                                        Department department,
                                                        Course course) {
        return FeedbackAssignmentResponseDto.builder()
                .id(assignment.getId())
                .feedbackFormId(form.getId())
                .formTitle(form.getTitle())
                .departmentName(department.getName())
                .courseName(course == null ? null : course.getName())
                .courseCode(course == null ? null : course.getCode())
                .semester(assignment.getSemester())
                .section(assignment.getSection())
                .batch(assignment.getBatch())
                .academicYear(assignment.getAcademicYear())
                .build();
    }
}
