package edu.jecrc.portal.modules.interview.entity;

import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "feedback_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "feedback_type", nullable = false, length = 50)
    private String feedbackType; // STUDENT_TO_DRIVE, EVALUATOR_TO_CANDIDATE, TPO_TO_COMPANY

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private InterviewRound round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1 to 5

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Builder.Default
    @Column(name = "is_confidential_to_staff", nullable = false)
    private Boolean isConfidentialToStaff = false;

    @Builder.Default
    @Column(name = "moderation_status", nullable = false, length = 30)
    private String moderationStatus = "APPROVED"; // PENDING, APPROVED, REJECTED

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
