package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record PortalEventCreateRequest(
        @NotBlank(message = "Event title is required")
        String eventTitle,

        @NotBlank(message = "Event type is required")
        String eventType, // WORKSHOP, SEMINAR, HACKATHON, PRE_PLACEMENT_TALK, CAREER_SESSION, ANNOUNCEMENT

        String description,

        @NotNull(message = "Event date is required")
        LocalDate eventDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        String venue,
        String onlineMeetingUrl,
        String targetAudience, // ALL, BTECH_CSE, FINAL_YEAR, FACULTY_ONLY, PLACEMENT_ELIGIBLE
        Integer departmentId,
        Integer batchId,
        Instant registrationDeadline,
        Integer maxParticipants,
        String organizerName
) {}
