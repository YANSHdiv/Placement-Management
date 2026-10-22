package edu.jecrc.portal.modules.academic.ops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ClassroomCreateRequest(
        @NotBlank(message = "Room number is required")
        String roomNumber,

        @NotBlank(message = "Building name is required")
        String buildingName,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be positive")
        Integer capacity,

        String roomType,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer geofenceRadiusMeters
) {}
