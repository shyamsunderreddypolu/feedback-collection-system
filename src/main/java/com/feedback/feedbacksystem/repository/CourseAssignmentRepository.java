package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.AssignmentStatus;
import com.feedback.feedbacksystem.model.CourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {

    List<CourseAssignment> findByCourseId(Long courseId);

    List<CourseAssignment> findByFacultyId(Long facultyId);

    List<CourseAssignment> findByAcademicYearAndSemesterAndSection(String academicYear, int semester, String section);

    List<CourseAssignment> findByStatus(AssignmentStatus status);
}
