package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.AttendanceSession;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record AttendanceSessionDto(
        UUID id,
        UUID slotId,
        Integer subjectId,
        String subjectCode,
        String subjectName,
        UUID facultyId,
        String facultyName,
        String facultyCode,
        Integer batchId,
        String batchName,
        Integer semester,
        String section,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        String topicCovered,
        String sessionStatus,
        Boolean isGeofenceEnabled,
        BigDecimal authorizedLatitude,
        BigDecimal authorizedLongitude,
        Integer geofenceRadiusMeters,
        Instant openedAt,
        Instant finalizedAt,
        String finalizedByName,
        Instant createdAt
) {
    public static AttendanceSessionDto from(AttendanceSession s) {
        if (s == null) return null;
        return AttendanceSessionDto.builder()
                .id(s.getId())
                .slotId(s.getSlot() != null ? s.getSlot().getId() : null)
                .subjectId(s.getSubject() != null ? s.getSubject().getId() : null)
                .subjectCode(s.getSubject() != null ? s.getSubject().getSubjectCode() : null)
                .subjectName(s.getSubject() != null ? s.getSubject().getSubjectName() : null)
                .facultyId(s.getFaculty() != null ? s.getFaculty().getId() : null)
                .facultyName(s.getFaculty() != null && s.getFaculty().getUser() != null
                        ? s.getFaculty().getUser().getFirstName() + " " + s.getFaculty().getUser().getLastName() : null)
                .facultyCode(s.getFaculty() != null ? s.getFaculty().getFacultyCode() : null)
                .batchId(s.getBatch() != null ? s.getBatch().getId() : null)
                .batchName(s.getBatch() != null ? s.getBatch().getBatchName() : null)
                .semester(s.getSemester())
                .section(s.getSection())
                .sessionDate(s.getSessionDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .topicCovered(s.getTopicCovered())
                .sessionStatus(s.getSessionStatus())
                .isGeofenceEnabled(s.getIsGeofenceEnabled())
                .authorizedLatitude(s.getAuthorizedLatitude())
                .authorizedLongitude(s.getAuthorizedLongitude())
                .geofenceRadiusMeters(s.getGeofenceRadiusMeters())
                .openedAt(s.getOpenedAt())
                .finalizedAt(s.getFinalizedAt())
                .finalizedByName(s.getFinalizedBy() != null
                        ? s.getFinalizedBy().getFirstName() + " " + s.getFinalizedBy().getLastName() : null)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
