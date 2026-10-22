package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.CrtSession;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record CrtSessionDto(
        UUID id,
        UUID programId,
        String programName,
        String programCode,
        String title,
        String category,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        String venue,
        String trainerName,
        String trainerEmail,
        String status,
        Integer maxSeats,
        long enrolledCount,
        Instant createdAt
) {
    public static CrtSessionDto from(CrtSession s, long enrolledCount) {
        if (s == null) return null;
        return CrtSessionDto.builder()
                .id(s.getId())
                .programId(s.getCrtProgram() != null ? s.getCrtProgram().getId() : null)
                .programName(s.getCrtProgram() != null ? s.getCrtProgram().getProgramName() : null)
                .programCode(s.getCrtProgram() != null ? s.getCrtProgram().getProgramCode() : null)
                .title(s.getTitle())
                .category(s.getCategory())
                .sessionDate(s.getSessionDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .venue(s.getVenue())
                .trainerName(s.getTrainerName())
                .trainerEmail(s.getTrainerEmail())
                .status(s.getStatus())
                .maxSeats(s.getMaxSeats())
                .enrolledCount(enrolledCount)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
