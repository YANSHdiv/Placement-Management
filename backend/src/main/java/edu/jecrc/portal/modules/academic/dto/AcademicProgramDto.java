package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.academic.entity.AcademicProgram;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProgramDto {
    private Integer id;
    private String programCode;
    private String programName;
    private String degreeType;
    private Integer departmentId;
    private String departmentName;
    private Integer durationYears;
    private Integer totalSemesters;
    private Boolean isActive;

    public static AcademicProgramDto fromEntity(AcademicProgram program) {
        if (program == null) return null;
        return AcademicProgramDto.builder()
                .id(program.getId())
                .programCode(program.getProgramCode())
                .programName(program.getProgramName())
                .degreeType(program.getDegreeType())
                .departmentId(program.getDepartment() != null ? program.getDepartment().getId() : null)
                .departmentName(program.getDepartment() != null ? program.getDepartment().getDepartmentName() : null)
                .durationYears(program.getDurationYears())
                .totalSemesters(program.getTotalSemesters())
                .isActive(program.getIsActive())
                .build();
    }
}
