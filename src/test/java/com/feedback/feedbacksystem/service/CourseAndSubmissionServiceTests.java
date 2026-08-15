package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CourseAndSubmissionServiceTests {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseAssignmentService courseAssignmentService;

    @Autowired
    private FeedbackSubmissionService feedbackSubmissionService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FeedbackFormRepository feedbackFormRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    private Department dept;
    private User faculty;
    private User student;
    private FeedbackForm activeForm;
    private Question ratingQuestion;
    private Question textQuestion;
    private Question choiceQuestion;
    private QuestionOption option1;

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByCode("CSE_TEST")
                .orElseGet(() -> departmentRepository.save(Department.builder().name("CSE Test Dept").code("CSE_TEST").build()));

        Role facultyRole = roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_FACULTY").build()));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        faculty = userRepository.save(User.builder()
                .name("Dr. Smith")
                .email("smith_test@college.edu")
                .password("password")
                .role(facultyRole)
                .department(dept)
                .build());

        student = userRepository.save(User.builder()
                .name("Alice Student")
                .email("alice_test@college.edu")
                .password("password")
                .role(studentRole)
                .department(dept)
                .build());

        activeForm = feedbackFormRepository.save(FeedbackForm.builder()
                .title("Active Midterm Form")
                .category("COURSE")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .status(FormStatus.ACTIVE)
                .creator(faculty)
                .build());

        ratingQuestion = questionRepository.save(Question.builder()
                .feedbackForm(activeForm)
                .questionText("Rate course organization")
                .questionType(QuestionType.RATING)
                .isMandatory(true)
                .displayOrder(1)
                .build());

        textQuestion = questionRepository.save(Question.builder()
                .feedbackForm(activeForm)
                .questionText("Feedback comments")
                .questionType(QuestionType.TEXT)
                .isMandatory(false)
                .displayOrder(2)
                .build());

        choiceQuestion = questionRepository.save(Question.builder()
                .feedbackForm(activeForm)
                .questionText("Pacing option")
                .questionType(QuestionType.RADIO)
                .isMandatory(true)
                .displayOrder(3)
                .build());

        option1 = questionOptionRepository.save(QuestionOption.builder()
                .question(choiceQuestion)
                .optionValue("Pace is perfect")
                .displayOrder(1)
                .isActive(true)
                .build());
    }

    @Test
    void testCreateCourseAndDuplicateCodeValidation() {
        CreateCourseRequestDto createDto = CreateCourseRequestDto.builder()
                .name("Operating Systems")
                .code("CS305_UNIQUE")
                .departmentId(dept.getId())
                .build();

        CourseResponseDto response = courseService.createCourse(createDto);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCode()).isEqualTo("CS305_UNIQUE");
        assertThat(response.getDepartmentName()).isEqualTo("CSE Test Dept");

        assertThatThrownBy(() -> courseService.createCourse(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Course code already exists");
    }

    @Test
    void testGetAllActiveCoursesAndGetByCode() {
        courseService.createCourse(CreateCourseRequestDto.builder()
                .name("Algorithms")
                .code("CS306_TEST")
                .departmentId(dept.getId())
                .build());

        List<CourseResponseDto> activeCourses = courseService.getAllActiveCourses();
        assertThat(activeCourses).extracting("code").contains("CS306_TEST");

        CourseResponseDto fetched = courseService.getCourseByCode("CS306_TEST");
        assertThat(fetched.getName()).isEqualTo("Algorithms");
    }

    @Test
    void testAssignFacultyToCourseAndValidations() {
        CourseResponseDto courseDto = courseService.createCourse(CreateCourseRequestDto.builder()
                .name("Database Systems")
                .code("CS307_TEST")
                .departmentId(dept.getId())
                .build());

        CreateCourseAssignmentDto assignDto = CreateCourseAssignmentDto.builder()
                .courseId(courseDto.getId())
                .facultyId(faculty.getId())
                .academicYear("2025-2026")
                .semester(5)
                .section("A")
                .build();

        CourseAssignmentResponseDto assignmentResponse = courseAssignmentService.assignFacultyToCourse(assignDto);
        assertThat(assignmentResponse.getId()).isNotNull();
        assertThat(assignmentResponse.getCourseCode()).isEqualTo("CS307_TEST");
        assertThat(assignmentResponse.getFacultyName()).isEqualTo("Dr. Smith");

        // Test duplicate assignment constraint
        assertThatThrownBy(() -> courseAssignmentService.assignFacultyToCourse(assignDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate assignment");

        // Test Non-faculty user role validation
        CreateCourseAssignmentDto invalidRoleDto = CreateCourseAssignmentDto.builder()
                .courseId(courseDto.getId())
                .facultyId(student.getId()) // Student ID used instead of faculty
                .academicYear("2025-2026")
                .semester(5)
                .section("B")
                .build();

        assertThatThrownBy(() -> courseAssignmentService.assignFacultyToCourse(invalidRoleDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have ROLE_FACULTY");
    }

    @Test
    void testSubmitFeedbackAndDuplicateSubmissionConstraint() {
        SubmitFeedbackResponseDto request = SubmitFeedbackResponseDto.builder()
                .feedbackFormId(activeForm.getId())
                .answers(List.of(
                        SubmitAnswerDto.builder().questionId(ratingQuestion.getId()).ratingValue(5).build(),
                        SubmitAnswerDto.builder().questionId(textQuestion.getId()).textValue("Great module").build(),
                        SubmitAnswerDto.builder().questionId(choiceQuestion.getId()).selectedOptionId(option1.getId()).build()
                ))
                .build();

        FeedbackSubmissionResponseDto response = feedbackSubmissionService.submitFeedback(request, student.getId());
        assertThat(response.getResponseId()).isNotNull();
        assertThat(response.getFormTitle()).isEqualTo("Active Midterm Form");
        assertThat(response.getTotalAnswersCount()).isEqualTo(3);

        assertThat(feedbackSubmissionService.hasStudentSubmitted(activeForm.getId(), student.getId())).isTrue();

        // Duplicate submission should fail
        assertThatThrownBy(() -> feedbackSubmissionService.submitFeedback(request, student.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void testSubmitFeedbackMandatoryQuestionValidation() {
        // Missing choiceQuestion which is mandatory
        SubmitFeedbackResponseDto incompleteRequest = SubmitFeedbackResponseDto.builder()
                .feedbackFormId(activeForm.getId())
                .answers(List.of(
                        SubmitAnswerDto.builder().questionId(ratingQuestion.getId()).ratingValue(4).build()
                ))
                .build();

        assertThatThrownBy(() -> feedbackSubmissionService.submitFeedback(incompleteRequest, student.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mandatory question was not answered");
    }
}
