package edu.jecrc.portal.modules.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobProfileDto {
    private UUID id;
    private UUID driveId;
    private String driveTitle;
    private String companyName;
    private String companyCode;
    private UUID policyId;
    private String policyCode;
    private String policyName;
    private Integer policyVersion;
    private String profileTitle;
    private String jobDescription;
    private String employmentType;
    private String location;
    private String workMode;
    private String requiredSkills;
    private BigDecimal totalCtcLpa;
    private BigDecimal fixedPayLpa;
    private BigDecimal variablePayLpa;
    private BigDecimal retentionBonusLpa;
    private BigDecimal monthlyStipendInr;
    private Boolean ppoEligible;
    private String selectionProcess;
    private Instant createdAt;
    private Instant updatedAt;
}
