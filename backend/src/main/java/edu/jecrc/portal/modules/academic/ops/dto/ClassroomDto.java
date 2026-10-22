package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.Classroom;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record ClassroomDto(
        Integer id,
        String roomNumber,
        String buildingName,
        Integer capacity,
        String roomType,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer geofenceRadiusMeters,
        Boolean isActive,
        Instant createdAt
) {
    public static ClassroomDto from(Classroom c) {
        if (c == null) return null;
        return ClassroomDto.builder()
                .id(c.getId())
                .roomNumber(c.getRoomNumber())
                .buildingName(c.getBuildingName())
                .capacity(c.getCapacity())
                .roomType(c.getRoomType())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .geofenceRadiusMeters(c.getGeofenceRadiusMeters())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
