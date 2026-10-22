package edu.jecrc.portal.modules.placement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PlacementDriveCreateRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Drive title is required")
    @Size(max = 255)
    private String driveTitle;

    @NotBlank(message = "Academic year is required")
    @Size(max = 20)
    private String academicYear;

    private Instant registrationStartDate;
    private Instant registrationDeadline;
    private LocalDate driveDate;
    private String recruitmentSchedule;
    private String venue;
    private String reportingTime;
    private String instructions;
    private String contactDetails;
}
