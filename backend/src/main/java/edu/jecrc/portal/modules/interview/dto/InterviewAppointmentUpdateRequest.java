package edu.jecrc.portal.modules.interview.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAppointmentUpdateRequest {
    private UUID evaluatorUserId;
    private Instant startTime;
    private Instant endTime;
    private String venueOrMeetingLink;
    private String status; // SCHEDULED, RESCHEDULED, COMPLETED, CANCELLED
    private String rescheduleReason;
}
