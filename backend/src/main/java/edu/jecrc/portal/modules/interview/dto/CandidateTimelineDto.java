package edu.jecrc.portal.modules.interview.dto;

import edu.jecrc.portal.modules.interview.entity.CandidateStatusHistory;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateTimelineDto {
    private UUID id;
    private UUID applicationId;
    private UUID roundId;
    private String roundName;
    private String fromStatus;
    private String toStatus;
    private String changedByName;
    private String remarks;
    private Instant createdAt;

    public static CandidateTimelineDto fromEntity(CandidateStatusHistory h) {
        return CandidateTimelineDto.builder()
                .id(h.getId())
                .applicationId(h.getApplication() != null ? h.getApplication().getId() : null)
                .roundId(h.getRound() != null ? h.getRound().getId() : null)
                .roundName(h.getRound() != null ? h.getRound().getRoundName() : null)
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .changedByName(h.getChangedBy() != null ? h.getChangedBy().getFullName() : "System")
                .remarks(h.getRemarks())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
