package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CourseAndSubmissionRepositoryTests {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseAssignmentRepository courseAssignmentRepository;

    @Autowired
    private ResponseRepository responseRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FeedbackFormRepository feedbackFormRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    private Department dept;
    private User faculty;
    private User student;
    private Course course;
    private FeedbackForm form;
    private Question question;
    private QuestionOption option;

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder().name("Computer Science").code("CSE").build()
                ));

        Role facultyRole = roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_FACULTY").build()));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        faculty = userRepository.save(User.builder()
                .name("Prof. Alan")
                .email("alan@college.edu")
                .password("pass123")
                .department(dept)
                .role(facultyRole)
                .build());

        student = userRepository.save(User.builder()
                .name("Jane Student")
                .email("jane@college.edu")
                .password("pass123")
                .department(dept)
                .role(studentRole)
                .build());

        course = courseRepository.save(Course.builder()
                .name("Data Structures")
                .code("CS201")
                .department(dept)
                .active(true)
                .build());

        form = feedbackFormRepository.save(FeedbackForm.builder()
                .title("DS Mid-Term Evaluation")
                .category("COURSE")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .status(FormStatus.ACTIVE)
                .creator(faculty)
                .build());

        question = questionRepository.save(Question.builder()
                .feedbackForm(form)
                .questionText("How is the course pace?")
                .questionType(QuestionType.RADIO)
                .isMandatory(true)
                .displayOrder(1)
                .build());

        option = questionOptionRepository.save(QuestionOption.builder()
                .question(question)
                .optionValue("Good Pace")
                .displayOrder(1)
                .isActive(true)
                .build());
    }

    @Test
    void testCourseRepositoryFinders() {
        Optional<Course> foundByCode = courseRepository.findByCode("CS201");
        assertThat(foundByCode).isPresent();
        assertThat(foundByCode.get().getName()).isEqualTo("Data Structures");

        List<Course> deptCourses = courseRepository.findByDepartmentId(dept.getId());
        assertThat(deptCourses).contains(course);

        List<Course> activeCourses = courseRepository.findByActiveTrue();
        assertThat(activeCourses).contains(course);

        assertThat(courseRepository.existsByCode("CS201")).isTrue();
    }

    @Test
    void testCourseAssignmentRepositoryFinders() {
        CourseAssignment assignment = courseAssignmentRepository.save(CourseAssignment.builder()
                .course(course)
                .faculty(faculty)
                .academicYear("2025-2026")
                .semester(4)
                .section("A")
                .status(AssignmentStatus.ACTIVE)
                .build());

        List<CourseAssignment> facultyAssignments = courseAssignmentRepository.findByFacultyId(faculty.getId());
        assertThat(facultyAssignments).contains(assignment);

        List<CourseAssignment> courseAssignments = courseAssignmentRepository.findByCourseId(course.getId());
        assertThat(courseAssignments).contains(assignment);

        List<CourseAssignment> sectionAssignments = courseAssignmentRepository
                .findByAcademicYearAndSemesterAndSection("2025-2026", 4, "A");
        assertThat(sectionAssignments).contains(assignment);
    }

    @Test
    void testResponseAndAnswerRepositoryFinders() {
        // 1. Submit Response
        Response response = responseRepository.save(Response.builder()
                .feedbackForm(form)
                .submitter(student)
                .build());

        assertThat(response.getId()).isNotNull();

        List<Response> submitterResponses = responseRepository.findBySubmitterId(student.getId());
        assertThat(submitterResponses).contains(response);

        List<Response> formResponses = responseRepository.findByFeedbackFormId(form.getId());
        assertThat(formResponses).contains(response);

        assertThat(responseRepository.existsByFeedbackFormIdAndSubmitterId(form.getId(), student.getId())).isTrue();
        assertThat(responseRepository.countByFeedbackFormId(form.getId())).isEqualTo(1);

        // 2. Submit Answer
        Answer answer = answerRepository.save(Answer.builder()
                .response(response)
                .question(question)
                .selectedOption(option)
                .build());

        List<Answer> responseAnswers = answerRepository.findByResponseId(response.getId());
        assertThat(responseAnswers).contains(answer);

        List<Answer> questionAnswers = answerRepository.findByQuestionId(question.getId());
        assertThat(questionAnswers).contains(answer);

        long count = answerRepository.countByQuestionIdAndSelectedOptionId(question.getId(), option.getId());
        assertThat(count).isEqualTo(1);
    }
}
