package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.AttendanceCorrectionHistory;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AttendanceCorrectionHistoryDto(
        UUID id,
        UUID recordId,
        UUID sessionId,
        UUID studentId,
        String rollNumber,
        String studentName,
        String previousStatus,
        String newStatus,
        String correctionReason,
        String actorName,
        Instant createdAt
) {
    public static AttendanceCorrectionHistoryDto from(AttendanceCorrectionHistory h) {
        if (h == null) return null;
        return AttendanceCorrectionHistoryDto.builder()
                .id(h.getId())
                .recordId(h.getRecord() != null ? h.getRecord().getId() : null)
                .sessionId(h.getSession() != null ? h.getSession().getId() : null)
                .studentId(h.getStudent() != null ? h.getStudent().getId() : null)
                .rollNumber(h.getStudent() != null ? h.getStudent().getRollNumber() : null)
                .studentName(h.getStudent() != null ? h.getStudent().getFirstName() + " " + h.getStudent().getLastName() : null)
                .previousStatus(h.getPreviousStatus())
                .newStatus(h.getNewStatus())
                .correctionReason(h.getCorrectionReason())
                .actorName(h.getActorUser() != null
                        ? h.getActorUser().getFirstName() + " " + h.getActorUser().getLastName() : null)
                .createdAt(h.getCreatedAt())
                .build();
    }
}
