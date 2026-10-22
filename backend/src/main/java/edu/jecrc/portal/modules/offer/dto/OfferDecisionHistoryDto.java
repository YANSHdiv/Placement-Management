package edu.jecrc.portal.modules.offer.dto;

import edu.jecrc.portal.modules.offer.entity.OfferDecisionHistory;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferDecisionHistoryDto {
    private UUID id;
    private UUID offerId;
    private String previousStatus;
    private String newStatus;
    private String actorEmail;
    private String actorRole;
    private String decisionReason;
    private Integer effectivePolicyVersion;
    private Instant createdAt;

    public static OfferDecisionHistoryDto fromEntity(OfferDecisionHistory h) {
        return OfferDecisionHistoryDto.builder()
                .id(h.getId())
                .offerId(h.getOffer() != null ? h.getOffer().getId() : null)
                .previousStatus(h.getPreviousStatus())
                .newStatus(h.getNewStatus())
                .actorEmail(h.getActorEmail())
                .actorRole(h.getActorRole())
                .decisionReason(h.getDecisionReason())
                .effectivePolicyVersion(h.getEffectivePolicyVersion())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
