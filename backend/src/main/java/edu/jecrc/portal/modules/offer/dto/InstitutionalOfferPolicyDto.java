package edu.jecrc.portal.modules.offer.dto;

import edu.jecrc.portal.modules.offer.entity.InstitutionalOfferPolicy;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionalOfferPolicyDto {
    private UUID id;
    private String policyCode;
    private String policyName;
    private String description;
    private Boolean allowMultipleOffers;
    private Boolean allowMultipleAccepted;
    private Integer maxActiveOffersPerStudent;
    private BigDecimal dreamTierMultiplierThreshold;
    private BigDecimal superDreamCtcThresholdLpa;
    private Boolean autoWithdrawOtherApplications;
    private Integer versionNumber;
    private Boolean isSyntheticDefault;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static InstitutionalOfferPolicyDto fromEntity(InstitutionalOfferPolicy p) {
        return InstitutionalOfferPolicyDto.builder()
                .id(p.getId())
                .policyCode(p.getPolicyCode())
                .policyName(p.getPolicyName())
                .description(p.getDescription())
                .allowMultipleOffers(p.getAllowMultipleOffers())
                .allowMultipleAccepted(p.getAllowMultipleAccepted())
                .maxActiveOffersPerStudent(p.getMaxActiveOffersPerStudent())
                .dreamTierMultiplierThreshold(p.getDreamTierMultiplierThreshold())
                .superDreamCtcThresholdLpa(p.getSuperDreamCtcThresholdLpa())
                .autoWithdrawOtherApplications(p.getAutoWithdrawOtherApplications())
                .versionNumber(p.getVersionNumber())
                .isSyntheticDefault(p.getIsSyntheticDefault())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
