package edu.jecrc.portal.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StudentDashboardDto {
    private UUID studentId;
    private String fullName;
    private String rollNumber;
    private String universityEmail;
    private String departmentName;
    private String departmentCode;
    private String programName;
    private Integer graduationYear;
    private BigDecimal cgpa;
    private Integer activeBacklogs;
    private String verificationStatus;

    // Stat counts
    private Long appliedDrivesCount;
    private Long activeDrivesCount;
    private Long shortlistedCount;
    private Long offersCount;

    // Attendance summary
    private AttendanceStatDto attendanceSummary;

    // Lists
    private List<StudentAppSummaryDto> recentApplications;
    private List<UpcomingDriveDto> upcomingDrives;
    private List<StudentOfferSummaryDto> offers;
    private List<StudentCrtSummaryDto> crtSessions;

    @Data
    @Builder
    public static class AttendanceStatDto {
        private Long totalSessions;
        private Long presentSessions;
        private Double attendancePercentage;
        private Boolean shortageWarning; // true if < 75%
    }

    @Data
    @Builder
    public static class StudentAppSummaryDto {
        private UUID applicationId;
        private UUID jobProfileId;
        private String companyName;
        private String designation;
        private BigDecimal ctcLpa;
        private String applicationStatus;
        private Instant appliedAt;
    }

    @Data
    @Builder
    public static class UpcomingDriveDto {
        private UUID driveId;
        private String driveName;
        private String companyName;
        private Instant registrationDeadline;
        private LocalDate driveDate;
        private String venue;
    }

    @Data
    @Builder
    public static class StudentOfferSummaryDto {
        private UUID offerId;
        private String offerReferenceNumber;
        private String companyName;
        private String designation;
        private BigDecimal ctcLpa;
        private String offerStatus;
        private Instant decisionDeadline;
        private Instant issuanceDate;
    }

    @Data
    @Builder
    public static class StudentCrtSummaryDto {
        private UUID enrollmentId;
        private String programName;
        private String moduleName;
        private String sessionTopic;
        private LocalDate sessionDate;
        private String attendanceStatus;
        private BigDecimal score;
    }
}
