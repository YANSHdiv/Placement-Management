package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.academic.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryDto {
    private UUID id;
    private String universityStudentId;
    private String rollNumber;
    private String registrationNumber;
    private String firstName;
    private String lastName;
    private String universityEmail;
    private String phoneNumber;
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
    private String academicStatus;
    private String verificationStatus;
    private BigDecimal cgpa;
    private Integer activeBacklogs;
    private Integer historicalBacklogs;

    public static StudentSummaryDto fromEntity(Student s) {
        if (s == null) return null;
        return StudentSummaryDto.builder()
                .id(s.getId())
                .universityStudentId(s.getUniversityStudentId())
                .rollNumber(s.getRollNumber())
                .registrationNumber(s.getRegistrationNumber())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .universityEmail(s.getUniversityEmail())
                .phoneNumber(s.getPhoneNumber())
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
                .academicStatus(s.getAcademicStatus())
                .verificationStatus(s.getVerificationStatus())
                .cgpa(s.getCgpa())
                .activeBacklogs(s.getActiveBacklogs())
                .historicalBacklogs(s.getHistoricalBacklogs())
                .build();
    }
}
