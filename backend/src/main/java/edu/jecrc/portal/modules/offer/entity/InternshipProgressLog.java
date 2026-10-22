package edu.jecrc.portal.modules.offer.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "internship_progress_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipProgressLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private InternshipRecord internship;

    @Column(name = "log_number", nullable = false)
    private Integer logNumber;

    @Column(name = "reporting_period_start", nullable = false)
    private LocalDate reportingPeriodStart;

    @Column(name = "reporting_period_end", nullable = false)
    private LocalDate reportingPeriodEnd;

    @Column(name = "summary_of_work", nullable = false, columnDefinition = "TEXT")
    private String summaryOfWork;

    @Column(name = "learning_outcomes", columnDefinition = "TEXT")
    private String learningOutcomes;

    @Column(name = "challenges_encountered", columnDefinition = "TEXT")
    private String challengesEncountered;

    @Column(name = "evidence_attachment_ref", length = 300)
    private String evidenceAttachmentRef;

    @Column(name = "submission_status", nullable = false, length = 50)
    @Builder.Default
    private String submissionStatus = "SUBMITTED"; // SUBMITTED, REVIEWED, CORRECTION_REQUESTED

    @Column(name = "mentor_feedback", columnDefinition = "TEXT")
    private String mentorFeedback;

    @Column(name = "mentor_reviewed_at")
    private Instant mentorReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_reviewer_id")
    private User mentorReviewer;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
