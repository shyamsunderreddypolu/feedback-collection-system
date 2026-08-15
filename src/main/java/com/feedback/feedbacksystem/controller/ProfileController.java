package com.feedback.feedbacksystem.controller;

import com.feedback.feedbacksystem.dto.FacultyProfileDto;
import com.feedback.feedbacksystem.dto.StudentProfileDto;
import com.feedback.feedbacksystem.dto.UserResponseDto;
import com.feedback.feedbacksystem.security.service.UserPrincipal;
import com.feedback.feedbacksystem.service.ProfileService;
import com.feedback.feedbacksystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Long userId = principal.getId();
        UserResponseDto userDto = userService.getUserById(userId);

        if ("ROLE_STUDENT".equalsIgnoreCase(userDto.getRoleName())) {
            try {
                return ResponseEntity.ok(profileService.getStudentProfileByUserId(userId));
            } catch (Exception e) {
                return ResponseEntity.ok(userDto);
            }
        } else if ("ROLE_FACULTY".equalsIgnoreCase(userDto.getRoleName())) {
            try {
                return ResponseEntity.ok(profileService.getFacultyProfileByUserId(userId));
            } catch (Exception e) {
                return ResponseEntity.ok(userDto);
            }
        }

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<StudentProfileDto> getStudentProfile(@PathVariable Long userId) {
        StudentProfileDto profile = profileService.getStudentProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/student/{userId}")
    public ResponseEntity<StudentProfileDto> updateStudentProfile(
            @PathVariable Long userId,
            @Valid @RequestBody StudentProfileDto dto) {
        StudentProfileDto updated = profileService.updateStudentProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/faculty/{userId}")
    public ResponseEntity<FacultyProfileDto> getFacultyProfile(@PathVariable Long userId) {
        FacultyProfileDto profile = profileService.getFacultyProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/faculty/{userId}")
    public ResponseEntity<FacultyProfileDto> updateFacultyProfile(
            @PathVariable Long userId,
            @Valid @RequestBody FacultyProfileDto dto) {
        FacultyProfileDto updated = profileService.updateFacultyProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }
}
