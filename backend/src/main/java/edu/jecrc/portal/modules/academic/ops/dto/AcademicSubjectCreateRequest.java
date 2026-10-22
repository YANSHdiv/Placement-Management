package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AcademicSubjectCreateRequest(
        @NotNull(message = "Department ID is required")
        Integer departmentId,

        Integer programId,

        @NotBlank(message = "Subject code is required")
        String subjectCode,

        @NotBlank(message = "Subject name is required")
        String subjectName,

        @NotNull(message = "Credits is required")
        @Positive(message = "Credits must be positive")
        Integer credits,

        @NotNull(message = "Semester is required")
        @Positive(message = "Semester must be positive")
        Integer semester
) {}
