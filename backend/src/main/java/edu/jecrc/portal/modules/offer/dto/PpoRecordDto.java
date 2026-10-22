package edu.jecrc.portal.modules.offer.dto;

import edu.jecrc.portal.modules.offer.entity.PpoRecord;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PpoRecordDto {
    private UUID id;
    private UUID internshipId;
    private String internshipTitle;
    private UUID studentId;
    private String studentName;
    private String studentRollNumber;
    private String studentEmail;
    private UUID companyId;
    private String companyName;
    private UUID targetJobProfileId;
    private String targetProfileTitle;
    private String ppoStatus;
    private BigDecimal minimumEvaluationScoreRequired;
    private String recommendationNotes;
    private String decisionRemarks;
    private String recommendedByName;
    private String decidedByName;
    private Instant decidedAt;
    private UUID resultingOfferId;
    private String resultingOfferReferenceNumber;
    private Instant createdAt;
    private Instant updatedAt;

    public static PpoRecordDto fromEntity(PpoRecord p) {
        return PpoRecordDto.builder()
                .id(p.getId())
                .internshipId(p.getInternship() != null ? p.getInternship().getId() : null)
                .internshipTitle(p.getInternship() != null ? p.getInternship().getInternshipTitle() : null)
                .studentId(p.getStudent() != null ? p.getStudent().getId() : null)
                .studentName(p.getStudent() != null ? p.getStudent().getFullName() : null)
                .studentRollNumber(p.getStudent() != null ? p.getStudent().getUniversityStudentId() : null)
                .studentEmail(p.getStudent() != null ? p.getStudent().getUniversityEmail() : null)
                .companyId(p.getCompany() != null ? p.getCompany().getId() : null)
                .companyName(p.getCompany() != null ? p.getCompany().getCompanyName() : null)
                .targetJobProfileId(p.getTargetJobProfile() != null ? p.getTargetJobProfile().getId() : null)
                .targetProfileTitle(p.getTargetJobProfile() != null ? p.getTargetJobProfile().getProfileTitle() : null)
                .ppoStatus(p.getPpoStatus())
                .minimumEvaluationScoreRequired(p.getMinimumEvaluationScoreRequired())
                .recommendationNotes(p.getRecommendationNotes())
                .decisionRemarks(p.getDecisionRemarks())
                .recommendedByName(p.getRecommendedBy() != null ? p.getRecommendedBy().getFullName() : null)
                .decidedByName(p.getDecidedBy() != null ? p.getDecidedBy().getFullName() : null)
                .decidedAt(p.getDecidedAt())
                .resultingOfferId(p.getResultingOffer() != null ? p.getResultingOffer().getId() : null)
                .resultingOfferReferenceNumber(p.getResultingOffer() != null ? p.getResultingOffer().getOfferReferenceNumber() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
