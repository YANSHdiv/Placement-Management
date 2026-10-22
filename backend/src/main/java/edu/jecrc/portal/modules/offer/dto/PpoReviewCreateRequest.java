package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PpoReviewCreateRequest {

    private UUID targetJobProfileId;
    private BigDecimal minimumEvaluationScoreRequired;
    private String recommendationNotes;
}
