package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    List<Course> findByDepartmentId(Long departmentId);

    List<Course> findByActiveTrue();

    boolean existsByCode(String code);
}
