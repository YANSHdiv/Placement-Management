package edu.jecrc.portal.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HodDashboardDto {
    private Integer departmentId;
    private String departmentCode;
    private String departmentName;

    // Academic Registry stats
    private Long totalStudents;
    private Long verifiedStudents;
    private Long pendingVerifications;
    private Long rejectedVerifications;

    // Department Placement stats
    private Long participatingStudents;
    private Long uniqueStudentsPlaced;
    private Long totalOffersReceived;
    private Double departmentPlacementPercentage;
    private BigDecimal highestCtc;
    private BigDecimal averageCtc;

    // Faculty & Attendance stats
    private Long totalFacultyCount;
    private Double departmentAverageAttendance;
    private Long attendanceShortageCount; // students < 75%

    private List<ShortageStudentDto> studentsInShortage;
    private List<RecentDepartmentOfferDto> recentOffers;

    @Data
    @Builder
    public static class ShortageStudentDto {
        private UUID studentId;
        private String rollNumber;
        private String fullName;
        private Integer currentSemester;
        private String section;
        private Long totalSessions;
        private Long presentSessions;
        private Double attendancePercentage;
    }

    @Data
    @Builder
    public static class RecentDepartmentOfferDto {
        private UUID offerId;
        private String studentName;
        private String rollNumber;
        private String companyName;
        private BigDecimal ctcLpa;
        private String offerStatus;
    }
}
