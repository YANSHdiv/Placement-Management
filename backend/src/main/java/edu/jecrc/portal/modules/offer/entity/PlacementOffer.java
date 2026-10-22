package edu.jecrc.portal.modules.offer.entity;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.placement.entity.PlacementDrive;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "placement_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "offer_reference_number", nullable = false, unique = true, length = 100)
    private String offerReferenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_profile_id")
    private JobProfile jobProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drive_id")
    private PlacementDrive drive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_policy_id")
    private InstitutionalOfferPolicy appliedPolicy;

    @Column(name = "effective_policy_version")
    @Builder.Default
    private Integer effectivePolicyVersion = 1;

    @Column(name = "designation", nullable = false, length = 150)
    private String designation;

    @Column(name = "employment_type", nullable = false, length = 50)
    @Builder.Default
    private String employmentType = "FULL_TIME";

    @Column(name = "total_ctc_lpa", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCtcLpa;

    @Column(name = "fixed_salary_lpa", precision = 10, scale = 2)
    private BigDecimal fixedSalaryLpa;

    @Column(name = "variable_salary_lpa", precision = 10, scale = 2)
    private BigDecimal variableSalaryLpa;

    @Column(name = "joining_bonus", precision = 10, scale = 2)
    private BigDecimal joiningBonus;

    @Column(name = "retention_bonus", precision = 10, scale = 2)
    private BigDecimal retentionBonus;

    @Column(name = "monthly_stipend_inr", precision = 10, scale = 2)
    private BigDecimal monthlyStipendInr;

    @Column(name = "job_location", length = 200)
    private String jobLocation;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "offer_letter_doc_ref", length = 300)
    private String offerLetterDocRef;

    @Column(name = "offer_status", nullable = false, length = 50)
    @Builder.Default
    private String offerStatus = "DRAFT"; // DRAFT, ISSUED, ACCEPTED, DECLINED, EXPIRED, REVOKED

    @Column(name = "issuance_date")
    private Instant issuanceDate;

    @Column(name = "decision_deadline")
    private Instant decisionDeadline;

    @Column(name = "student_decision", length = 50)
    private String studentDecision; // ACCEPTED, DECLINED

    @Column(name = "student_decision_at")
    private Instant studentDecisionAt;

    @Column(name = "student_decision_remarks", columnDefinition = "TEXT")
    private String studentDecisionRemarks;

    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String revocationReason;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_user_id")
    private User revokedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_user_id")
    private User issuedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
