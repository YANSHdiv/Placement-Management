package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.CandidateInterviewAppointment;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAppointmentDto {
    private UUID id;
    private UUID roundCandidateId;
    private UUID roundId;
    private String roundName;
    private UUID jobProfileId;
    private String profileTitle;
    private String companyName;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private UUID evaluatorUserId;
    private String evaluatorName;
    private Instant startTime;
    private Instant endTime;
    private String venueOrMeetingLink;
    private String status; // SCHEDULED, RESCHEDULED, COMPLETED, CANCELLED
    private String rescheduleReason;
    private Instant createdAt;

    public static InterviewAppointmentDto fromEntity(CandidateInterviewAppointment a) {
        return InterviewAppointmentDto.builder()
                .id(a.getId())
                .roundCandidateId(a.getCandidate() != null ? a.getCandidate().getId() : null)
                .roundId(a.getCandidate() != null && a.getCandidate().getRound() != null ? a.getCandidate().getRound().getId() : null)
                .roundName(a.getCandidate() != null && a.getCandidate().getRound() != null ? a.getCandidate().getRound().getRoundName() : null)
                .jobProfileId(a.getCandidate() != null && a.getCandidate().getRound() != null && a.getCandidate().getRound().getJobProfile() != null
                        ? a.getCandidate().getRound().getJobProfile().getId() : null)
                .profileTitle(a.getCandidate() != null && a.getCandidate().getRound() != null && a.getCandidate().getRound().getJobProfile() != null
                        ? a.getCandidate().getRound().getJobProfile().getProfileTitle() : null)
                .companyName(a.getCandidate() != null && a.getCandidate().getRound() != null && a.getCandidate().getRound().getJobProfile() != null
                        && a.getCandidate().getRound().getJobProfile().getDrive() != null && a.getCandidate().getRound().getJobProfile().getDrive().getCompany() != null
                        ? a.getCandidate().getRound().getJobProfile().getDrive().getCompany().getCompanyName() : null)
                .studentId(a.getStudent() != null ? a.getStudent().getId() : null)
                .studentName(a.getStudent() != null ? a.getStudent().getFullName() : null)
                .studentEmail(a.getStudent() != null ? a.getStudent().getUniversityEmail() : null)
                .evaluatorUserId(a.getEvaluatorUser() != null ? a.getEvaluatorUser().getId() : null)
                .evaluatorName(a.getEvaluatorUser() != null ? a.getEvaluatorUser().getFullName() : null)
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .venueOrMeetingLink(a.getVenueOrMeetingLink())
                .status(a.getStatus())
                .rescheduleReason(a.getRescheduleReason())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
