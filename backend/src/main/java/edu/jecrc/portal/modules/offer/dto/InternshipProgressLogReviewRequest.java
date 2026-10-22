package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipProgressLogReviewRequest {

    @NotBlank(message = "Submission status must be REVIEWED or CORRECTION_REQUESTED")
    private String submissionStatus; // REVIEWED, CORRECTION_REQUESTED

    @NotBlank(message = "Mentor feedback is required")
    private String mentorFeedback;
}
