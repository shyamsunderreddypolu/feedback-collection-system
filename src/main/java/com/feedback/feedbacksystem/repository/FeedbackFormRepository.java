package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.FeedbackForm;
import com.feedback.feedbacksystem.model.FormStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {

    List<FeedbackForm> findByStatus(FormStatus status);

    List<FeedbackForm> findByCreatorId(Long creatorId);

    List<FeedbackForm> findByIsDeletedFalse();

    List<FeedbackForm> findByStatusAndStartDateBeforeAndEndDateAfter(
            FormStatus status, LocalDateTime start, LocalDateTime end);
}
