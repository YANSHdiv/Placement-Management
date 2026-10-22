package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record FacultyTeachingAssignmentCreateRequest(
        @NotNull(message = "Faculty ID is required")
        UUID facultyId,

        @NotNull(message = "Subject ID is required")
        Integer subjectId,

        @NotNull(message = "Batch ID is required")
        Integer batchId,

        @NotBlank(message = "Section is required")
        String section,

        @NotNull(message = "Semester is required")
        @Positive(message = "Semester must be positive")
        Integer semester,

        String academicYear
) {}
