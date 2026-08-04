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

    List<Answer> findByQuestionIdAndRatingValueIsNotNull(Long questionId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(a.ratingValue) FROM Answer a WHERE a.question.id = :questionId AND a.ratingValue IS NOT NULL")
    Double findAverageRatingByQuestionId(@org.springframework.data.repository.query.Param("questionId") Long questionId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(a.ratingValue) FROM Answer a WHERE a.question.feedbackForm.id = :formId AND a.ratingValue IS NOT NULL")
    Double findAverageRatingByFeedbackFormId(@org.springframework.data.repository.query.Param("formId") Long formId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(a.ratingValue) FROM Answer a WHERE a.question.feedbackForm.id IN :formIds AND a.ratingValue IS NOT NULL")
    Double findAverageRatingByFeedbackFormIds(@org.springframework.data.repository.query.Param("formIds") List<Long> formIds);
}
