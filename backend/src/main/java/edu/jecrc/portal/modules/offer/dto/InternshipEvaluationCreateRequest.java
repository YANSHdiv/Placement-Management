package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipEvaluationCreateRequest {

    private String evaluationType; // MID_TERM, FINAL, AD_HOC

    private BigDecimal technicalScore;
    private BigDecimal communicationScore;
    private BigDecimal professionalismScore;

    @NotNull(message = "Overall score is required")
    private BigDecimal overallScore;

    private String writtenFeedback;
    private String confidentialStaffNotes;
    private Boolean recommendForPpo;
}
