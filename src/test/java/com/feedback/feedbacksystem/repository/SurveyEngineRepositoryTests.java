package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.*;
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
class SurveyEngineRepositoryTests {

    @Autowired
    private FeedbackFormRepository feedbackFormRepository;

    @Autowired
    private FeedbackAssignmentRepository feedbackAssignmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Department dept;
    private User admin;
    private FeedbackForm form;

    @BeforeEach
    void setUp() {
        dept = departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder().name("Computer Science").code("CSE").build()
                ));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        admin = userRepository.save(User.builder()
                .name("Admin Tester")
                .email("admin.survey@college.edu")
                .password("pass123")
                .department(dept)
                .role(adminRole)
                .build());

        form = feedbackFormRepository.save(FeedbackForm.builder()
                .title("Mid-Term Faculty Feedback")
                .description("Feedback form for mid-semester evaluation")
                .category("FACULTY")
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().plusDays(10))
                .status(FormStatus.ACTIVE)
                .creator(admin)
                .build());
    }

    @Test
    void testFeedbackFormRepositoryFinders() {
        List<FeedbackForm> activeForms = feedbackFormRepository.findByStatus(FormStatus.ACTIVE);
        assertThat(activeForms).contains(form);

        List<FeedbackForm> creatorForms = feedbackFormRepository.findByCreatorId(admin.getId());
        assertThat(creatorForms).contains(form);

        List<FeedbackForm> nonDeletedForms = feedbackFormRepository.findByIsDeletedFalse();
        assertThat(nonDeletedForms).contains(form);

        List<FeedbackForm> activeInRange = feedbackFormRepository
                .findByStatusAndStartDateBeforeAndEndDateAfter(
                        FormStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        assertThat(activeInRange).contains(form);
    }

    @Test
    void testFeedbackAssignmentRepositoryFinders() {
        FeedbackAssignment assignment = feedbackAssignmentRepository.save(FeedbackAssignment.builder()
                .feedbackForm(form)
                .department(dept)
                .semester(6)
                .section("A")
                .batch("2023-2027")
                .academicYear("2025-2026")
                .build());

        List<FeedbackAssignment> formAssignments = feedbackAssignmentRepository.findByFeedbackFormId(form.getId());
        assertThat(formAssignments).contains(assignment);

        List<FeedbackAssignment> deptAssignments = feedbackAssignmentRepository
                .findByDepartmentIdAndSemesterAndSectionAndBatch(dept.getId(), 6, "A", "2023-2027");
        assertThat(deptAssignments).contains(assignment);
    }

    @Test
    void testQuestionAndOptionRepositoryFinders() {
        Question question = questionRepository.save(Question.builder()
                .feedbackForm(form)
                .questionText("Rate course content delivery")
                .questionType(QuestionType.RATING)
                .isMandatory(true)
                .displayOrder(1)
                .build());

        QuestionOption option = questionOptionRepository.save(QuestionOption.builder()
                .question(question)
                .optionValue("Excellent")
                .displayOrder(1)
                .isActive(true)
                .build());

        List<Question> questions = questionRepository.findByFeedbackFormIdOrderByDisplayOrderAsc(form.getId());
        assertThat(questions).contains(question);

        long questionCount = questionRepository.countByFeedbackFormId(form.getId());
        assertThat(questionCount).isEqualTo(1);

        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId());
        assertThat(options).contains(option);

        List<QuestionOption> activeOptions = questionOptionRepository.findByQuestionIdAndIsActiveTrue(question.getId());
        assertThat(activeOptions).contains(option);
    }
}
