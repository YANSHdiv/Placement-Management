package edu.jecrc.portal.modules.academic.ops.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;

    @Builder.Default
    @Column(name = "target_audience_type", nullable = false, length = 50)
    private String targetAudienceType = "INDIVIDUAL"; // INDIVIDUAL, PROGRAM, BATCH, DEPARTMENT, ALL_STUDENTS, ALL_FACULTY

    @Column(name = "target_audience_id", length = 100)
    private String targetAudienceId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    @Column(name = "category", nullable = false, length = 50)
    private String category = "GENERAL"; // ACADEMIC, TIMETABLE, ATTENDANCE, RECRUITMENT_DRIVE, INTERVIEW, OFFER, CRT, EVENT, GENERAL

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id", length = 100)
    private String relatedEntityId;

    @Column(name = "action_url", length = 255)
    private String actionUrl;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
