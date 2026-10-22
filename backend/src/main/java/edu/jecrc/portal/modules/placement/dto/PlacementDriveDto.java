package edu.jecrc.portal.modules.placement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDriveDto {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String companyCode;
    private String driveTitle;
    private String academicYear;
    private String driveStatus;
    private Instant registrationStartDate;
    private Instant registrationDeadline;
    private LocalDate driveDate;
    private String recruitmentSchedule;
    private String venue;
    private String reportingTime;
    private String instructions;
    private String contactDetails;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer jobProfilesCount;
}
