package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.FacultyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyProfileRepository extends JpaRepository<FacultyProfile, Long> {

    Optional<FacultyProfile> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    List<FacultyProfile> findByDesignation(String designation);

    Optional<FacultyProfile> findByUserId(Long userId);
}
