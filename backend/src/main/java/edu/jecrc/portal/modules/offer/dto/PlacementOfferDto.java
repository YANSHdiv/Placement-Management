package edu.jecrc.portal.modules.offer.dto;

import edu.jecrc.portal.modules.offer.entity.PlacementOffer;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementOfferDto {
    private UUID id;
    private String offerReferenceNumber;
    private UUID studentId;
    private String studentName;
    private String studentRollNumber;
    private String studentEmail;
    private String departmentName;
    private UUID applicationId;
    private UUID companyId;
    private String companyName;
    private UUID jobProfileId;
    private String profileTitle;
    private UUID driveId;
    private String driveTitle;
    private String designation;
    private String employmentType;
    private BigDecimal totalCtcLpa;
    private BigDecimal fixedSalaryLpa;
    private BigDecimal variableSalaryLpa;
    private BigDecimal joiningBonus;
    private BigDecimal retentionBonus;
    private BigDecimal monthlyStipendInr;
    private String jobLocation;
    private String termsAndConditions;
    private String offerLetterDocRef;
    private String offerStatus;
    private Instant issuanceDate;
    private Instant decisionDeadline;
    private String studentDecision;
    private Instant studentDecisionAt;
    private String studentDecisionRemarks;
    private String revocationReason;
    private Instant revokedAt;
    private String createdByName;
    private String issuedByName;
    private Integer effectivePolicyVersion;
    private Instant createdAt;
    private Instant updatedAt;

    public static PlacementOfferDto fromEntity(PlacementOffer o) {
        return PlacementOfferDto.builder()
                .id(o.getId())
                .offerReferenceNumber(o.getOfferReferenceNumber())
                .studentId(o.getStudent() != null ? o.getStudent().getId() : null)
                .studentName(o.getStudent() != null ? o.getStudent().getFullName() : null)
                .studentRollNumber(o.getStudent() != null ? o.getStudent().getUniversityStudentId() : null)
                .studentEmail(o.getStudent() != null ? o.getStudent().getUniversityEmail() : null)
                .departmentName(o.getStudent() != null && o.getStudent().getDepartment() != null
                        ? o.getStudent().getDepartment().getDepartmentName() : null)
                .applicationId(o.getApplication() != null ? o.getApplication().getId() : null)
                .companyId(o.getCompany() != null ? o.getCompany().getId() : null)
                .companyName(o.getCompany() != null ? o.getCompany().getCompanyName() : null)
                .jobProfileId(o.getJobProfile() != null ? o.getJobProfile().getId() : null)
                .profileTitle(o.getJobProfile() != null ? o.getJobProfile().getProfileTitle() : null)
                .driveId(o.getDrive() != null ? o.getDrive().getId() : null)
                .driveTitle(o.getDrive() != null ? o.getDrive().getDriveTitle() : null)
                .designation(o.getDesignation())
                .employmentType(o.getEmploymentType())
                .totalCtcLpa(o.getTotalCtcLpa())
                .fixedSalaryLpa(o.getFixedSalaryLpa())
                .variableSalaryLpa(o.getVariableSalaryLpa())
                .joiningBonus(o.getJoiningBonus())
                .retentionBonus(o.getRetentionBonus())
                .monthlyStipendInr(o.getMonthlyStipendInr())
                .jobLocation(o.getJobLocation())
                .termsAndConditions(o.getTermsAndConditions())
                .offerLetterDocRef(o.getOfferLetterDocRef())
                .offerStatus(o.getOfferStatus())
                .issuanceDate(o.getIssuanceDate())
                .decisionDeadline(o.getDecisionDeadline())
                .studentDecision(o.getStudentDecision())
                .studentDecisionAt(o.getStudentDecisionAt())
                .studentDecisionRemarks(o.getStudentDecisionRemarks())
                .revocationReason(o.getRevocationReason())
                .revokedAt(o.getRevokedAt())
                .createdByName(o.getCreatedBy() != null ? o.getCreatedBy().getFullName() : null)
                .issuedByName(o.getIssuedBy() != null ? o.getIssuedBy().getFullName() : null)
                .effectivePolicyVersion(o.getEffectivePolicyVersion())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
