package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FacultyProfileCreateRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Faculty code is required")
        String facultyCode,

        @NotNull(message = "Department ID is required")
        Integer departmentId,

        @NotBlank(message = "Designation is required")
        String designation,

        String specialization
) {}
