package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.InterviewQuestion;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestionDto {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private UUID jobProfileId;
    private String profileTitle;
    private String roundType;
    private String title;
    private String questionText;
    private String questionType;
    private String category;
    private String difficulty;
    private String tags;
    private String expectedAnswerGuidance;
    private Boolean isConfidential;
    private Boolean isArchived;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;

    public static InterviewQuestionDto fromEntity(InterviewQuestion q, boolean includeConfidential) {
        return InterviewQuestionDto.builder()
                .id(q.getId())
                .companyId(q.getCompany() != null ? q.getCompany().getId() : null)
                .companyName(q.getCompany() != null ? q.getCompany().getCompanyName() : null)
                .jobProfileId(q.getJobProfile() != null ? q.getJobProfile().getId() : null)
                .profileTitle(q.getJobProfile() != null ? q.getJobProfile().getProfileTitle() : null)
                .roundType(q.getRoundType())
                .title(q.getTitle())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType())
                .category(q.getCategory())
                .difficulty(q.getDifficulty())
                .tags(q.getTags())
                .expectedAnswerGuidance(includeConfidential ? q.getExpectedAnswerGuidance() : null)
                .isConfidential(q.getIsConfidential())
                .isArchived(q.getIsArchived())
                .createdByName(q.getCreatedBy() != null ? q.getCreatedBy().getFullName() : null)
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }
}
