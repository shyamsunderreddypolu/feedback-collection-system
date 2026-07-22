package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByFeedbackFormIdOrderByDisplayOrderAsc(Long feedbackFormId);

    long countByFeedbackFormId(Long feedbackFormId);
}
