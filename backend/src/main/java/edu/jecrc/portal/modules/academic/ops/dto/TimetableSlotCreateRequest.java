package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;
import java.util.UUID;

public record TimetableSlotCreateRequest(
        @NotNull(message = "Batch ID is required")
        Integer batchId,

        @NotNull(message = "Semester is required")
        @Positive(message = "Semester must be positive")
        Integer semester,

        @NotBlank(message = "Section is required")
        String section,

        @NotNull(message = "Subject ID is required")
        Integer subjectId,

        @NotNull(message = "Faculty ID is required")
        UUID facultyId,

        @NotNull(message = "Classroom ID is required")
        Integer classroomId,

        @NotBlank(message = "Day of week is required")
        String dayOfWeek, // MONDAY, TUESDAY, etc.

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        String academicYear,
        String status // DRAFT, PUBLISHED
) {}
