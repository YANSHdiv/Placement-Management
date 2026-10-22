package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionalOfferPolicyUpdateRequest {

    private String policyName;
    private String description;

    @NotNull
    private Boolean allowMultipleOffers;

    @NotNull
    private Boolean allowMultipleAccepted;

    @NotNull
    private Integer maxActiveOffersPerStudent;

    private BigDecimal dreamTierMultiplierThreshold;
    private BigDecimal superDreamCtcThresholdLpa;

    @NotNull
    private Boolean autoWithdrawOtherApplications;

    private Boolean isActive;
}
