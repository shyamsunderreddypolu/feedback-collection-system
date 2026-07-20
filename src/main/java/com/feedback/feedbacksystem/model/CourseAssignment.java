package com.feedback.feedbacksystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_assignments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_course_assignment_details", columnNames = {
        "course_id", "faculty_id", "academic_year", "semester", "section"
    })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private User faculty;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "academic year must follow the YYYY-YYYY format, e.g. 2025-2026")
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Min(1)
    @Max(8)
    @Column(nullable = false)
    private int semester;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String section;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
