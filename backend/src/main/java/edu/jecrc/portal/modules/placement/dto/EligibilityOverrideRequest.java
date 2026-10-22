package edu.jecrc.portal.modules.placement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityOverrideRequest {

    @NotBlank(message = "Mandatory override justification reason must be provided")
    private String reason;
}
