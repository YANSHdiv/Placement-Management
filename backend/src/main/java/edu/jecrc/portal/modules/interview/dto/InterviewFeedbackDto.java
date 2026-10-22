package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.InterviewFeedback;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewFeedbackDto {
    private UUID id;
    private String feedbackType;
    private UUID applicationId;
    private UUID roundId;
    private String roundName;
    private UUID companyId;
    private String companyName;
    private UUID submittedByUserId;
    private String submittedByName;
    private Integer rating;
    private String comments;
    private Boolean isConfidentialToStaff;
    private String moderationStatus;
    private Instant createdAt;

    public static InterviewFeedbackDto fromEntity(InterviewFeedback f, boolean canViewConfidential) {
        return InterviewFeedbackDto.builder()
                .id(f.getId())
                .feedbackType(f.getFeedbackType())
                .applicationId(f.getApplication() != null ? f.getApplication().getId() : null)
                .roundId(f.getRound() != null ? f.getRound().getId() : null)
                .roundName(f.getRound() != null ? f.getRound().getRoundName() : null)
                .companyId(f.getCompany() != null ? f.getCompany().getId() : null)
                .companyName(f.getCompany() != null ? f.getCompany().getCompanyName() : null)
                .submittedByUserId(f.getSubmittedBy() != null ? f.getSubmittedBy().getId() : null)
                .submittedByName(f.getSubmittedBy() != null ? f.getSubmittedBy().getFullName() : null)
                .rating(f.getRating())
                .comments(f.getIsConfidentialToStaff() && !canViewConfidential ? "[Confidential to staff]" : f.getComments())
                .isConfidentialToStaff(f.getIsConfidentialToStaff())
                .moderationStatus(f.getModerationStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
