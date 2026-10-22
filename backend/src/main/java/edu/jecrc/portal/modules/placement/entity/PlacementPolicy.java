package edu.jecrc.portal.modules.placement.entity;

import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "placement_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_id")
    private UUID id;

    @Column(name = "policy_code", nullable = false, length = 100)
    private String policyCode;

    @Column(name = "policy_name", nullable = false)
    private String policyName;

    @Builder.Default
    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Builder.Default
    @Column(name = "policy_status", nullable = false, length = 30)
    private String policyStatus = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED

    @Column(name = "min_cgpa", precision = 4, scale = 2)
    private BigDecimal minCgpa;

    @Column(name = "min_class_10_percentage", precision = 5, scale = 2)
    private BigDecimal minClass10Percentage;

    @Column(name = "min_class_12_percentage", precision = 5, scale = 2)
    private BigDecimal minClass12Percentage;

    @Column(name = "min_diploma_percentage", precision = 5, scale = 2)
    private BigDecimal minDiplomaPercentage;

    @Builder.Default
    @Column(name = "max_active_backlogs", nullable = false)
    private Integer maxActiveBacklogs = 0;

    @Builder.Default
    @Column(name = "max_historical_backlogs", nullable = false)
    private Integer maxHistoricalBacklogs = 0;

    @Builder.Default
    @Column(name = "allow_academic_gaps", nullable = false)
    private Boolean allowAcademicGaps = true;

    @Builder.Default
    @Column(name = "max_academic_gap_years")
    private Integer maxAcademicGapYears = 0;

    @Builder.Default
    @Column(name = "requires_all_qualifications_verified", nullable = false)
    private Boolean requiresAllQualificationsVerified = true;

    @Builder.Default
    @Column(name = "requires_cgpa_verified", nullable = false)
    private Boolean requiresCgpaVerified = true;

    @Column(name = "criteria_payload_json", columnDefinition = "TEXT")
    private String criteriaPayloadJson;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "placement_policy_programs",
        joinColumns = @JoinColumn(name = "policy_id"),
        inverseJoinColumns = @JoinColumn(name = "program_id")
    )
    @Builder.Default
    private Set<AcademicProgram> eligiblePrograms = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "placement_policy_batches",
        joinColumns = @JoinColumn(name = "policy_id"),
        inverseJoinColumns = @JoinColumn(name = "batch_id")
    )
    @Builder.Default
    private Set<AcademicBatch> eligibleBatches = new HashSet<>();

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

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
