package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.CrtProgram;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CrtProgramDto(
        UUID id,
        String programCode,
        String programName,
        String description,
        String academicYear,
        Integer targetBatchId,
        String targetBatchName,
        Boolean isActive,
        Instant createdAt
) {
    public static CrtProgramDto from(CrtProgram p) {
        if (p == null) return null;
        return CrtProgramDto.builder()
                .id(p.getId())
                .programCode(p.getProgramCode())
                .programName(p.getProgramName())
                .description(p.getDescription())
                .academicYear(p.getAcademicYear())
                .targetBatchId(p.getTargetBatch() != null ? p.getTargetBatch().getId() : null)
                .targetBatchName(p.getTargetBatch() != null ? p.getTargetBatch().getBatchName() : null)
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
