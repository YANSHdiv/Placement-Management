package edu.jecrc.portal.modules.interview.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRoundUpdateRequest {
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
    private String publicationStatus; // DRAFT, CONFIGURED, SCHEDULED, IN_PROGRESS, COMPLETED
}
