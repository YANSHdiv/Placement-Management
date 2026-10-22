package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AttendanceRecordSubmitRequest(
        @NotNull(message = "Student ID is required")
        UUID studentId,

        @NotBlank(message = "Attendance status is required")
        String attendanceStatus, // PRESENT, ABSENT, LATE, EXCUSED

        String verificationMethod, // MANUAL_ROSTER, GEOTAGGED_SELF, QR_CODE
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        String evidencePhotoRef,
        String remarks
) {}
