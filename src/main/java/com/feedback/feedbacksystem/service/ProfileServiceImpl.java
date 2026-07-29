package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.FacultyProfileDto;
import com.feedback.feedbacksystem.dto.StudentProfileDto;
import com.feedback.feedbacksystem.exception.ResourceNotFoundException;
import com.feedback.feedbacksystem.model.FacultyProfile;
import com.feedback.feedbacksystem.model.StudentProfile;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.FacultyProfileRepository;
import com.feedback.feedbacksystem.repository.StudentProfileRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentProfileDto getStudentProfileByUserId(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", userId));
        return toStudentProfileDto(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyProfileDto getFacultyProfileByUserId(Long userId) {
        FacultyProfile profile = facultyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("FacultyProfile", userId));
        return toFacultyProfileDto(profile);
    }

    @Override
    public StudentProfileDto updateStudentProfile(Long userId, StudentProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> StudentProfile.builder().user(user).build());

        if (dto.getRollNumber() != null) profile.setRollNumber(dto.getRollNumber().trim());
        if (dto.getAcademicYear() != null) profile.setAcademicYear(dto.getAcademicYear().trim());
        if (dto.getSemester() > 0) profile.setSemester(dto.getSemester());
        if (dto.getSection() != null) profile.setSection(dto.getSection().trim());
        if (dto.getBatch() != null) profile.setBatch(dto.getBatch().trim());

        StudentProfile saved = studentProfileRepository.save(profile);
        return toStudentProfileDto(saved);
    }

    @Override
    public FacultyProfileDto updateFacultyProfile(Long userId, FacultyProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        FacultyProfile profile = facultyProfileRepository.findByUserId(userId)
                .orElseGet(() -> FacultyProfile.builder().user(user).build());

        if (dto.getEmployeeId() != null) profile.setEmployeeId(dto.getEmployeeId().trim());
        if (dto.getDesignation() != null) profile.setDesignation(dto.getDesignation().trim());
        if (dto.getJoiningDate() != null) profile.setJoiningDate(dto.getJoiningDate());

        FacultyProfile saved = facultyProfileRepository.save(profile);
        return toFacultyProfileDto(saved);
    }

    private StudentProfileDto toStudentProfileDto(StudentProfile profile) {
        return StudentProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .rollNumber(profile.getRollNumber())
                .academicYear(profile.getAcademicYear())
                .semester(profile.getSemester())
                .section(profile.getSection())
                .batch(profile.getBatch())
                .build();
    }

    private FacultyProfileDto toFacultyProfileDto(FacultyProfile profile) {
        return FacultyProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .employeeId(profile.getEmployeeId())
                .designation(profile.getDesignation())
                .joiningDate(profile.getJoiningDate())
                .build();
    }
}
