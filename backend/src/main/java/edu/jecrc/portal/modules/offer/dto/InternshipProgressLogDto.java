package edu.jecrc.portal.modules.offer.dto;

import edu.jecrc.portal.modules.offer.entity.InternshipProgressLog;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipProgressLogDto {
    private UUID id;
    private UUID internshipId;
    private Integer logNumber;
    private LocalDate reportingPeriodStart;
    private LocalDate reportingPeriodEnd;
    private String summaryOfWork;
    private String learningOutcomes;
    private String challengesEncountered;
    private String evidenceAttachmentRef;
    private String submissionStatus;
    private String mentorFeedback;
    private Instant mentorReviewedAt;
    private String mentorReviewerName;
    private Instant submittedAt;
    private Instant updatedAt;

    public static InternshipProgressLogDto fromEntity(InternshipProgressLog l) {
        return InternshipProgressLogDto.builder()
                .id(l.getId())
                .internshipId(l.getInternship() != null ? l.getInternship().getId() : null)
                .logNumber(l.getLogNumber())
                .reportingPeriodStart(l.getReportingPeriodStart())
                .reportingPeriodEnd(l.getReportingPeriodEnd())
                .summaryOfWork(l.getSummaryOfWork())
                .learningOutcomes(l.getLearningOutcomes())
                .challengesEncountered(l.getChallengesEncountered())
                .evidenceAttachmentRef(l.getEvidenceAttachmentRef())
                .submissionStatus(l.getSubmissionStatus())
                .mentorFeedback(l.getMentorFeedback())
                .mentorReviewedAt(l.getMentorReviewedAt())
                .mentorReviewerName(l.getMentorReviewer() != null ? l.getMentorReviewer().getFullName() : null)
                .submittedAt(l.getSubmittedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
