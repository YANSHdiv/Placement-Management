package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResultUpdateRequest {

    @NotBlank(message = "Candidate status is required")
    private String candidateRoundStatus; // SCHEDULED, IN_PROGRESS, ADVANCED, ELIMINATED, ON_HOLD, ABSENT, EXEMPTED

    private BigDecimal finalScore;
    private String remarks;
}
