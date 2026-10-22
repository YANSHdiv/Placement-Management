package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentOfferDecisionRequest {

    @NotBlank(message = "Decision must be ACCEPTED or DECLINED")
    private String decision; // ACCEPTED, DECLINED

    private String remarks;
}
