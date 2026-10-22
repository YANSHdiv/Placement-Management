package edu.jecrc.portal.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PlacementAnalyticsDto {
    // Cohort Scope
    private String scopeDescription;
    private Integer departmentId;
    private String departmentCode;
    private String academicYear;

    // Student Cohort Funnel
    private Long totalStudents;
    private Long eligibleStudents;
    private Long participatingStudents;
    private Long selectedCandidates;
    private Long totalOffersIssued;
    private Long uniqueStudentsPlaced;

    // Placement Percentages
    private Double placementRateOnEligible; // (uniqueStudentsPlaced / eligibleStudents) * 100
    private Double placementRateOnTotal;    // (uniqueStudentsPlaced / totalStudents) * 100

    // Compensation Percentiles & Metrics (over accepted offers)
    private CompensationMetricsDto compensation;

    // Tier Distribution (REGULAR, DREAM, SUPER_DREAM)
    private TierDistributionDto tierDistribution;

    // Department-wise breakdown
    private List<DepartmentAnalyticsDto> departmentBreakdown;

    // Top Recruiters
    private List<TopRecruiterAnalyticsDto> topRecruiters;

    // Monthly placement timeline
    private List<MonthlyPlacementStatDto> monthlyTimeline;

    // Policy & Statistical Notes
    private String policyNotes;

    @Data
    @Builder
    public static class CompensationMetricsDto {
        private BigDecimal highestCtc;
        private BigDecimal averageCtc;
        private BigDecimal medianCtc;
        private BigDecimal lowestCtc;
    }

    @Data
    @Builder
    public static class TierDistributionDto {
        private Long regularCount;    // CTC < 10 LPA
        private Long dreamCount;      // 10 LPA <= CTC < 25 LPA
        private Long superDreamCount; // CTC >= 25 LPA
    }

    @Data
    @Builder
    public static class DepartmentAnalyticsDto {
        private Integer departmentId;
        private String departmentCode;
        private String departmentName;
        private Long totalStudents;
        private Long eligibleStudents;
        private Long placedStudents;
        private Double placementRate;
        private BigDecimal highestCtc;
        private BigDecimal averageCtc;
    }

    @Data
    @Builder
    public static class TopRecruiterAnalyticsDto {
        private String companyName;
        private String industry;
        private Long offersCount;
        private Long acceptedCount;
        private BigDecimal highestCtc;
        private BigDecimal averageCtc;
    }

    @Data
    @Builder
    public static class MonthlyPlacementStatDto {
        private String monthLabel; // e.g. "2026-09"
        private Long offersIssued;
        private Long offersAccepted;
    }
}
