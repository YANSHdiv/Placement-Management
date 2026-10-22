package edu.jecrc.portal.modules.placement.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_profile_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id", nullable = false)
    private PlacementDrive drive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PlacementPolicy policy;

    @Column(name = "profile_title", nullable = false)
    private String profileTitle;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "employment_type", nullable = false, length = 50)
    private String employmentType; // FULL_TIME, INTERNSHIP, INTERNSHIP_PLUS_FULL_TIME

    @Column(name = "location", length = 150)
    private String location;

    @Builder.Default
    @Column(name = "work_mode", nullable = false, length = 50)
    private String workMode = "ON_SITE"; // ON_SITE, HYBRID, REMOTE

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "total_ctc_lpa", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalCtcLpa;

    @Column(name = "fixed_pay_lpa", precision = 8, scale = 2)
    private BigDecimal fixedPayLpa;

    @Column(name = "variable_pay_lpa", precision = 8, scale = 2)
    private BigDecimal variablePayLpa;

    @Column(name = "retention_bonus_lpa", precision = 8, scale = 2)
    private BigDecimal retentionBonusLpa;

    @Column(name = "monthly_stipend_inr", precision = 10, scale = 2)
    private BigDecimal monthlyStipendInr;

    @Builder.Default
    @Column(name = "ppo_eligible", nullable = false)
    private Boolean ppoEligible = false;

    @Column(name = "selection_process", columnDefinition = "TEXT")
    private String selectionProcess;

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
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
