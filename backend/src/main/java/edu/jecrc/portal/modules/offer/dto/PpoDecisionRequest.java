package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PpoDecisionRequest {

    @NotBlank(message = "Decision must be RECOMMENDED, APPROVED, or DECLINED")
    private String decision; // RECOMMENDED, APPROVED, DECLINED

    private String decisionRemarks;

    // Optional fields when converting directly into a formal placement offer upon APPROVAL
    private BigDecimal totalCtcLpa;
    private BigDecimal fixedSalaryLpa;
    private BigDecimal variableSalaryLpa;
    private BigDecimal joiningBonus;
    private BigDecimal retentionBonus;
    private String jobLocation;
    private String termsAndConditions;
    private Instant decisionDeadline;
}
