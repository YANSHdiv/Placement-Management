package edu.jecrc.portal.modules.academic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentVerifyRequest {

    @NotBlank(message = "Verification status is required (VERIFIED, REJECTED, CHANGES_REQUESTED)")
    private String verificationStatus;

    private String verificationRemarks;
}
