package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.CourseAnalyticsDto;
import com.feedback.feedbacksystem.dto.FormAnalyticsSummaryDto;
import com.feedback.feedbacksystem.dto.NotificationResponseDto;
import com.feedback.feedbacksystem.model.*;
import com.feedback.feedbacksystem.repository.*;
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
class AnalyticsAndNotificationServiceTests {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FeedbackFormRepository feedbackFormRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private FeedbackAssignmentRepository feedbackAssignmentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    private User admin;
    private User student;
    private Department cseDept;
    private Course course;
    private FeedbackForm form;
    private Question ratingQuestion;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        cseDept = departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(Department.builder().name("Computer Science").code("CSE").build()));

        admin = userRepository.save(User.builder()
                .name("Admin")
                .email("admin_test@college.edu")
                .password("password")
                .role(adminRole)
                .department(cseDept)
                .build());

        student = userRepository.save(User.builder()
                .name("Student 1")
                .email("student1_test@college.edu")
                .password("password")
                .role(studentRole)
                .department(cseDept)
                .build());

        studentProfileRepository.save(StudentProfile.builder()
                .user(student)
                .rollNumber("CSE2025-001")
                .year(3)
                .semester(6)
                .section("A")
                .batch("2023-2027")
                .build());

        course = courseRepository.save(Course.builder()
                .name("Software Engineering")
                .code("CS301_TEST")
                .department(cseDept)
                .active(true)
                .build());

        form = feedbackFormRepository.save(FeedbackForm.builder()
                .title("Mid Term SE Survey")
                .category("COURSE")
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().plusDays(5))
                .status(FormStatus.ACTIVE)
                .creator(admin)
                .build());

        ratingQuestion = questionRepository.save(Question.builder()
                .feedbackForm(form)
                .questionText("Rate course content clarity")
                .questionType(QuestionType.RATING)
                .isMandatory(true)
                .displayOrder(1)
                .build());

        feedbackAssignmentRepository.save(FeedbackAssignment.builder()
                .feedbackForm(form)
                .department(cseDept)
                .course(course)
                .semester(6)
                .section("A")
                .batch("2023-2027")
                .academicYear("2025-2026")
                .build());
    }

    @Test
    void testSendNotificationAndGetUserNotificationsAndMarkAsRead() {
        notificationService.sendNotification(
                student.getId(),
                "New Form Assigned",
                "Please submit SE Survey",
                NotificationType.SURVEY_ASSIGNED,
                NotificationPriority.HIGH
        );

        List<NotificationResponseDto> notifications = notificationService.getUserNotifications(student.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getTitle()).isEqualTo("New Form Assigned");
        assertThat(notifications.get(0).isRead()).isFalse();

        Long notificationId = notifications.get(0).getId();
        notificationService.markAsRead(notificationId);

        List<NotificationResponseDto> updatedNotifications = notificationService.getUserNotifications(student.getId());
        assertThat(updatedNotifications.get(0).isRead()).isTrue();
    }

    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void testAuditLogService() throws InterruptedException {
        User savedAdmin = userRepository.save(User.builder()
                .name("Admin Async")
                .email("admin_async@college.edu")
                .password("password")
                .role(roleRepository.findByName("ROLE_ADMIN").orElseThrow())
                .department(cseDept)
                .build());

        auditLogService.logAction("CREATE_FORM", "FeedbackForm", form.getId(), savedAdmin.getId(), "Created SE survey form");

        Thread.sleep(200);

        List<AuditLog> logs = auditLogRepository.findByPerformedByIdOrderByPerformedAtDesc(savedAdmin.getId());
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getAction()).isEqualTo("CREATE_FORM");

        auditLogRepository.deleteAll(logs);
        userRepository.delete(savedAdmin);
    }

    @Test
    void testAnalyticsServiceFormAndCourseAnalytics() {
        // Submit response and answer
        Response response = responseRepository.save(Response.builder()
                .feedbackForm(form)
                .submitter(student)
                .build());

        answerRepository.save(Answer.builder()
                .response(response)
                .question(ratingQuestion)
                .ratingValue(5)
                .build());

        FormAnalyticsSummaryDto formAnalytics = analyticsService.getFormAnalytics(form.getId());
        assertThat(formAnalytics.getFormId()).isEqualTo(form.getId());
        assertThat(formAnalytics.getTotalResponses()).isEqualTo(1);
        assertThat(formAnalytics.getTotalTargetedStudents()).isEqualTo(1);
        assertThat(formAnalytics.getCompletionRate()).isEqualTo(100.0);
        assertThat(formAnalytics.getOverallAverageRating()).isEqualTo(5.0);
        assertThat(formAnalytics.getQuestionRatings()).hasSize(1);
        assertThat(formAnalytics.getQuestionRatings().get(0).getAverageRating()).isEqualTo(5.0);

        CourseAnalyticsDto courseAnalytics = analyticsService.getCourseAnalytics(course.getId());
        assertThat(courseAnalytics.getCourseId()).isEqualTo(course.getId());
        assertThat(courseAnalytics.getTotalFormsAssigned()).isEqualTo(1);
        assertThat(courseAnalytics.getTotalResponses()).isEqualTo(1);
        assertThat(courseAnalytics.getAverageCourseRating()).isEqualTo(5.0);
    }
}
