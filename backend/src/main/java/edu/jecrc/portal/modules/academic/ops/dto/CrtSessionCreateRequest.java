package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CrtSessionCreateRequest(
        @NotNull(message = "Program ID is required")
        UUID programId,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Category is required")
        String category, // APTITUDE, CODING, COMMUNICATION, INTERVIEW_PREP, RESUME_BUILDING

        @NotNull(message = "Session date is required")
        LocalDate sessionDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        String venue,
        String trainerName,
        String trainerEmail,
        Integer maxSeats
) {}
