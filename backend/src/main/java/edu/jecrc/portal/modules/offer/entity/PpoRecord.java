package edu.jecrc.portal.modules.offer.entity;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ppo_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PpoRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private InternshipRecord internship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_job_profile_id")
    private JobProfile targetJobProfile;

    @Column(name = "ppo_status", nullable = false, length = 50)
    @Builder.Default
    private String ppoStatus = "UNDER_REVIEW"; // NOT_ELIGIBLE, UNDER_REVIEW, RECOMMENDED, APPROVED, DECLINED, OFFER_ISSUED

    @Column(name = "minimum_evaluation_score_required", precision = 5, scale = 2)
    private BigDecimal minimumEvaluationScoreRequired;

    @Column(name = "recommendation_notes", columnDefinition = "TEXT")
    private String recommendationNotes;

    @Column(name = "decision_remarks", columnDefinition = "TEXT")
    private String decisionRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_by_user_id")
    private User recommendedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_user_id")
    private User decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resulting_offer_id")
    private PlacementOffer resultingOffer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
