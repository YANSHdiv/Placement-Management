package edu.jecrc.portal.modules.offer.dto;

import edu.jecrc.portal.modules.offer.entity.InternshipEvaluation;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipEvaluationDto {
    private UUID id;
    private UUID internshipId;
    private UUID evaluatorUserId;
    private String evaluatorName;
    private String evaluationType;
    private BigDecimal technicalScore;
    private BigDecimal communicationScore;
    private BigDecimal professionalismScore;
    private BigDecimal overallScore;
    private String writtenFeedback;
    private String confidentialStaffNotes; // Null/redacted for student callers
    private Boolean recommendForPpo;
    private Instant evaluatedAt;

    public static InternshipEvaluationDto fromEntity(InternshipEvaluation e, boolean maskConfidentialNotes) {
        return InternshipEvaluationDto.builder()
                .id(e.getId())
                .internshipId(e.getInternship() != null ? e.getInternship().getId() : null)
                .evaluatorUserId(e.getEvaluatorUser() != null ? e.getEvaluatorUser().getId() : null)
                .evaluatorName(e.getEvaluatorUser() != null ? e.getEvaluatorUser().getFullName() : null)
                .evaluationType(e.getEvaluationType())
                .technicalScore(e.getTechnicalScore())
                .communicationScore(e.getCommunicationScore())
                .professionalismScore(e.getProfessionalismScore())
                .overallScore(e.getOverallScore())
                .writtenFeedback(e.getWrittenFeedback())
                .confidentialStaffNotes(maskConfidentialNotes ? null : e.getConfidentialStaffNotes())
                .recommendForPpo(e.getRecommendForPpo())
                .evaluatedAt(e.getEvaluatedAt())
                .build();
    }
}
