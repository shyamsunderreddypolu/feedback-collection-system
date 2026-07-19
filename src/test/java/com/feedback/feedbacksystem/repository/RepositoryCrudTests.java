package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.Department;
import com.feedback.feedbacksystem.model.Role;
import com.feedback.feedbacksystem.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // Automatically rolls back database modifications after each test execution
class RepositoryCrudTests {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

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
}
