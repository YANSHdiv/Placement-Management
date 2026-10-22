package edu.jecrc.portal.modules.academic.ops.dto;

import java.time.LocalTime;
import java.util.UUID;

public record TimetableSlotUpdateRequest(
        Integer classroomId,
        UUID facultyId,
        Integer subjectId,
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String status,
        Boolean isActive
) {}
