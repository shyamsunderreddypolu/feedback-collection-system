package com.feedback.feedbacksystem.security;

import com.feedback.feedbacksystem.model.Department;
import com.feedback.feedbacksystem.model.Role;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.DepartmentRepository;
import com.feedback.feedbacksystem.repository.RoleRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import com.feedback.feedbacksystem.security.service.CustomUserDetailsService;
import com.feedback.feedbacksystem.security.service.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SecurityStage3Tests {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByCode("CSE")
                .orElseGet(() -> departmentRepository.save(
                        Department.builder().name("Computer Science").code("CSE").build()
                ));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        testUser = userRepository.save(User.builder()
                .name("Stage 3 Tester")
                .email("stage3.tester@college.edu")
                .password("password123")
                .department(dept)
                .role(studentRole)
                .active(true)
                .build());
    }

    @Test
    void testCustomUserDetailsServiceLoadUserByUsername() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("stage3.tester@college.edu");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("stage3.tester@college.edu");
        assertThat(userDetails.getAuthorities()).extracting("authority").contains("ROLE_STUDENT");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void testUserPrincipalCreation() {
        UserPrincipal principal = UserPrincipal.create(testUser);

        assertThat(principal.getId()).isEqualTo(testUser.getId());
        assertThat(principal.getName()).isEqualTo("Stage 3 Tester");
        assertThat(principal.getEmail()).isEqualTo("stage3.tester@college.edu");
        assertThat(principal.getAuthorities()).extracting("authority").contains("ROLE_STUDENT");
    }

    @Test
    void testLoadNonExistentUserThrowsException() {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@college.edu"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
