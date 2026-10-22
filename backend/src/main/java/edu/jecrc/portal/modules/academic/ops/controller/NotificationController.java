package edu.jecrc.portal.modules.academic.ops.controller;

import edu.jecrc.portal.common.api.ApiResponse;
import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.common.exception.ResourceNotFoundException;
import edu.jecrc.portal.modules.academic.ops.dto.NotificationBroadcastRequest;
import edu.jecrc.portal.modules.academic.ops.dto.NotificationDto;
import edu.jecrc.portal.modules.academic.ops.service.NotificationService;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications & Announcements", description = "In-app notifications feed, transactional outbox alerts, and broadcast announcements")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User getCallerUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    @Operation(summary = "Get user's personal notifications feed")
    public ResponseEntity<PageResponse<NotificationDto>> getMyNotifications(
            @RequestParam(required = false) Boolean isRead,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication auth
    ) {
        Page<NotificationDto> page = notificationService.getMyNotifications(getCallerUser(auth), isRead, pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    @Operation(summary = "Get total count of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Authentication auth) {
        long count = notificationService.getUnreadCount(getCallerUser(auth));
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", Map.of("unreadCount", count)));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(
            @PathVariable UUID notificationId,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read",
                notificationService.markAsRead(notificationId, getCallerUser(auth))));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(Authentication auth) {
        int updated = notificationService.markAllAsRead(getCallerUser(auth));
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", Map.of("markedCount", updated)));
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasAuthority('NOTIFICATION_BROADCAST')")
    @Operation(summary = "Broadcast announcement to individuals, cohorts, or departments (with transactional outbox)")
    public ResponseEntity<ApiResponse<NotificationDto>> broadcast(
            @Valid @RequestBody NotificationBroadcastRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(ApiResponse.success("Notification broadcast dispatched",
                notificationService.broadcast(req, getCallerUser(auth))));
    }
}
