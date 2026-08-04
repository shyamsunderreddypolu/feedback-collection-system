package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionIdOrderByDisplayOrderAsc(Long questionId);

    List<QuestionOption> findByQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(Long questionId);
}
