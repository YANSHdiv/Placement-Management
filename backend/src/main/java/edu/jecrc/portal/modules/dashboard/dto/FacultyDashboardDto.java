package edu.jecrc.portal.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FacultyDashboardDto {
    private UUID facultyId;
    private String fullName;
    private String employeeId;
    private String designation;
    private String departmentName;
    private String departmentCode;

    private Long assignedClassesCount;
    private Long sessionsConductedToday;
    private Long sessionsConductedTotal;
    private Long assignedInternsCount;
    private Long pendingLogReviews;

    private List<TimetableSlotSummaryDto> timetableSlots;
    private List<AttendanceSessionSummaryDto> recentAttendanceSessions;
    private List<AssignedInternSummaryDto> assignedInterns;

    @Data
    @Builder
    public static class TimetableSlotSummaryDto {
        private UUID slotId;
        private String dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private String subjectCode;
        private String subjectName;
        private String classroomCode;
        private String section;
        private Integer semester;
    }

    @Data
    @Builder
    public static class AttendanceSessionSummaryDto {
        private UUID sessionId;
        private String subjectName;
        private LocalDate sessionDate;
        private String section;
        private Integer semester;
        private Long totalStudents;
        private Long presentStudents;
    }

    @Data
    @Builder
    public static class AssignedInternSummaryDto {
        private UUID internshipId;
        private String studentName;
        private String rollNumber;
        private String companyName;
        private String internshipStatus;
        private Long pendingLogsCount;
    }
}
