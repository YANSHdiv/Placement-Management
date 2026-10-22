package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewFeedbackCreateRequest {

    @NotBlank(message = "Feedback type is required")
    private String feedbackType; // STUDENT_TO_DRIVE, EVALUATOR_TO_CANDIDATE, TPO_TO_COMPANY

    private UUID applicationId;
    private UUID roundId;
    private UUID companyId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Minimum rating is 1")
    @Max(value = 5, message = "Maximum rating is 5")
    private Integer rating;

    private String comments;

    @Builder.Default
    private Boolean isConfidentialToStaff = false;
}
