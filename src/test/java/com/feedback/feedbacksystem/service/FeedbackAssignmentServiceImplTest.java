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
import com.feedback.feedbacksystem.model.FormStatus;
import com.feedback.feedbacksystem.repository.CourseRepository;
import com.feedback.feedbacksystem.repository.DepartmentRepository;
import com.feedback.feedbacksystem.repository.FeedbackAssignmentRepository;
import com.feedback.feedbacksystem.repository.FeedbackFormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackAssignmentServiceImplTest {

    @Mock
    private FeedbackAssignmentRepository feedbackAssignmentRepository;
    @Mock
    private FeedbackFormRepository feedbackFormRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private FeedbackAssignmentServiceImpl service;

    private FeedbackForm form;
    private Department department;
    private Course course;

    @BeforeEach
    void setUp() {
        form = FeedbackForm.builder().id(1L).title("Course Feedback").status(FormStatus.ACTIVE).build();
        department = Department.builder().id(2L).name("Computer Science").code("CSE").build();
        course = Course.builder().id(3L).name("Operating Systems").code("CSE301").department(department).build();
    }

    private CreateFeedbackAssignmentDto request() {
        return CreateFeedbackAssignmentDto.builder()
                .feedbackFormId(1L)
                .departmentId(2L)
                .courseId(3L)
                .semester(6)
                .section("A")
                .batch("2023-2027")
                .academicYear("2025-2026")
                .build();
    }

    private FeedbackAssignment existingAssignment() {
        return FeedbackAssignment.builder()
                .id(40L)
                .feedbackForm(form)
                .department(department)
                .course(course)
                .semester(6)
                .section("A")
                .batch("2023-2027")
                .academicYear("2025-2026")
                .build();
    }

    private void stubLookups() {
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(department));
    }

    @Test
    @DisplayName("assignForm targets a course and returns the resolved names")
    void assignsCourseTarget() {
        stubLookups();
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(feedbackAssignmentRepository.findByFeedbackFormId(1L)).thenReturn(List.of());
        when(feedbackAssignmentRepository.save(any(FeedbackAssignment.class))).thenAnswer(invocation -> {
            FeedbackAssignment saved = invocation.getArgument(0);
            saved.setId(40L);
            return saved;
        });

        FeedbackAssignmentResponseDto response = service.assignForm(request());

        assertThat(response.getId()).isEqualTo(40L);
        assertThat(response.getFormTitle()).isEqualTo("Course Feedback");
        assertThat(response.getDepartmentName()).isEqualTo("Computer Science");
        assertThat(response.getCourseName()).isEqualTo("Operating Systems");
        assertThat(response.getCourseCode()).isEqualTo("CSE301");
        assertThat(response.getSemester()).isEqualTo(6);
        assertThat(response.getBatch()).isEqualTo("2023-2027");
    }

    @Test
    @DisplayName("assignForm allows a department wide target with no course")
    void assignsDepartmentWideTarget() {
        stubLookups();
        when(feedbackAssignmentRepository.findByFeedbackFormId(1L)).thenReturn(List.of());
        when(feedbackAssignmentRepository.save(any(FeedbackAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateFeedbackAssignmentDto request = request();
        request.setCourseId(null);

        FeedbackAssignmentResponseDto response = service.assignForm(request);

        assertThat(response.getCourseName()).isNull();
        assertThat(response.getCourseCode()).isNull();
        verify(courseRepository, never()).findById(any());
    }

    @Test
    @DisplayName("assignForm rejects a duplicate target")
    void rejectsDuplicateAssignment() {
        stubLookups();
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(feedbackAssignmentRepository.findByFeedbackFormId(1L)).thenReturn(List.of(existingAssignment()));

        assertThatThrownBy(() -> service.assignForm(request()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already assigned");

        verify(feedbackAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignForm treats two department wide targets as duplicates")
    void rejectsDuplicateDepartmentWideAssignment() {
        stubLookups();
        FeedbackAssignment existing = existingAssignment();
        existing.setCourse(null);
        when(feedbackAssignmentRepository.findByFeedbackFormId(1L)).thenReturn(List.of(existing));

        CreateFeedbackAssignmentDto request = request();
        request.setCourseId(null);

        assertThatThrownBy(() -> service.assignForm(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("assignForm keeps a different section as a separate target")
    void allowsDifferentSection() {
        stubLookups();
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(feedbackAssignmentRepository.findByFeedbackFormId(1L)).thenReturn(List.of(existingAssignment()));
        when(feedbackAssignmentRepository.save(any(FeedbackAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateFeedbackAssignmentDto request = request();
        request.setSection("B");

        assertThat(service.assignForm(request).getSection()).isEqualTo("B");
    }

    @Test
    @DisplayName("assignForm rejects an unknown department")
    void rejectsUnknownDepartment() {
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(departmentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignForm(request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found with id: 2");
    }

    @Test
    @DisplayName("assignForm rejects an unknown course")
    void rejectsUnknownCourse() {
        stubLookups();
        when(courseRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignForm(request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 3");
    }

    @Test
    @DisplayName("assignForm rejects a soft deleted form")
    void rejectsDeletedForm() {
        form.setDeleted(true);
        when(feedbackFormRepository.findById(1L)).thenReturn(Optional.of(form));

        assertThatThrownBy(() -> service.assignForm(request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("FeedbackForm not found with id: 1");
    }

    @Test
    @DisplayName("assignForm rejects a missing section before touching the database")
    void rejectsMissingSection() {
        CreateFeedbackAssignmentDto request = request();
        request.setSection(null);

        assertThatThrownBy(() -> service.assignForm(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("section is required");

        verify(feedbackFormRepository, never()).findById(any());
    }
}
