package edu.jecrc.portal.modules.offer.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "internship_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private InternshipRecord internship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_user_id")
    private User evaluatorUser;

    @Column(name = "evaluation_type", nullable = false, length = 50)
    @Builder.Default
    private String evaluationType = "MID_TERM"; // MID_TERM, FINAL, AD_HOC

    @Column(name = "technical_score", precision = 5, scale = 2)
    private BigDecimal technicalScore;

    @Column(name = "communication_score", precision = 5, scale = 2)
    private BigDecimal communicationScore;

    @Column(name = "professionalism_score", precision = 5, scale = 2)
    private BigDecimal professionalismScore;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "written_feedback", columnDefinition = "TEXT")
    private String writtenFeedback;

    @Column(name = "confidential_staff_notes", columnDefinition = "TEXT")
    private String confidentialStaffNotes; // STRICTLY HIDDEN FROM STUDENT

    @Column(name = "recommend_for_ppo", nullable = false)
    @Builder.Default
    private Boolean recommendForPpo = false;

    @CreationTimestamp
    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private Instant evaluatedAt;
}
