package com.feedback.feedbacksystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_assignments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_feedback_assignment", columnNames = {
        "feedback_form_id", "department_id", "course_id", "semester", "section", "batch", "academic_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_form_id", nullable = false)
    private FeedbackForm feedbackForm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Plain FK column until the Course entity is delivered (issue #4);
    // NULL targets a general department-wide survey instead of a course.
    @Column(name = "course_id")
    private Long courseId;

    @Min(1)
    @Max(8)
    @Column(nullable = false)
    private int semester;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String section;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "batch must follow the YYYY-YYYY format, e.g. 2023-2027")
    @Column(nullable = false, length = 30)
    private String batch;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "academic year must follow the YYYY-YYYY format, e.g. 2025-2026")
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
