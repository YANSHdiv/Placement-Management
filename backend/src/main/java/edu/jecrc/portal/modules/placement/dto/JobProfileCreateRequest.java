package edu.jecrc.portal.modules.placement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobProfileCreateRequest {

    @NotNull(message = "Policy ID is required")
    private UUID policyId;

    @NotBlank(message = "Profile title is required")
    @Size(max = 255)
    private String profileTitle;

    private String jobDescription;

    @NotBlank(message = "Employment type is required")
    private String employmentType; // FULL_TIME, INTERNSHIP, INTERNSHIP_PLUS_FULL_TIME

    private String location;

    @Builder.Default
    private String workMode = "ON_SITE"; // ON_SITE, HYBRID, REMOTE

    private String requiredSkills;

    @NotNull(message = "Total CTC (LPA) is required")
    @Positive(message = "Total CTC must be positive")
    private BigDecimal totalCtcLpa;

    private BigDecimal fixedPayLpa;
    private BigDecimal variablePayLpa;
    private BigDecimal retentionBonusLpa;
    private BigDecimal monthlyStipendInr;

    @Builder.Default
    private Boolean ppoEligible = false;

    private String selectionProcess;
}
