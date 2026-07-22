package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.FeedbackAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackAssignmentRepository extends JpaRepository<FeedbackAssignment, Long> {

    List<FeedbackAssignment> findByFeedbackFormId(Long feedbackFormId);

    List<FeedbackAssignment> findByDepartmentIdAndSemesterAndSectionAndBatch(
            Long deptId, int sem, String sec, String batch);
}
