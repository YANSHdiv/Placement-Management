package edu.jecrc.portal.modules.academic.ops.dto;

import edu.jecrc.portal.modules.academic.ops.entity.Notification;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record NotificationDto(
        UUID id,
        UUID recipientUserId,
        String targetAudienceType,
        String targetAudienceId,
        String title,
        String message,
        String category,
        String relatedEntityType,
        String relatedEntityId,
        String actionUrl,
        Boolean isRead,
        Instant readAt,
        Instant createdAt
) {
    public static NotificationDto from(Notification n) {
        if (n == null) return null;
        return NotificationDto.builder()
                .id(n.getId())
                .recipientUserId(n.getRecipientUser() != null ? n.getRecipientUser().getId() : null)
                .targetAudienceType(n.getTargetAudienceType())
                .targetAudienceId(n.getTargetAudienceId())
                .title(n.getTitle())
                .message(n.getMessage())
                .category(n.getCategory())
                .relatedEntityType(n.getRelatedEntityType())
                .relatedEntityId(n.getRelatedEntityId())
                .actionUrl(n.getActionUrl())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
