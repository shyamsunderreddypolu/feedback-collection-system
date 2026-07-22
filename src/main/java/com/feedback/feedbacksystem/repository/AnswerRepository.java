package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByResponseId(Long responseId);

    List<Answer> findByQuestionId(Long questionId);

    long countByQuestionIdAndSelectedOptionId(Long questionId, Long selectedOptionId);
}
