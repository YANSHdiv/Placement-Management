package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.CrtEnrollment;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CrtEnrollmentDto(
        UUID id,
        UUID sessionId,
        String sessionTitle,
        UUID studentId,
        String rollNumber,
        String studentName,
        String attendanceStatus,
        BigDecimal score,
        Integer feedbackRating,
        String feedbackComments,
        Instant markedAt,
        Instant createdAt
) {
    public static CrtEnrollmentDto from(CrtEnrollment e) {
        if (e == null) return null;
        return CrtEnrollmentDto.builder()
                .id(e.getId())
                .sessionId(e.getSession() != null ? e.getSession().getId() : null)
                .sessionTitle(e.getSession() != null ? e.getSession().getTitle() : null)
                .studentId(e.getStudent() != null ? e.getStudent().getId() : null)
                .rollNumber(e.getStudent() != null ? e.getStudent().getRollNumber() : null)
                .studentName(e.getStudent() != null ? e.getStudent().getFirstName() + " " + e.getStudent().getLastName() : null)
                .attendanceStatus(e.getAttendanceStatus())
                .score(e.getScore())
                .feedbackRating(e.getFeedbackRating())
                .feedbackComments(e.getFeedbackComments())
                .markedAt(e.getMarkedAt())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
