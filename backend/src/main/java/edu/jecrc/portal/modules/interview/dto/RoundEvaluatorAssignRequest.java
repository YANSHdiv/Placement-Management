package edu.jecrc.portal.modules.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundEvaluatorAssignRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String roleOrDesignation;
}
