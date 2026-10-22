package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.FacultyTeachingAssignment;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FacultyTeachingAssignmentDto(
        UUID id,
        UUID facultyId,
        String facultyName,
        String facultyCode,
        Integer subjectId,
        String subjectCode,
        String subjectName,
        Integer batchId,
        String batchName,
        String section,
        Integer semester,
        String academicYear,
        Boolean isActive,
        Instant createdAt
) {
    public static FacultyTeachingAssignmentDto from(FacultyTeachingAssignment a) {
        if (a == null) return null;
        return FacultyTeachingAssignmentDto.builder()
                .id(a.getId())
                .facultyId(a.getFaculty() != null ? a.getFaculty().getId() : null)
                .facultyName(a.getFaculty() != null && a.getFaculty().getUser() != null
                        ? a.getFaculty().getUser().getFirstName() + " " + a.getFaculty().getUser().getLastName() : null)
                .facultyCode(a.getFaculty() != null ? a.getFaculty().getFacultyCode() : null)
                .subjectId(a.getSubject() != null ? a.getSubject().getId() : null)
                .subjectCode(a.getSubject() != null ? a.getSubject().getSubjectCode() : null)
                .subjectName(a.getSubject() != null ? a.getSubject().getSubjectName() : null)
                .batchId(a.getBatch() != null ? a.getBatch().getId() : null)
                .batchName(a.getBatch() != null ? a.getBatch().getBatchName() : null)
                .section(a.getSection())
                .semester(a.getSemester())
                .academicYear(a.getAcademicYear())
                .isActive(a.getIsActive())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
