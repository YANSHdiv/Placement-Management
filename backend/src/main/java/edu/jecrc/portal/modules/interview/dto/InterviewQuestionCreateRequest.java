package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestionCreateRequest {
    private UUID companyId;
    private UUID jobProfileId;
    private String roundType;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Question type is required")
    private String questionType; // MULTIPLE_CHOICE, CODING_QUESTION, TECHNICAL_DISCUSSION, BEHAVIORAL_QUESTION, HR_QUESTION, OPEN_ENDED

    @NotBlank(message = "Category is required")
    private String category;

    @Builder.Default
    private String difficulty = "MEDIUM"; // EASY, MEDIUM, HARD

    private String tags;
    private String expectedAnswerGuidance;

    @Builder.Default
    private Boolean isConfidential = true;
}
