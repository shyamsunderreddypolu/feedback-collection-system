package com.feedback.feedbacksystem.config;

import com.feedback.feedbacksystem.model.Role;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.RoleRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Ensure Roles Exist
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

        Role facultyRole = roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_FACULTY").build()));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

        // 2. Seed Admin User (admin@fbcs.local / admin123)
        if (!userRepository.existsByEmail("admin@fbcs.local")) {
            User admin = User.builder()
                    .name("System Administrator")
                    .email("admin@fbcs.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role(adminRole)
                    .active(true)
                    .isDeleted(false)
                    .build();
            userRepository.save(admin);
        } else {
            // Ensure valid BCrypt hash for seeded admin
            userRepository.findByEmail("admin@fbcs.local").ifPresent(admin -> {
                admin.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
            });
        }

        // 3. Seed Faculty User (faculty@fbcs.local / faculty123)
        if (!userRepository.existsByEmail("faculty@fbcs.local")) {
            User faculty = User.builder()
                    .name("Dr. Alan Turing")
                    .email("faculty@fbcs.local")
                    .password(passwordEncoder.encode("faculty123"))
                    .role(facultyRole)
                    .active(true)
                    .isDeleted(false)
                    .build();
            userRepository.save(faculty);
        }

        // 4. Seed Student User (student@fbcs.local / student123)
        if (!userRepository.existsByEmail("student@fbcs.local")) {
            User student = User.builder()
                    .name("Alice Johnson")
                    .email("student@fbcs.local")
                    .password(passwordEncoder.encode("student123"))
                    .role(studentRole)
                    .active(true)
                    .isDeleted(false)
                    .build();
            userRepository.save(student);
        }
    }
}
