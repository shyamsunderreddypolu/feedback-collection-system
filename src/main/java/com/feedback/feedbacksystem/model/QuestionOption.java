package com.feedback.feedbacksystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "question_options", uniqueConstraints = {
    @UniqueConstraint(name = "uk_question_option_order", columnNames = {"question_id", "display_order"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @NotBlank
    @Size(max = 255)
    @Column(name = "option_value", nullable = false, length = 255)
    private String optionValue;

    @Min(1)
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
