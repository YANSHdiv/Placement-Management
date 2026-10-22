package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementOfferIssueRequest {

    @NotNull(message = "Decision deadline is required")
    private Instant decisionDeadline;

    private String offerLetterDocRef;
    private String termsAndConditions;
}
