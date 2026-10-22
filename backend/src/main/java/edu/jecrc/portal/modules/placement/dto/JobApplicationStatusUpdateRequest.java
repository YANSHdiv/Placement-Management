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
public class JobApplicationStatusUpdateRequest {

    @NotBlank(message = "New application status is required")
    private String status; // APPLIED, IN_PROCESS, WITHDRAWN, REJECTED, OFFERED, PLACED

    private String reason;
}
