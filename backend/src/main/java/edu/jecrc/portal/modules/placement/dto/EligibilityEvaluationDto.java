package edu.jecrc.portal.modules.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityEvaluationDto {
    private UUID id;
    private UUID studentId;
    private String studentRollNumber;
    private String studentName;
    private String studentEmail;
    private String studentDepartment;
    private UUID jobProfileId;
    private String jobProfileTitle;
    private String companyName;
    private UUID policyId;
    private String policyCode;
    private Integer policyVersion;
    private String outcome; // ELIGIBLE, INELIGIBLE, NEEDS_VERIFICATION
    private Map<String, Object> criteriaSnapshot;
    private List<String> unmetCriteria;
    private List<String> missingVerification;
    private Instant evaluatedAt;
    private Boolean isOverridden;
    private String overrideReason;
    private String overrideApprovedBy;
    private Instant overriddenAt;
}
