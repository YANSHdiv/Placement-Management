package edu.jecrc.portal.modules.interview.entity;

import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interview_rounds", uniqueConstraints = {
        @UniqueConstraint(name = "uq_round_profile_sequence", columnNames = {"job_profile_id", "sequence_order"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRound {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "round_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_profile_id", nullable = false)
    private JobProfile jobProfile;

    @Column(name = "round_name", nullable = false, length = 150)
    private String roundName;

    @Column(name = "round_type", nullable = false, length = 50)
    private String roundType; // APTITUDE, ONLINE_ASSESSMENT, CODING_ASSESSMENT, TECHNICAL_INTERVIEW, MANAGERIAL_INTERVIEW, HR_INTERVIEW, GROUP_DISCUSSION, CUSTOM

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "venue_or_meeting_url", length = 500)
    private String venueOrMeetingUrl;

    @Builder.Default
    @Column(name = "candidate_scheduling_mode", nullable = false, length = 50)
    private String candidateSchedulingMode = "SCHEDULED_SLOTS"; // SCHEDULED_SLOTS, WALK_IN, FLEXIBLE

    @Column(name = "evaluation_criteria", columnDefinition = "TEXT")
    private String evaluationCriteria;

    @Column(name = "max_score", precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "scoring_scale_description", length = 255)
    private String scoringScaleDescription;

    @Builder.Default
    @Column(name = "publication_status", nullable = false, length = 30)
    private String publicationStatus = "DRAFT"; // DRAFT, CONFIGURED, SCHEDULED, IN_PROGRESS, COMPLETED

    @Builder.Default
    @Column(name = "is_results_published", nullable = false)
    private Boolean isResultsPublished = false;

    @Column(name = "results_published_at")
    private Instant resultsPublishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "results_published_by")
    private User resultsPublishedBy;

    @Builder.Default
    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundEvaluator> evaluators = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundCandidate> candidates = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
