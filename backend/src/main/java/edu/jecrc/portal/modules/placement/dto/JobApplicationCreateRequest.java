package edu.jecrc.portal.modules.placement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationCreateRequest {

    @NotBlank(message = "Resume version tag is required (e.g. RESUME_V2_2026)")
    @Size(max = 100)
    private String resumeVersionTag;

    private String studentRemarks;
}
