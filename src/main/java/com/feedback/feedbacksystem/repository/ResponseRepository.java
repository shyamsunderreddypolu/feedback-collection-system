package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {

    List<Response> findBySubmitterId(Long submitterId);

    List<Response> findByFeedbackFormId(Long feedbackFormId);

    boolean existsByFeedbackFormIdAndSubmitterId(Long feedbackFormId, Long submitterId);

    long countByFeedbackFormId(Long feedbackFormId);
}
