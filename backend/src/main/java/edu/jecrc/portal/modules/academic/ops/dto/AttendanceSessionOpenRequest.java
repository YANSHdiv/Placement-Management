package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AttendanceSessionOpenRequest(
        UUID slotId,

        @NotNull(message = "Subject ID is required")
        Integer subjectId,

        @NotNull(message = "Batch ID is required")
        Integer batchId,

        @NotNull(message = "Semester is required")
        @Positive(message = "Semester must be positive")
        Integer semester,

        @NotBlank(message = "Section is required")
        String section,

        @NotNull(message = "Session date is required")
        LocalDate sessionDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        String topicCovered,
        Boolean isGeofenceEnabled,
        BigDecimal authorizedLatitude,
        BigDecimal authorizedLongitude,
        Integer geofenceRadiusMeters
) {}
