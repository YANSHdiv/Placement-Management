package edu.jecrc.portal.modules.placement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private UUID id;
    private String companyName;
    private String companyCode;
    private String website;
    private String industry;
    private String description;
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterPhone;
    private String companyStatus;
    private String internalNotes;
    private Instant createdAt;
    private Instant updatedAt;
}
