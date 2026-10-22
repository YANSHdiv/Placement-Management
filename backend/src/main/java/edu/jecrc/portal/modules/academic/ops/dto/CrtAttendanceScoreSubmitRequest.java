package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CrtAttendanceScoreSubmitRequest(
        @NotNull(message = "Student ID is required")
        UUID studentId,

        @NotBlank(message = "Attendance status is required")
        String attendanceStatus, // REGISTERED, ATTENDED, ABSENT, EXCUSED

        BigDecimal score,
        Integer feedbackRating,
        String feedbackComments
) {}
