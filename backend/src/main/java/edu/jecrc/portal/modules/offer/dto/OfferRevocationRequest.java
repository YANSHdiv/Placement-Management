package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRevocationRequest {

    @NotBlank(message = "Revocation reason is required")
    private String reason;
}
