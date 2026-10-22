package edu.jecrc.portal.modules.offer.dto;

import edu.jecrc.portal.modules.offer.entity.InternshipRecord;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipRecordDto {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String studentRollNumber;
    private String studentEmail;
    private String departmentName;
    private UUID companyId;
    private String companyName;
    private UUID jobProfileId;
    private String profileTitle;
    private UUID applicationId;
    private UUID placementOfferId;
    private String internshipTitle;
    private String internshipType;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private BigDecimal monthlyStipendInr;
    private String currency;
    private String workArrangement;
    private String location;
    private String appointmentDocRef;
    private UUID facultyMentorId;
    private String facultyMentorName;
    private String facultyMentorEmail;
    private String industryMentorName;
    private String industryMentorEmail;
    private String industryMentorPhone;
    private String internshipStatus;
    private Boolean hasPpoPotential;
    private String completionRemarks;
    private Instant completedAt;
    private Integer logCount;
    private Integer evaluationCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static InternshipRecordDto fromEntity(InternshipRecord r) {
        return InternshipRecordDto.builder()
                .id(r.getId())
                .studentId(r.getStudent() != null ? r.getStudent().getId() : null)
                .studentName(r.getStudent() != null ? r.getStudent().getFullName() : null)
                .studentRollNumber(r.getStudent() != null ? r.getStudent().getUniversityStudentId() : null)
                .studentEmail(r.getStudent() != null ? r.getStudent().getUniversityEmail() : null)
                .departmentName(r.getStudent() != null && r.getStudent().getDepartment() != null
                        ? r.getStudent().getDepartment().getDepartmentName() : null)
                .companyId(r.getCompany() != null ? r.getCompany().getId() : null)
                .companyName(r.getCompany() != null ? r.getCompany().getCompanyName() : null)
                .jobProfileId(r.getJobProfile() != null ? r.getJobProfile().getId() : null)
                .profileTitle(r.getJobProfile() != null ? r.getJobProfile().getProfileTitle() : null)
                .applicationId(r.getApplication() != null ? r.getApplication().getId() : null)
                .placementOfferId(r.getPlacementOffer() != null ? r.getPlacementOffer().getId() : null)
                .internshipTitle(r.getInternshipTitle())
                .internshipType(r.getInternshipType())
                .startDate(r.getStartDate())
                .expectedEndDate(r.getExpectedEndDate())
                .actualEndDate(r.getActualEndDate())
                .monthlyStipendInr(r.getMonthlyStipendInr())
                .currency(r.getCurrency())
                .workArrangement(r.getWorkArrangement())
                .location(r.getLocation())
                .appointmentDocRef(r.getAppointmentDocRef())
                .facultyMentorId(r.getFacultyMentor() != null ? r.getFacultyMentor().getId() : null)
                .facultyMentorName(r.getFacultyMentor() != null ? r.getFacultyMentor().getFullName() : null)
                .facultyMentorEmail(r.getFacultyMentor() != null ? r.getFacultyMentor().getEmail() : null)
                .industryMentorName(r.getIndustryMentorName())
                .industryMentorEmail(r.getIndustryMentorEmail())
                .industryMentorPhone(r.getIndustryMentorPhone())
                .internshipStatus(r.getInternshipStatus())
                .hasPpoPotential(r.getHasPpoPotential())
                .completionRemarks(r.getCompletionRemarks())
                .completedAt(r.getCompletedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
