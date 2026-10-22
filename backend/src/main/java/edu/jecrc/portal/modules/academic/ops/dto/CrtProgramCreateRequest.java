package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;

public record CrtProgramCreateRequest(
        @NotBlank(message = "Program code is required")
        String programCode,

        @NotBlank(message = "Program name is required")
        String programName,

        String description,
        String academicYear,
        Integer targetBatchId
) {}
