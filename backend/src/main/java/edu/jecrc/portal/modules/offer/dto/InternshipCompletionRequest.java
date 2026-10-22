package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipCompletionRequest {

    @NotNull(message = "Actual end date is required")
    private LocalDate actualEndDate;

    private String status; // COMPLETED, TERMINATED
    private String completionRemarks;
}
