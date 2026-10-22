package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAppointmentCreateRequest {

    @NotNull(message = "Round candidate ID is required")
    private UUID roundCandidateId;

    private UUID evaluatorUserId;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    private String venueOrMeetingLink;
}
