package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.EventRegistration;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record EventRegistrationDto(
        UUID id,
        UUID eventId,
        String eventTitle,
        UUID studentId,
        String rollNumber,
        String studentName,
        Instant registeredAt,
        String attendanceStatus
) {
    public static EventRegistrationDto from(EventRegistration r) {
        if (r == null) return null;
        return EventRegistrationDto.builder()
                .id(r.getId())
                .eventId(r.getEvent() != null ? r.getEvent().getId() : null)
                .eventTitle(r.getEvent() != null ? r.getEvent().getEventTitle() : null)
                .studentId(r.getStudent() != null ? r.getStudent().getId() : null)
                .rollNumber(r.getStudent() != null ? r.getStudent().getRollNumber() : null)
                .studentName(r.getStudent() != null ? r.getStudent().getFirstName() + " " + r.getStudent().getLastName() : null)
                .registeredAt(r.getRegisteredAt())
                .attendanceStatus(r.getAttendanceStatus())
                .build();
    }
}
