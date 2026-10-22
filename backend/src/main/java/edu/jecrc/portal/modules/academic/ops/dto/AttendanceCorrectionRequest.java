package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;

public record AttendanceCorrectionRequest(
        @NotBlank(message = "New status is required")
        String newStatus, // PRESENT, ABSENT, LATE, EXCUSED

        @NotBlank(message = "Correction reason is mandatory")
        String correctionReason
) {}
