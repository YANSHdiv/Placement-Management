package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.academic.entity.StudentQualification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentQualificationDto {
    private Integer id;

    @NotBlank(message = "Qualification type is required (e.g. CLASS_10, CLASS_12, DIPLOMA, GRADUATION)")
    private String qualificationType;

    @NotBlank(message = "Board or University name is required")
    private String boardOrUniversity;

    private String institutionName;

    @NotNull(message = "Passing year is required")
    private Integer passingYear;

    @Builder.Default
    private String markingScheme = "PERCENTAGE"; // PERCENTAGE, CGPA

    // Percentage or CGPA (can be null, must NOT be coerced to 0.0)
    private BigDecimal percentageOrCgpa;

    private BigDecimal maximumMarks;
    private BigDecimal obtainedMarks;
    private String marksheetDocumentRef;
    private String verificationStatus;
    private String verificationRemarks;
    private Instant verifiedAt;

    public static StudentQualificationDto fromEntity(StudentQualification q) {
        if (q == null) return null;
        return StudentQualificationDto.builder()
                .id(q.getId())
                .qualificationType(q.getQualificationType())
                .boardOrUniversity(q.getBoardOrUniversity())
                .institutionName(q.getInstitutionName())
                .passingYear(q.getPassingYear())
                .markingScheme(q.getMarkingScheme())
                .percentageOrCgpa(q.getPercentageOrCgpa())
                .maximumMarks(q.getMaximumMarks())
                .obtainedMarks(q.getObtainedMarks())
                .marksheetDocumentRef(q.getMarksheetDocumentRef())
                .verificationStatus(q.getVerificationStatus())
                .verificationRemarks(q.getVerificationRemarks())
                .verifiedAt(q.getVerifiedAt())
                .build();
    }
}
