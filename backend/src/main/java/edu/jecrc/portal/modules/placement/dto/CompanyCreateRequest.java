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
public class CompanyCreateRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255)
    private String companyName;

    @NotBlank(message = "Company code is required")
    @Size(max = 50)
    private String companyCode;

    private String website;
    private String industry;
    private String description;
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterPhone;
    private String internalNotes;
}
