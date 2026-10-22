package edu.jecrc.portal.modules.placement.entity;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eligibility_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "evaluation_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_profile_id", nullable = false)
    private JobProfile jobProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PlacementPolicy policy;

    @Column(name = "policy_version", nullable = false)
    private Integer policyVersion;

    @Column(name = "outcome", nullable = false, length = 30)
    private String outcome; // ELIGIBLE, INELIGIBLE, NEEDS_VERIFICATION

    @Column(name = "criteria_snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String criteriaSnapshotJson;

    @Column(name = "unmet_criteria_json", columnDefinition = "TEXT")
    private String unmetCriteriaJson;

    @Column(name = "missing_verification_json", columnDefinition = "TEXT")
    private String missingVerificationJson;

    @Builder.Default
    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by_user_id")
    private User evaluatedByUser;
}
