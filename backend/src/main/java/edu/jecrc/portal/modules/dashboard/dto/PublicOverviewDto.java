package edu.jecrc.portal.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PublicOverviewDto {
    private String institutionName;
    private String tagline;
    private String officialWebsite;
    private String placementsWebsite;
    private String campusAddress;
    private String admissionsEmail;
    private String placementsEmail;
    private String contactPhone;
    
    // Official Placement Metrics (Batch 2026, reported through 31 March 2026)
    private String academicYear;
    private String reportingPeriod;
    private String sourceAttribution;
    private Long totalStudentsPlaced;
    private Long totalOffers;
    private BigDecimal highestPackageLpa;
    private BigDecimal averagePackageLpa;
    private BigDecimal top30PercentAveragePackageLpa;
    private Long recruitingPartnersCount;
    private Long fortune500Offers;
    private Long activeDrivesCount;
    
    // Showcases
    private List<String> topRecruitingPartners;
    private List<SchoolOverviewDto> academicSchools;
    private List<String> placementMilestones;

    @Data
    @Builder
    public static class SchoolOverviewDto {
        private String schoolName;
        private String code;
        private List<String> keyPrograms;
    }
}
