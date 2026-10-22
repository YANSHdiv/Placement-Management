package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.AcademicSubject;
import lombok.Builder;

import java.time.Instant;

@Builder
public record AcademicSubjectDto(
        Integer id,
        Integer departmentId,
        String departmentName,
        Integer programId,
        String programName,
        String subjectCode,
        String subjectName,
        Integer credits,
        Integer semester,
        Boolean isActive,
        Instant createdAt
) {
    public static AcademicSubjectDto from(AcademicSubject s) {
        if (s == null) return null;
        return AcademicSubjectDto.builder()
                .id(s.getId())
                .departmentId(s.getDepartment() != null ? s.getDepartment().getId() : null)
                .departmentName(s.getDepartment() != null ? s.getDepartment().getDepartmentName() : null)
                .programId(s.getProgram() != null ? s.getProgram().getId() : null)
                .programName(s.getProgram() != null ? s.getProgram().getProgramName() : null)
                .subjectCode(s.getSubjectCode())
                .subjectName(s.getSubjectName())
                .credits(s.getCredits())
                .semester(s.getSemester())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
