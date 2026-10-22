package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.RoundCandidate;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundCandidateDto {
    private UUID id;
    private UUID roundId;
    private String roundName;
    private UUID applicationId;
    private UUID studentId;
    private String universityStudentId;
    private String studentName;
    private String studentEmail;
    private String departmentName;
    private String candidateRoundStatus;
    private BigDecimal finalScore;
    private String tpoDecisionRemarks;
    private Boolean isPublished;
    private Instant publishedAt;
    private String publishedByName;
    private Integer evaluationCount;
    private Instant createdAt;

    public static RoundCandidateDto fromEntity(RoundCandidate rc) {
        return RoundCandidateDto.builder()
                .id(rc.getId())
                .roundId(rc.getRound() != null ? rc.getRound().getId() : null)
                .roundName(rc.getRound() != null ? rc.getRound().getRoundName() : null)
                .applicationId(rc.getApplication() != null ? rc.getApplication().getId() : null)
                .studentId(rc.getApplication() != null && rc.getApplication().getStudent() != null ? rc.getApplication().getStudent().getId() : null)
                .universityStudentId(rc.getApplication() != null && rc.getApplication().getStudent() != null ? rc.getApplication().getStudent().getUniversityStudentId() : null)
                .studentName(rc.getApplication() != null && rc.getApplication().getStudent() != null ? rc.getApplication().getStudent().getFullName() : null)
                .studentEmail(rc.getApplication() != null && rc.getApplication().getStudent() != null ? rc.getApplication().getStudent().getUniversityEmail() : null)
                .departmentName(rc.getApplication() != null && rc.getApplication().getStudent() != null && rc.getApplication().getStudent().getDepartment() != null
                        ? rc.getApplication().getStudent().getDepartment().getDepartmentName() : null)
                .candidateRoundStatus(rc.getCandidateRoundStatus())
                .finalScore(rc.getFinalScore())
                .tpoDecisionRemarks(rc.getTpoDecisionRemarks())
                .isPublished(rc.getIsPublished())
                .publishedAt(rc.getPublishedAt())
                .publishedByName(rc.getPublishedBy() != null ? rc.getPublishedBy().getFullName() : null)
                .evaluationCount(rc.getEvaluations() != null ? rc.getEvaluations().size() : 0)
                .createdAt(rc.getCreatedAt())
                .build();
    }
}
