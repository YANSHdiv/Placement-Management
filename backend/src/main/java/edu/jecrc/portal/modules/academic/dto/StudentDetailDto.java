package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.academic.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailDto {
    private UUID id;
    private UUID userId;
    private String universityStudentId;
    private String rollNumber;
    private String registrationNumber;
    private String firstName;
    private String lastName;
    private String universityEmail;
    private String personalEmail;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private Integer departmentId;
    private String departmentCode;
    private String departmentName;
    private Integer programId;
    private String programCode;
    private String programName;
    private Integer batchId;
    private String batchName;
    private Integer currentSemester;
    private String section;
    private LocalDate admissionDate;
    private String academicStatus;
    private String verificationStatus;
    private UUID verifiedByUserId;
    private String verifiedByEmail;
    private Instant verifiedAt;
    private String verificationRemarks;
    private BigDecimal cgpa;
    private Integer activeBacklogs;
    private Integer historicalBacklogs;
    private BigDecimal totalCreditsEarned;
    private Instant createdAt;
    private Instant updatedAt;

    private List<StudentQualificationDto> qualifications;
    private List<SemesterResultDto> semesterResults;

    public static StudentDetailDto fromEntity(Student s) {
        if (s == null) return null;
        return StudentDetailDto.builder()
                .id(s.getId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .universityStudentId(s.getUniversityStudentId())
                .rollNumber(s.getRollNumber())
                .registrationNumber(s.getRegistrationNumber())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .universityEmail(s.getUniversityEmail())
                .personalEmail(s.getPersonalEmail())
                .phoneNumber(s.getPhoneNumber())
                .dateOfBirth(s.getDateOfBirth())
                .gender(s.getGender())
                .departmentId(s.getDepartment() != null ? s.getDepartment().getId() : null)
                .departmentCode(s.getDepartment() != null ? s.getDepartment().getDepartmentCode() : null)
                .departmentName(s.getDepartment() != null ? s.getDepartment().getDepartmentName() : null)
                .programId(s.getProgram() != null ? s.getProgram().getId() : null)
                .programCode(s.getProgram() != null ? s.getProgram().getProgramCode() : null)
                .programName(s.getProgram() != null ? s.getProgram().getProgramName() : null)
                .batchId(s.getBatch() != null ? s.getBatch().getId() : null)
                .batchName(s.getBatch() != null ? s.getBatch().getBatchName() : null)
                .currentSemester(s.getCurrentSemester())
                .section(s.getSection())
                .admissionDate(s.getAdmissionDate())
                .academicStatus(s.getAcademicStatus())
                .verificationStatus(s.getVerificationStatus())
                .verifiedByUserId(s.getVerifiedBy() != null ? s.getVerifiedBy().getId() : null)
                .verifiedByEmail(s.getVerifiedBy() != null ? s.getVerifiedBy().getEmail() : null)
                .verifiedAt(s.getVerifiedAt())
                .verificationRemarks(s.getVerificationRemarks())
                .cgpa(s.getCgpa())
                .activeBacklogs(s.getActiveBacklogs())
                .historicalBacklogs(s.getHistoricalBacklogs())
                .totalCreditsEarned(s.getTotalCreditsEarned())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
