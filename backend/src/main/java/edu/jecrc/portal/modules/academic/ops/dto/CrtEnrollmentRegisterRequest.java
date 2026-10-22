package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CrtEnrollmentRegisterRequest(
        @NotNull(message = "Session ID is required")
        UUID sessionId
) {}
