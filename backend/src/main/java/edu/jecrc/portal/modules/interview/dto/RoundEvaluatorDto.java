package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.RoundEvaluator;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundEvaluatorDto {
    private UUID id;
    private UUID roundId;
    private UUID userId;
    private String evaluatorName;
    private String evaluatorEmail;
    private String roleOrDesignation;
    private String assignedByName;
    private Instant createdAt;

    public static RoundEvaluatorDto fromEntity(RoundEvaluator re) {
        return RoundEvaluatorDto.builder()
                .id(re.getId())
                .roundId(re.getRound() != null ? re.getRound().getId() : null)
                .userId(re.getUser() != null ? re.getUser().getId() : null)
                .evaluatorName(re.getUser() != null ? re.getUser().getFullName() : null)
                .evaluatorEmail(re.getUser() != null ? re.getUser().getEmail() : null)
                .roleOrDesignation(re.getRoleOrDesignation())
                .assignedByName(re.getAssignedBy() != null ? re.getAssignedBy().getFullName() : null)
                .createdAt(re.getCreatedAt())
                .build();
    }
}
