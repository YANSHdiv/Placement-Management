package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipCreateRequest {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    private UUID jobProfileId;
    private UUID applicationId;
    private UUID placementOfferId;

    @NotBlank(message = "Internship title is required")
    private String internshipTitle;

    private String internshipType; // SUMMER, SEMESTER, WINTER, FULL_TIME_CONCURRENT

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Expected end date is required")
    private LocalDate expectedEndDate;

    private BigDecimal monthlyStipendInr;
    private String currency;
    private String workArrangement; // ON_SITE, HYBRID, REMOTE
    private String location;
    private String appointmentDocRef;
    private UUID facultyMentorId;
    private String industryMentorName;
    private String industryMentorEmail;
    private String industryMentorPhone;
    private Boolean hasPpoPotential;
}
