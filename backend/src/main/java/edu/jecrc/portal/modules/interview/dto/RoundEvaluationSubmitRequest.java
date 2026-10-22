package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundEvaluationSubmitRequest {

    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    private BigDecimal score;

    private String recommendation; // STRONG_HIRE, HIRE, BORDERLINE, REJECT
    private String qualitativeFeedback;
    private String confidentialNotes;

    @Builder.Default
    private Boolean isDraft = false;
}
