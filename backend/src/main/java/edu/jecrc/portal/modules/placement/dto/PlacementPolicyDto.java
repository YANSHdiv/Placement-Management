package edu.jecrc.portal.modules.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementPolicyDto {
    private UUID id;
    private String policyCode;
    private String policyName;
    private Integer versionNumber;
    private String policyStatus;
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
    private List<String> eligibleProgramNames;
    private List<Integer> eligibleBatchIds;
    private List<String> eligibleBatchNames;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
