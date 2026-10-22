package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipProgressLogCreateRequest {

    @NotNull(message = "Reporting period start is required")
    private LocalDate reportingPeriodStart;

    @NotNull(message = "Reporting period end is required")
    private LocalDate reportingPeriodEnd;

    @NotBlank(message = "Summary of work is required")
    private String summaryOfWork;

    private String learningOutcomes;
    private String challengesEncountered;
    private String evidenceAttachmentRef;
}
