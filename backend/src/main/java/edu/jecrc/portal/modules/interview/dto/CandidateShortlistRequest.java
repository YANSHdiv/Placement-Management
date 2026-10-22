package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateShortlistRequest {

    @NotEmpty(message = "At least one application ID must be provided")
    private List<UUID> applicationIds;

    private String remarks;
}
