package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicBatchDto {
    private Integer id;
    private String batchName;
    private Integer startYear;
    private Integer graduationYear;
    private Integer programId;
    private String programCode;
    private String programName;
    private Boolean isPlacementActive;
    private Integer currentSemester;
    private Boolean isActive;

    public static AcademicBatchDto fromEntity(AcademicBatch batch) {
        if (batch == null) return null;
        return AcademicBatchDto.builder()
                .id(batch.getId())
                .batchName(batch.getBatchName())
                .startYear(batch.getStartYear())
                .graduationYear(batch.getGraduationYear())
                .programId(batch.getProgram() != null ? batch.getProgram().getId() : null)
                .programCode(batch.getProgram() != null ? batch.getProgram().getProgramCode() : null)
                .programName(batch.getProgram() != null ? batch.getProgram().getProgramName() : null)
                .isPlacementActive(batch.getIsPlacementActive())
                .currentSemester(batch.getCurrentSemester())
                .isActive(batch.getIsActive())
                .build();
    }
}
