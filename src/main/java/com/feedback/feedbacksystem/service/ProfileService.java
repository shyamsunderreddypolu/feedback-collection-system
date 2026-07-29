package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.FacultyProfileDto;
import com.feedback.feedbacksystem.dto.StudentProfileDto;

public interface ProfileService {
    StudentProfileDto getStudentProfileByUserId(Long userId);
    FacultyProfileDto getFacultyProfileByUserId(Long userId);
    StudentProfileDto updateStudentProfile(Long userId, StudentProfileDto dto);
    FacultyProfileDto updateFacultyProfile(Long userId, FacultyProfileDto dto);
}
