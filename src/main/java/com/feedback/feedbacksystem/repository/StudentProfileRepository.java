package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    Optional<StudentProfile> findByRollNumber(String rollNumber);

    long countByUserDepartmentIdAndSemesterAndBatch(Long departmentId, int semester, String batch);

    long countByUserDepartmentIdAndSemesterAndSectionAndBatch(Long departmentId, int semester, String section, String batch);

    List<StudentProfile> findByUserDepartmentIdAndSemesterAndBatch(Long departmentId, int semester, String batch);
}
