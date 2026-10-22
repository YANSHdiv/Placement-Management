package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.InterviewRound;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRoundDto {
    private UUID id;
    private UUID jobProfileId;
    private String profileTitle;
    private String companyName;
    private String roundName;
    private String roundType;
    private Integer sequenceOrder;
    private String instructions;
    private Instant startTime;
    private Instant endTime;
    private String venueOrMeetingUrl;
    private String candidateSchedulingMode;
    private String evaluationCriteria;
    private BigDecimal maxScore;
    private String scoringScaleDescription;
    private String publicationStatus;
    private Boolean isResultsPublished;
    private Instant resultsPublishedAt;
    private String resultsPublishedByName;
    private Integer evaluatorCount;
    private Integer candidateCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static InterviewRoundDto fromEntity(InterviewRound r) {
        return InterviewRoundDto.builder()
                .id(r.getId())
                .jobProfileId(r.getJobProfile() != null ? r.getJobProfile().getId() : null)
                .profileTitle(r.getJobProfile() != null ? r.getJobProfile().getProfileTitle() : null)
                .companyName(r.getJobProfile() != null && r.getJobProfile().getDrive() != null && r.getJobProfile().getDrive().getCompany() != null
                        ? r.getJobProfile().getDrive().getCompany().getCompanyName() : null)
                .roundName(r.getRoundName())
                .roundType(r.getRoundType())
                .sequenceOrder(r.getSequenceOrder())
                .instructions(r.getInstructions())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .venueOrMeetingUrl(r.getVenueOrMeetingUrl())
                .candidateSchedulingMode(r.getCandidateSchedulingMode())
                .evaluationCriteria(r.getEvaluationCriteria())
                .maxScore(r.getMaxScore())
                .scoringScaleDescription(r.getScoringScaleDescription())
                .publicationStatus(r.getPublicationStatus())
                .isResultsPublished(r.getIsResultsPublished())
                .resultsPublishedAt(r.getResultsPublishedAt())
                .resultsPublishedByName(r.getResultsPublishedBy() != null ? r.getResultsPublishedBy().getFullName() : null)
                .evaluatorCount(r.getEvaluators() != null ? r.getEvaluators().size() : 0)
                .candidateCount(r.getCandidates() != null ? r.getCandidates().size() : 0)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
