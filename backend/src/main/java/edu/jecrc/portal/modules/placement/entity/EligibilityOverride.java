package edu.jecrc.portal.modules.placement.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eligibility_overrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "override_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private EligibilityEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id", nullable = false)
    private User approvedByUser;

    @Column(name = "override_reason", nullable = false, columnDefinition = "TEXT")
    private String overrideReason;

    @Builder.Default
    @Column(name = "override_outcome", nullable = false, length = 30)
    private String overrideOutcome = "ELIGIBLE"; // ELIGIBLE, INELIGIBLE, NEEDS_VERIFICATION

    @Column(name = "original_outcome", nullable = false, length = 30)
    private String originalOutcome;

    @Builder.Default
    @Column(name = "overridden_at", nullable = false)
    private Instant overriddenAt = Instant.now();
}
