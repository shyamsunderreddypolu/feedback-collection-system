package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByRollNumber(String rollNumber);

    boolean existsByRollNumber(String rollNumber);

    List<StudentProfile> findByYearAndSemesterAndSection(int year, int semester, String section);

    List<StudentProfile> findByBatch(String batch);

    Optional<StudentProfile> findByUserId(Long userId);
}
