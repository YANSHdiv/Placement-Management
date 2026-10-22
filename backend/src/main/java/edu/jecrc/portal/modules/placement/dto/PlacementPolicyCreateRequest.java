package edu.jecrc.portal.modules.placement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementPolicyCreateRequest {

    @NotBlank(message = "Policy code is required")
    @Size(max = 100)
    private String policyCode;

    @NotBlank(message = "Policy name is required")
    @Size(max = 255)
    private String policyName;

    private BigDecimal minCgpa;
    private BigDecimal minClass10Percentage;
    private BigDecimal minClass12Percentage;
    private BigDecimal minDiplomaPercentage;
    private Integer maxActiveBacklogs;
    private Integer maxHistoricalBacklogs;
    private Boolean allowAcademicGaps;
    private Integer maxAcademicGapYears;
    private Boolean requiresAllQualificationsVerified;
    private Boolean requiresCgpaVerified;
    private String criteriaPayloadJson;

    private List<Integer> eligibleProgramIds;
    private List<Integer> eligibleBatchIds;
}
