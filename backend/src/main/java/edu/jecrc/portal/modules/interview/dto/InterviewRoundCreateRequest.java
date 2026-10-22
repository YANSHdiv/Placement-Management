package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRoundCreateRequest {

    @NotBlank(message = "Round name is required")
    private String roundName;

    @NotBlank(message = "Round type is required")
    private String roundType; // APTITUDE, ONLINE_ASSESSMENT, CODING_ASSESSMENT, TECHNICAL_INTERVIEW, MANAGERIAL_INTERVIEW, HR_INTERVIEW, GROUP_DISCUSSION, CUSTOM

    @NotNull(message = "Sequence order is required")
    private Integer sequenceOrder;

    private String instructions;
    private Instant startTime;
    private Instant endTime;
    private String venueOrMeetingUrl;

    @Builder.Default
    private String candidateSchedulingMode = "SCHEDULED_SLOTS"; // SCHEDULED_SLOTS, WALK_IN, FLEXIBLE

    private String evaluationCriteria;
    private BigDecimal maxScore;
    private String scoringScaleDescription;
}
