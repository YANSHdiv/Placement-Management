package edu.jecrc.portal.modules.interview.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "round_evaluations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_candidate_evaluator", columnNames = {"round_candidate_id", "evaluator_user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "evaluation_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_candidate_id", nullable = false)
    private RoundCandidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_user_id", nullable = false)
    private User evaluatorUser;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Builder.Default
    @Column(name = "evaluation_state", nullable = false, length = 20)
    private String evaluationState = "DRAFT"; // DRAFT, SUBMITTED

    @Column(name = "recommendation", length = 30)
    private String recommendation; // STRONG_HIRE, HIRE, BORDERLINE, REJECT

    @Column(name = "qualitative_feedback", columnDefinition = "TEXT")
    private String qualitativeFeedback;

    @Column(name = "confidential_notes", columnDefinition = "TEXT")
    private String confidentialNotes;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
