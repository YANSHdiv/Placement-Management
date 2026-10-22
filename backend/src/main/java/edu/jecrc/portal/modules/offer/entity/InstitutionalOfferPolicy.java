package edu.jecrc.portal.modules.offer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "institutional_offer_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionalOfferPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_code", nullable = false, unique = true, length = 50)
    private String policyCode;

    @Column(name = "policy_name", nullable = false, length = 150)
    private String policyName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "allow_multiple_offers", nullable = false)
    @Builder.Default
    private Boolean allowMultipleOffers = false;

    @Column(name = "allow_multiple_accepted", nullable = false)
    @Builder.Default
    private Boolean allowMultipleAccepted = false;

    @Column(name = "max_active_offers_per_student", nullable = false)
    @Builder.Default
    private Integer maxActiveOffersPerStudent = 1;

    @Column(name = "dream_tier_multiplier_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal dreamTierMultiplierThreshold = new BigDecimal("1.50");

    @Column(name = "super_dream_ctc_threshold_lpa", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal superDreamCtcThresholdLpa = new BigDecimal("18.00");

    @Column(name = "auto_withdraw_other_applications", nullable = false)
    @Builder.Default
    private Boolean autoWithdrawOtherApplications = true;

    @Column(name = "version_number", nullable = false)
    @Builder.Default
    private Integer versionNumber = 1;

    @Column(name = "is_synthetic_default", nullable = false)
    @Builder.Default
    private Boolean isSyntheticDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
