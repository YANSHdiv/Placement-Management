package edu.jecrc.portal.modules.interview.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestionUpdateRequest {
    private UUID companyId;
    private UUID jobProfileId;
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
}
