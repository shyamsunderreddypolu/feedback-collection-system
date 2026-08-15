package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.*;
import com.feedback.feedbacksystem.model.*;
import com.feedback.feedbacksystem.repository.*;
import com.feedback.feedbacksystem.service.AnalyticsService;
import com.feedback.feedbacksystem.service.FeedbackSubmissionService;
import com.feedback.feedbacksystem.service.ReportExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ControllerAndReportExportServiceTests {

    @Autowired
    private FeedbackSubmissionService feedbackSubmissionService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ReportExportService reportExportService;

    @Autowired
    private FeedbackFormRepository feedbackFormRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseAssignmentRepository courseAssignmentRepository;

    private User faculty;
    private User student;
    private Course course;
    private FeedbackForm activeForm;
    private Question ratingQuestion;
    private CourseAssignment assignment;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByCode("IT_TEST")
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Information Tech").code("IT_TEST").build()));

        Role facultyRole = roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_FACULTY").build()));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        faculty = userRepository.save(User.builder()
                .name("Dr. Johnson")
                .email("johnson_test@college.edu")
                .password("password")
                .role(facultyRole)
                .department(dept)
                .build());

        student = userRepository.save(User.builder()
                .name("Bob Student")
                .email("bob_test@college.edu")
                .password("password")
                .role(studentRole)
                .department(dept)
                .build());

        course = courseRepository.save(Course.builder()
                .name("Web Architecture")
                .code("CS401_TEST")
                .department(dept)
                .active(true)
                .build());

        assignment = courseAssignmentRepository.save(CourseAssignment.builder()
                .course(course)
                .faculty(faculty)
                .academicYear("2025-2026")
                .semester(6)
                .section("A")
                .status(AssignmentStatus.ACTIVE)
                .build());

        activeForm = feedbackFormRepository.save(FeedbackForm.builder()
                .title("Web Arch Midterm Form")
                .category("COURSE")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .status(FormStatus.ACTIVE)
                .creator(faculty)
                .build());

        ratingQuestion = questionRepository.save(Question.builder()
                .feedbackForm(activeForm)
                .questionText("Rate course assignments clarity")
                .questionType(QuestionType.RATING)
                .isMandatory(true)
                .displayOrder(1)
                .build());
    }

    @Test
    void testStudentSubmissionWorkflowAndHistory() {
        SubmitFeedbackResponseDto request = SubmitFeedbackResponseDto.builder()
                .feedbackFormId(activeForm.getId())
                .answers(List.of(
                        SubmitAnswerDto.builder().questionId(ratingQuestion.getId()).ratingValue(4).build()
                ))
                .build();

        FeedbackSubmissionResponseDto submissionResult = feedbackSubmissionService.submitFeedback(request, student.getId());
        assertThat(submissionResult.getResponseId()).isNotNull();

        boolean submittedForAssignment = feedbackSubmissionService.hasStudentSubmittedForAssignment(assignment.getId(), student.getId());
        assertThat(submittedForAssignment).isTrue();

        List<FeedbackSubmissionResponseDto> history = feedbackSubmissionService.getStudentSubmissionHistory(student.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getFormTitle()).isEqualTo("Web Arch Midterm Form");
    }

    @Test
    void testAnalyticsControllerServiceMethods() {
        FacultyAnalyticsDto facultyAnalytics = analyticsService.getFacultyAnalytics(faculty.getId());
        assertThat(facultyAnalytics.getFacultyId()).isEqualTo(faculty.getId());
        assertThat(facultyAnalytics.getFacultyName()).isEqualTo("Dr. Johnson");

        CourseAnalyticsDto courseAnalytics = analyticsService.getCourseAnalytics(course.getId());
        assertThat(courseAnalytics.getCourseId()).isEqualTo(course.getId());
        assertThat(courseAnalytics.getCourseCode()).isEqualTo("CS401_TEST");

        AdminSummaryAnalyticsDto adminSummary = analyticsService.getAdminSummaryAnalytics();
        assertThat(adminSummary.getTotalForms()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testReportExportPdfAndExcelGeneration() {
        byte[] pdfReport = reportExportService.generatePdfReport(activeForm.getId(), null, null);
        assertThat(pdfReport).isNotEmpty();
        assertThat(new String(pdfReport, 0, 4)).contains("%PDF");

        byte[] excelReport = reportExportService.generateExcelReport(null, course.getId(), null);
        assertThat(excelReport).isNotEmpty();
    }
}
