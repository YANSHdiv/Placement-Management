package edu.jecrc.portal.modules.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationDto {
    private UUID id;
    private UUID jobProfileId;
    private String jobProfileTitle;
    private String companyName;
    private String driveTitle;
    private UUID studentId;
    private String studentRollNumber;
    private String studentName;
    private String studentEmail;
    private UUID evaluationId;
    private String evaluationOutcome;
    private Boolean isOverridden;
    private String applicationStatus;
    private String resumeVersionTag;
    private String studentRemarks;
    private Instant appliedAt;
    private Instant statusUpdatedAt;
    private String statusChangeReason;
}
