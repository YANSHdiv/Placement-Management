package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AttendanceBatchSubmitRequest(
        @NotEmpty(message = "Roster records cannot be empty")
        @Valid
        List<AttendanceRecordSubmitRequest> records
) {}
