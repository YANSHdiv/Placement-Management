package edu.jecrc.portal.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class TpoDashboardDto {
    private Long totalRecruiters;
    private Long activeDrives;
    private Long totalApplications;
    private Long totalOffersIssued;
    private Long uniqueStudentsPlaced;
    private Long activeInterviewRounds;
    private Long pendingVerifications;
    private Long pendingEligibilityOverrides;
    private Double placementPercentage;

    private BigDecimal highestCtc;
    private BigDecimal averageCtc;
    private BigDecimal medianCtc;

    private Map<String, Long> tierDistribution;
    private List<TopRecruiterDto> topRecruiters;
    private List<DriveSummaryDto> recentDrives;
    private List<OfferSummaryDto> recentOffers;

    @Data
    @Builder
    public static class TopRecruiterDto {
        private String companyName;
        private Long offersCount;
        private BigDecimal highestCtc;
        private BigDecimal averageCtc;
    }

    @Data
    @Builder
    public static class DriveSummaryDto {
        private UUID driveId;
        private String driveName;
        private String companyName;
        private String driveStatus;
        private LocalDate driveDate;
        private Long applicationsCount;
    }

    @Data
    @Builder
    public static class OfferSummaryDto {
        private UUID offerId;
        private String studentName;
        private String rollNumber;
        private String departmentCode;
        private String companyName;
        private BigDecimal ctcLpa;
        private String offerStatus;
        private Instant issuanceDate;
    }
}
