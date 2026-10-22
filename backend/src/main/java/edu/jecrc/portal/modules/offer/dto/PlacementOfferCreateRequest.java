package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementOfferCreateRequest {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    private UUID applicationId;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    private UUID jobProfileId;

    @NotBlank(message = "Designation is required")
    private String designation;

    private String employmentType; // FULL_TIME, INTERNSHIP_PLUS_FULL_TIME

    @NotNull(message = "Total CTC (LPA) is required")
    private BigDecimal totalCtcLpa;

    private BigDecimal fixedSalaryLpa;
    private BigDecimal variableSalaryLpa;
    private BigDecimal joiningBonus;
    private BigDecimal retentionBonus;
    private BigDecimal monthlyStipendInr;
    private String jobLocation;
    private String termsAndConditions;
    private String offerLetterDocRef;
}
