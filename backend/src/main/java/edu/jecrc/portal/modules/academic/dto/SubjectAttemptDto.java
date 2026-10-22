package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.academic.entity.SubjectAttempt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectAttemptDto {
    private Integer id;

    @NotBlank(message = "Subject code is required")
    private String subjectCode;

    @NotBlank(message = "Subject name is required")
    private String subjectName;

    @NotNull(message = "Semester number is required")
    private Integer semesterNumber;

    @Builder.Default
    private Integer attemptNumber = 1;

    @Builder.Default
    private BigDecimal credits = new BigDecimal("3.0");

    private String grade;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;

    @Builder.Default
    private Boolean isBacklog = false;

    @Builder.Default
    private Boolean isCleared = false;

    private Integer clearedInSemester;

    public static SubjectAttemptDto fromEntity(SubjectAttempt a) {
        if (a == null) return null;
        return SubjectAttemptDto.builder()
                .id(a.getId())
                .subjectCode(a.getSubjectCode())
                .subjectName(a.getSubjectName())
                .semesterNumber(a.getSemesterNumber())
                .attemptNumber(a.getAttemptNumber())
                .credits(a.getCredits())
                .grade(a.getGrade())
                .marksObtained(a.getMarksObtained())
                .maxMarks(a.getMaxMarks())
                .isBacklog(a.getIsBacklog())
                .isCleared(a.getIsCleared())
                .clearedInSemester(a.getClearedInSemester())
                .build();
    }
}
