package edu.jecrc.portal.modules.interview.entity;

import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_profile_id")
    private JobProfile jobProfile;

    @Column(name = "round_type", length = 50)
    private String roundType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 50)
    private String questionType; // MULTIPLE_CHOICE, CODING_QUESTION, TECHNICAL_DISCUSSION, BEHAVIORAL_QUESTION, HR_QUESTION, OPEN_ENDED

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Builder.Default
    @Column(name = "difficulty", nullable = false, length = 20)
    private String difficulty = "MEDIUM"; // EASY, MEDIUM, HARD

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "expected_answer_guidance", columnDefinition = "TEXT")
    private String expectedAnswerGuidance;

    @Builder.Default
    @Column(name = "is_confidential", nullable = false)
    private Boolean isConfidential = true;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

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
