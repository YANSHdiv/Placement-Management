package edu.jecrc.portal.modules.academic.dto;

import edu.jecrc.portal.modules.academic.entity.SemesterResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterResultDto {
    private Integer id;

    @NotNull(message = "Semester number is required")
    private Integer semesterNumber;

    // Can be null
    private BigDecimal sgpa;
    private BigDecimal creditsRegistered;
    private BigDecimal creditsEarned;
    private String resultStatus;
    private Boolean hasActiveBacklog;
    private String marksheetDocumentRef;
    private String verificationStatus;
    private String verificationRemarks;
    private Instant verifiedAt;

    private List<SubjectAttemptDto> subjectAttempts;

    public static SemesterResultDto fromEntity(SemesterResult r) {
        if (r == null) return null;
        return SemesterResultDto.builder()
                .id(r.getId())
                .semesterNumber(r.getSemesterNumber())
                .sgpa(r.getSgpa())
                .creditsRegistered(r.getCreditsRegistered())
                .creditsEarned(r.getCreditsEarned())
                .resultStatus(r.getResultStatus())
                .hasActiveBacklog(r.getHasActiveBacklog())
                .marksheetDocumentRef(r.getMarksheetDocumentRef())
                .verificationStatus(r.getVerificationStatus())
                .verificationRemarks(r.getVerificationRemarks())
                .verifiedAt(r.getVerifiedAt())
                .subjectAttempts(r.getSubjectAttempts() != null
                        ? r.getSubjectAttempts().stream().map(SubjectAttemptDto::fromEntity).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
