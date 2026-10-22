package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.AttendanceRecord;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record AttendanceRecordDto(
        UUID id,
        UUID sessionId,
        UUID studentId,
        String rollNumber,
        String studentName,
        String attendanceStatus,
        Instant markedAt,
        String verificationMethod,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        BigDecimal calculatedDistanceMeters,
        String geofenceStatus,
        String evidencePhotoRef,
        String remarks,
        Boolean isCorrected,
        String originalStatus,
        String correctionReason,
        String correctedByName,
        Instant correctedAt
) {
    public static AttendanceRecordDto from(AttendanceRecord r) {
        if (r == null) return null;
        return AttendanceRecordDto.builder()
                .id(r.getId())
                .sessionId(r.getSession() != null ? r.getSession().getId() : null)
                .studentId(r.getStudent() != null ? r.getStudent().getId() : null)
                .rollNumber(r.getStudent() != null ? r.getStudent().getRollNumber() : null)
                .studentName(r.getStudent() != null ? r.getStudent().getFirstName() + " " + r.getStudent().getLastName() : null)
                .attendanceStatus(r.getAttendanceStatus())
                .markedAt(r.getMarkedAt())
                .verificationMethod(r.getVerificationMethod())
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .accuracyMeters(r.getAccuracyMeters())
                .calculatedDistanceMeters(r.getCalculatedDistanceMeters())
                .geofenceStatus(r.getGeofenceStatus())
                .evidencePhotoRef(r.getEvidencePhotoRef())
                .remarks(r.getRemarks())
                .isCorrected(r.getIsCorrected())
                .originalStatus(r.getOriginalStatus())
                .correctionReason(r.getCorrectionReason())
                .correctedByName(r.getCorrectedBy() != null
                        ? r.getCorrectedBy().getFirstName() + " " + r.getCorrectedBy().getLastName() : null)
                .correctedAt(r.getCorrectedAt())
                .build();
    }
}
