package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProfileRepositoryTests {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private FacultyProfileRepository facultyProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User studentUser;
    private User facultyUser;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder().name("Computer Science").code("CSE").build()
                ));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        Role facultyRole = roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_FACULTY").build()));

        studentUser = userRepository.save(User.builder()
                .name("Student Tester")
                .email("student.tester@college.edu")
                .password("pass123")
                .department(dept)
                .role(studentRole)
                .build());

        facultyUser = userRepository.save(User.builder()
                .name("Faculty Tester")
                .email("faculty.tester@college.edu")
                .password("pass123")
                .department(dept)
                .role(facultyRole)
                .build());
    }

    @Test
    void testStudentProfileCrudAndFinders() {
        // 1. Save Student Profile
        StudentProfile profile = StudentProfile.builder()
                .user(studentUser)
                .rollNumber("2024CSE101")
                .year(2)
                .semester(4)
                .section("B")
                .batch("2024-2028")
                .build();
        StudentProfile savedProfile = studentProfileRepository.save(profile);

        assertThat(savedProfile.getId()).isNotNull();

        // 2. Test findByRollNumber
        Optional<StudentProfile> foundByRoll = studentProfileRepository.findByRollNumber("2024CSE101");
        assertThat(foundByRoll).isPresent();
        assertThat(foundByRoll.get().getUser().getEmail()).isEqualTo("student.tester@college.edu");

        // 3. Test existsByRollNumber
        assertThat(studentProfileRepository.existsByRollNumber("2024CSE101")).isTrue();
        assertThat(studentProfileRepository.existsByRollNumber("NON_EXISTENT")).isFalse();

        // 4. Test findByYearAndSemesterAndSection
        List<StudentProfile> classList = studentProfileRepository.findByYearAndSemesterAndSection(2, 4, "B");
        assertThat(classList).contains(savedProfile);

        // 5. Test findByBatch
        List<StudentProfile> batchList = studentProfileRepository.findByBatch("2024-2028");
        assertThat(batchList).contains(savedProfile);

        // 6. Test findByUserId
        Optional<StudentProfile> foundByUserId = studentProfileRepository.findByUserId(studentUser.getId());
        assertThat(foundByUserId).isPresent();

        // 7. Delete
        studentProfileRepository.delete(savedProfile);
        assertThat(studentProfileRepository.findByRollNumber("2024CSE101")).isEmpty();
    }

    @Test
    void testFacultyProfileCrudAndFinders() {
        // 1. Save Faculty Profile
        FacultyProfile profile = FacultyProfile.builder()
                .user(facultyUser)
                .employeeId("EMP-CSE-999")
                .designation("Associate Professor")
                .joiningDate(LocalDate.of(2021, 8, 1))
                .build();
        FacultyProfile savedProfile = facultyProfileRepository.save(profile);

        assertThat(savedProfile.getId()).isNotNull();

        // 2. Test findByEmployeeId
        Optional<FacultyProfile> foundByEmpId = facultyProfileRepository.findByEmployeeId("EMP-CSE-999");
        assertThat(foundByEmpId).isPresent();
        assertThat(foundByEmpId.get().getUser().getName()).isEqualTo("Faculty Tester");

        // 3. Test existsByEmployeeId
        assertThat(facultyProfileRepository.existsByEmployeeId("EMP-CSE-999")).isTrue();
        assertThat(facultyProfileRepository.existsByEmployeeId("INVALID_ID")).isFalse();

        // 4. Test findByDesignation
        List<FacultyProfile> professors = facultyProfileRepository.findByDesignation("Associate Professor");
        assertThat(professors).contains(savedProfile);

        // 5. Test findByUserId
        Optional<FacultyProfile> foundByUserId = facultyProfileRepository.findByUserId(facultyUser.getId());
        assertThat(foundByUserId).isPresent();

        // 6. Delete
        facultyProfileRepository.delete(savedProfile);
        assertThat(facultyProfileRepository.findByEmployeeId("EMP-CSE-999")).isEmpty();
    }
}
