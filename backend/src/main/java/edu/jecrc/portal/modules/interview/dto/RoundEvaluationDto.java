package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.RoundEvaluation;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundEvaluationDto {
    private UUID id;
    private UUID roundCandidateId;
    private UUID roundId;
    private String roundName;
    private String studentName;
    private String universityStudentId;
    private UUID evaluatorUserId;
    private String evaluatorName;
    private BigDecimal score;
    private String evaluationState; // DRAFT, SUBMITTED
    private String recommendation; // STRONG_HIRE, HIRE, BORDERLINE, REJECT
    private String qualitativeFeedback;
    private String confidentialNotes;
    private Instant submittedAt;
    private Instant updatedAt;

    public static RoundEvaluationDto fromEntity(RoundEvaluation re, boolean includeConfidential) {
        return RoundEvaluationDto.builder()
                .id(re.getId())
                .roundCandidateId(re.getCandidate() != null ? re.getCandidate().getId() : null)
                .roundId(re.getCandidate() != null && re.getCandidate().getRound() != null ? re.getCandidate().getRound().getId() : null)
                .roundName(re.getCandidate() != null && re.getCandidate().getRound() != null ? re.getCandidate().getRound().getRoundName() : null)
                .studentName(re.getCandidate() != null && re.getCandidate().getApplication() != null && re.getCandidate().getApplication().getStudent() != null
                        ? re.getCandidate().getApplication().getStudent().getFullName() : null)
                .universityStudentId(re.getCandidate() != null && re.getCandidate().getApplication() != null && re.getCandidate().getApplication().getStudent() != null
                        ? re.getCandidate().getApplication().getStudent().getUniversityStudentId() : null)
                .evaluatorUserId(re.getEvaluatorUser() != null ? re.getEvaluatorUser().getId() : null)
                .evaluatorName(re.getEvaluatorUser() != null ? re.getEvaluatorUser().getFullName() : null)
                .score(re.getScore())
                .evaluationState(re.getEvaluationState())
                .recommendation(re.getRecommendation())
                .qualitativeFeedback(re.getQualitativeFeedback())
                .confidentialNotes(includeConfidential ? re.getConfidentialNotes() : null)
                .submittedAt(re.getSubmittedAt())
                .updatedAt(re.getUpdatedAt())
                .build();
    }
}
