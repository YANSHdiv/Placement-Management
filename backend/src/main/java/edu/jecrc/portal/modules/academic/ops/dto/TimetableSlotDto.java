package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.TimetableSlot;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record TimetableSlotDto(
        UUID id,
        Integer batchId,
        String batchName,
        Integer semester,
        String section,
        Integer subjectId,
        String subjectCode,
        String subjectName,
        UUID facultyId,
        String facultyName,
        String facultyCode,
        Integer classroomId,
        String roomNumber,
        String buildingName,
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String academicYear,
        String status,
        Boolean isActive,
        Instant createdAt
) {
    public static TimetableSlotDto from(TimetableSlot s) {
        if (s == null) return null;
        return TimetableSlotDto.builder()
                .id(s.getId())
                .batchId(s.getBatch() != null ? s.getBatch().getId() : null)
                .batchName(s.getBatch() != null ? s.getBatch().getBatchName() : null)
                .semester(s.getSemester())
                .section(s.getSection())
                .subjectId(s.getSubject() != null ? s.getSubject().getId() : null)
                .subjectCode(s.getSubject() != null ? s.getSubject().getSubjectCode() : null)
                .subjectName(s.getSubject() != null ? s.getSubject().getSubjectName() : null)
                .facultyId(s.getFaculty() != null ? s.getFaculty().getId() : null)
                .facultyName(s.getFaculty() != null && s.getFaculty().getUser() != null
                        ? s.getFaculty().getUser().getFirstName() + " " + s.getFaculty().getUser().getLastName() : null)
                .facultyCode(s.getFaculty() != null ? s.getFaculty().getFacultyCode() : null)
                .classroomId(s.getClassroom() != null ? s.getClassroom().getId() : null)
                .roomNumber(s.getClassroom() != null ? s.getClassroom().getRoomNumber() : null)
                .buildingName(s.getClassroom() != null ? s.getClassroom().getBuildingName() : null)
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .academicYear(s.getAcademicYear())
                .status(s.getStatus())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
