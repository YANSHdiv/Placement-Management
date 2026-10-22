package edu.jecrc.portal.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DirectorDashboardDto {
    private Long totalEnrolledStudents;
    private Long totalVerifiedStudents;
    private Long totalActiveDrives;
    private Long totalCompaniesRegistered;
    private Long totalOffersIssued;
    private Long uniqueStudentsPlaced;
    private Double institutionalPlacementRate;

    private BigDecimal highestCtc;
    private BigDecimal averageCtc;
    private BigDecimal medianCtc;

    private Double institutionalAverageAttendance;

    private Map<String, Long> ctcTierDistribution;
    private List<DepartmentPlacementStatDto> departmentWiseStats;
    private List<RecentAuditActivityDto> recentAuditLogs;

    @Data
    @Builder
    public static class DepartmentPlacementStatDto {
        private Integer departmentId;
        private String departmentCode;
        private String departmentName;
        private Long totalStudents;
        private Long placedStudents;
        private Double placementRate;
        private BigDecimal highestCtc;
        private BigDecimal averageCtc;
    }

    @Data
    @Builder
    public static class RecentAuditActivityDto {
        private Long id;
        private String action;
        private String userEmail;
        private String entityName;
        private String ipAddress;
        private Instant createdAt;
    }
}
