package edu.jecrc.portal.modules.placement.entity;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_applications", uniqueConstraints = {
    @UniqueConstraint(name = "uq_job_app_student_profile", columnNames = {"job_profile_id", "student_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "application_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_profile_id", nullable = false)
    private JobProfile jobProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private EligibilityEvaluation evaluation;

    @Builder.Default
    @Column(name = "application_status", nullable = false, length = 30)
    private String applicationStatus = "APPLIED"; // APPLIED, IN_PROCESS, WITHDRAWN, REJECTED, OFFERED, PLACED

    @Column(name = "resume_version_tag", nullable = false, length = 100)
    private String resumeVersionTag;

    @Column(name = "student_remarks", columnDefinition = "TEXT")
    private String studentRemarks;

    @Builder.Default
    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt = Instant.now();

    @Builder.Default
    @Column(name = "status_updated_at", nullable = false)
    private Instant statusUpdatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_updated_by")
    private User statusUpdatedBy;

    @Column(name = "status_change_reason", columnDefinition = "TEXT")
    private String statusChangeReason;
}
