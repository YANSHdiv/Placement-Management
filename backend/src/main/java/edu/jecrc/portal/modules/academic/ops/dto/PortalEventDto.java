package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.PortalEvent;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record PortalEventDto(
        UUID id,
        String eventTitle,
        String eventType,
        String description,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        String venue,
        String onlineMeetingUrl,
        String targetAudience,
        Integer departmentId,
        String departmentName,
        Integer batchId,
        String batchName,
        Instant registrationDeadline,
        Integer maxParticipants,
        long registeredCount,
        String organizerName,
        String status,
        Instant publishedAt,
        Instant createdAt
) {
    public static PortalEventDto from(PortalEvent e, long registeredCount) {
        if (e == null) return null;
        return PortalEventDto.builder()
                .id(e.getId())
                .eventTitle(e.getEventTitle())
                .eventType(e.getEventType())
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .venue(e.getVenue())
                .onlineMeetingUrl(e.getOnlineMeetingUrl())
                .targetAudience(e.getTargetAudience())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getDepartmentName() : null)
                .batchId(e.getBatch() != null ? e.getBatch().getId() : null)
                .batchName(e.getBatch() != null ? e.getBatch().getBatchName() : null)
                .registrationDeadline(e.getRegistrationDeadline())
                .maxParticipants(e.getMaxParticipants())
                .registeredCount(registeredCount)
                .organizerName(e.getOrganizerName())
                .status(e.getStatus())
                .publishedAt(e.getPublishedAt())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
