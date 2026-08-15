package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.Question;
import com.feedback.feedbacksystem.model.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByFeedbackFormIdOrderByDisplayOrderAsc(Long feedbackFormId);

    List<Question> findByFeedbackFormId(Long feedbackFormId);

    List<Question> findByFeedbackFormIdAndQuestionType(Long feedbackFormId, QuestionType questionType);

    int countByFeedbackFormId(Long feedbackFormId);
}
