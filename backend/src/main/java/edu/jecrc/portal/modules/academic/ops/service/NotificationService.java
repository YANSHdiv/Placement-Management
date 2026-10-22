package edu.jecrc.portal.modules.academic.ops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jecrc.portal.common.exception.ApiException;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.ops.dto.NotificationBroadcastRequest;
import edu.jecrc.portal.modules.academic.ops.dto.NotificationDto;
import edu.jecrc.portal.modules.academic.ops.entity.Notification;
import edu.jecrc.portal.modules.academic.ops.entity.NotificationOutbox;
import edu.jecrc.portal.modules.academic.ops.repository.NotificationOutboxRepository;
import edu.jecrc.portal.modules.academic.ops.repository.NotificationRepository;
import edu.jecrc.portal.modules.audit.service.AuditService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository outboxRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<NotificationDto> getMyNotifications(User caller, Boolean isRead, Pageable pageable) {
        if (caller == null) {
            throw ApiException.unauthorized("Authentication required");
        }
        if (isRead != null) {
            return notificationRepository.findByRecipientUserIdAndIsRead(caller.getId(), isRead, pageable)
                    .map(NotificationDto::from);
        }
        return notificationRepository.findByRecipientUserId(caller.getId(), pageable)
                .map(NotificationDto::from);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User caller) {
        if (caller == null) return 0;
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(caller.getId());
    }

    @Transactional
    public NotificationDto markAsRead(UUID notificationId, User caller) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (caller != null && notification.getRecipientUser() != null &&
                !caller.getId().equals(notification.getRecipientUser().getId())) {
            throw ApiException.forbidden("Cannot access or update notification belonging to another user");
        }

        notification.setIsRead(true);
        notification.setReadAt(Instant.now());
        Notification saved = notificationRepository.save(notification);
        return NotificationDto.from(saved);
    }

    @Transactional
    public int markAllAsRead(User caller) {
        if (caller == null) return 0;
        return notificationRepository.markAllAsReadForUser(caller.getId(), Instant.now());
    }

    @Transactional
    public NotificationDto broadcast(NotificationBroadcastRequest req, User caller) {
        String audienceType = (req.targetAudienceType() != null && !req.targetAudienceType().isBlank())
                ? req.targetAudienceType().toUpperCase().trim() : "INDIVIDUAL";

        User recipient = null;
        if (req.recipientUserId() != null) {
            recipient = userRepository.findById(req.recipientUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found: " + req.recipientUserId()));
        } else if ("INDIVIDUAL".equalsIgnoreCase(audienceType)) {
            recipient = caller;
        }

        String category = (req.category() != null && !req.category().isBlank())
                ? req.category().toUpperCase().trim() : "GENERAL";

        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .targetAudienceType(audienceType)
                .targetAudienceId(req.targetAudienceId())
                .title(req.title().trim())
                .message(req.message().trim())
                .category(category)
                .relatedEntityType(req.relatedEntityType())
                .relatedEntityId(req.relatedEntityId())
                .actionUrl(req.actionUrl())
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        // Transactional Outbox write
        try {
            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("notificationId", saved.getId());
            payloadMap.put("recipientUserId", recipient != null ? recipient.getId() : null);
            payloadMap.put("recipientEmail", recipient != null ? recipient.getEmail() : null);
            payloadMap.put("title", saved.getTitle());
            payloadMap.put("message", saved.getMessage());
            payloadMap.put("category", saved.getCategory());
            payloadMap.put("audienceType", audienceType);
            payloadMap.put("timestamp", Instant.now().toString());

            String payloadJson = objectMapper.writeValueAsString(payloadMap);

            NotificationOutbox outbox = NotificationOutbox.builder()
                    .eventType("NOTIFICATION_BROADCAST")
                    .payload(payloadJson)
                    .status("PENDING")
                    .retryCount(0)
                    .maxRetries(3)
                    .createdAt(Instant.now())
                    .build();

            outboxRepository.save(outbox);
        } catch (Exception ex) {
            log.error("Failed to serialize transactional notification outbox payload", ex);
        }

        auditService.logAsync(
                caller != null ? caller.getId() : null,
                caller != null ? caller.getEmail() : "SYSTEM",
                "NOTIFICATION_BROADCAST",
                "NOTIFICATION",
                saved.getId().toString(),
                Map.of("audienceType", audienceType, "title", saved.getTitle()),
                "127.0.0.1", "API"
        );

        return NotificationDto.from(saved);
    }
}
