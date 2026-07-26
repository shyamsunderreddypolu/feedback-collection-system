package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // Automatically rolls back database modifications after each test execution
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RepositoryCrudTests {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseAssignmentRepository courseAssignmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void testDepartmentCrudOperations() {
        // 1. Create & Save Department
        Department dept = Department.builder()
                .name("Mechanical Engineering")
                .code("MECH")
                .active(true)
                .build();
        Department savedDept = departmentRepository.save(dept);

        assertThat(savedDept.getId()).isNotNull();
        assertThat(savedDept.getName()).isEqualTo("Mechanical Engineering");

        // 2. Read Finder Methods
        Optional<Department> foundByName = departmentRepository.findByName("Mechanical Engineering");
        assertThat(foundByName).isPresent();
        assertThat(foundByName.get().getCode()).isEqualTo("MECH");

        Optional<Department> foundByCode = departmentRepository.findByCode("MECH");
        assertThat(foundByCode).isPresent();

        boolean existsName = departmentRepository.existsByName("Mechanical Engineering");
        assertThat(existsName).isTrue();

        boolean existsCode = departmentRepository.existsByCode("MECH");
        assertThat(existsCode).isTrue();

        // 3. Update active flag
        savedDept.setActive(false);
        Department updatedDept = departmentRepository.save(savedDept);
        assertThat(updatedDept.isActive()).isFalse();

        // 4. Delete Department
        departmentRepository.delete(updatedDept);
        Optional<Department> deletedDept = departmentRepository.findByCode("MECH");
        assertThat(deletedDept).isEmpty();
    }

    @Test
    void testRoleCrudOperations() {
        // 1. Create & Save Role
        Role role = Role.builder()
                .name("ROLE_MODERATOR")
                .build();
        Role savedRole = roleRepository.save(role);

        assertThat(savedRole.getId()).isNotNull();

        // 2. Finder Methods
        Optional<Role> foundByName = roleRepository.findByName("ROLE_MODERATOR");
        assertThat(foundByName).isPresent();

        boolean exists = roleRepository.existsByName("ROLE_MODERATOR");
        assertThat(exists).isTrue();

        // 3. Delete Role
        roleRepository.delete(savedRole);
        Optional<Role> deletedRole = roleRepository.findByName("ROLE_MODERATOR");
        assertThat(deletedRole).isEmpty();
    }

    @Test
    void testUserCrudOperations() {
        // Fetch pre-seeded dependency objects or create temporary ones for testing
        Department dept = departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder().name("Computer Science").code("CSE").build()
                ));

        Role role = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_STUDENT").build()
                ));

        // 1. Create & Save User
        User user = User.builder()
                .name("Test User")
                .email("testuser@college.edu")
                .password("securehash123")
                .department(dept)
                .role(role)
                .active(true)
                .isDeleted(false)
                .build();
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();

        // 2. Finder Methods
        Optional<User> foundByEmail = userRepository.findByEmail("testuser@college.edu");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getName()).isEqualTo("Test User");

        boolean existsByEmail = userRepository.existsByEmail("testuser@college.edu");
        assertThat(existsByEmail).isTrue();

        List<User> usersByDept = userRepository.findByDepartmentId(dept.getId());
        assertThat(usersByDept).contains(savedUser);

        List<User> activeUsers = userRepository.findByActiveTrueAndIsDeletedFalse();
        assertThat(activeUsers).contains(savedUser);

        // 3. Update User
        savedUser.setName("Updated Test User");
        User updatedUser = userRepository.save(savedUser);
        assertThat(updatedUser.getName()).isEqualTo("Updated Test User");

        // 4. Delete User
        userRepository.delete(updatedUser);
        Optional<User> deletedUser = userRepository.findByEmail("testuser@college.edu");
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void testCourseAndCourseAssignmentCrud() {
        // 1. Setup Department & Course
        Department dept = departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder().name("Computer Science").code("CSE").build()
                ));

        Course course = Course.builder()
                .name("Software Engineering")
                .code("CSE-301")
                .department(dept)
                .active(true)
                .build();
        Course savedCourse = courseRepository.save(course);
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getName()).isEqualTo("Software Engineering");

        // Test Finders for Course
        Optional<Course> foundCourse = courseRepository.findByCode("CSE-301");
        assertThat(foundCourse).isPresent();
        assertThat(courseRepository.existsByCode("CSE-301")).isTrue();
        assertThat(courseRepository.findByDepartmentId(dept.getId())).contains(savedCourse);

        // 2. Setup User & Faculty Profile for assignment
        Role role = roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_FACULTY").build()
                ));

        User user = User.builder()
                .name("Dr. Smith")
                .email("smith@college.edu")
                .password("password123")
                .department(dept)
                .role(role)
                .active(true)
                .build();

        FacultyProfile facultyProfile = FacultyProfile.builder()
                .user(user)
                .employeeId("EMP101")
                .designation("Associate Professor")
                .joiningDate(LocalDate.of(2020, 8, 15))
                .build();
        user.setFacultyProfile(facultyProfile);

        User savedUser = userRepository.save(user);
        FacultyProfile savedFaculty = savedUser.getFacultyProfile();
        assertThat(savedFaculty.getId()).isNotNull();

        // 3. Create CourseAssignment
        CourseAssignment assignment = CourseAssignment.builder()
                .course(savedCourse)
                .faculty(savedUser)
                .academicYear("2025-2026")
                .semester(5)
                .section("A")
                .status("ACTIVE")
                .build();
        CourseAssignment savedAssignment = courseAssignmentRepository.save(assignment);
        assertThat(savedAssignment.getId()).isNotNull();

        // Test Finders for CourseAssignment
        assertThat(courseAssignmentRepository.findByCourseId(savedCourse.getId())).contains(savedAssignment);
        assertThat(courseAssignmentRepository.findByFacultyId(savedUser.getId())).contains(savedAssignment);
        assertThat(courseAssignmentRepository.findByAcademicYearAndSemesterAndSection("2025-2026", 5, "A")).contains(savedAssignment);

        // Update Assignment
        savedAssignment.setStatus("INACTIVE");
        CourseAssignment updatedAssignment = courseAssignmentRepository.save(savedAssignment);
        assertThat(updatedAssignment.getStatus()).isEqualTo("INACTIVE");

        // Delete Assignment
        courseAssignmentRepository.delete(updatedAssignment);
        assertThat(courseAssignmentRepository.findById(savedAssignment.getId())).isEmpty();

        // Delete Course
        courseRepository.delete(savedCourse);
        assertThat(courseRepository.findByCode("CSE-301")).isEmpty();
    }

    @Test
    void testNotificationCrudAndFinderMethods() {
        Role role = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        User user = userRepository.save(User.builder()
                .name("Notification Test User")
                .email("notifuser@college.edu")
                .password("password123")
                .role(role)
                .active(true)
                .build());

        Notification notif1 = notificationRepository.save(Notification.builder()
                .user(user)
                .title("Alert 1")
                .message("Test message 1")
                .notificationType(NotificationType.SYSTEM_ALERT)
                .priority(NotificationPriority.HIGH)
                .isRead(false)
                .build());

        Notification notif2 = notificationRepository.save(Notification.builder()
                .user(user)
                .title("Alert 2")
                .message("Test message 2")
                .notificationType(NotificationType.SURVEY_ASSIGNED)
                .priority(NotificationPriority.MEDIUM)
                .isRead(true)
                .build());

        // Test findByUserIdOrderByCreatedAtDesc
        List<Notification> allNotifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(allNotifs).hasSize(2);

        // Test findByUserIdAndIsReadFalse
        List<Notification> unreadNotifs = notificationRepository.findByUserIdAndIsReadFalse(user.getId());
        assertThat(unreadNotifs).hasSize(1);
        assertThat(unreadNotifs.get(0).getTitle()).isEqualTo("Alert 1");

        // Test countByUserIdAndIsReadFalse
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        assertThat(unreadCount).isEqualTo(1L);
    }

    @Test
    void testAuditLogCrudAndFinderMethods() {
        Role role = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        User user = userRepository.save(User.builder()
                .name("Audit Admin")
                .email("auditadmin@college.edu")
                .password("password123")
                .role(role)
                .active(true)
                .build());

        LocalDateTime start = LocalDateTime.now().minusHours(1);

        AuditLog log1 = auditLogRepository.save(AuditLog.builder()
                .action("CREATE_USER")
                .entityName("User")
                .entityId(100L)
                .performedBy(user)
                .details("Created user 100")
                .build());

        AuditLog log2 = auditLogRepository.save(AuditLog.builder()
                .action("UPDATE_USER")
                .entityName("User")
                .entityId(100L)
                .performedBy(user)
                .details("Updated user 100")
                .build());

        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // Test findByPerformedBy
        List<AuditLog> userLogs = auditLogRepository.findByPerformedBy(user.getId());
        assertThat(userLogs).hasSize(2);

        // Test findByEntityNameAndEntityId
        List<AuditLog> entityLogs = auditLogRepository.findByEntityNameAndEntityId("User", 100L);
        assertThat(entityLogs).hasSize(2);

        // Test findByPerformedAtBetween
        List<AuditLog> rangeLogs = auditLogRepository.findByPerformedAtBetween(start, end);
        assertThat(rangeLogs).contains(log1, log2);
    }
}
