package edu.jecrc.portal.modules.academic.ops.entity;

import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "portal_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id")
    private UUID id;

    @Column(name = "event_title", nullable = false, length = 200)
    private String eventTitle;

    @Builder.Default
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType = "WORKSHOP"; // WORKSHOP, SEMINAR, HACKATHON, PRE_PLACEMENT_TALK, CAREER_SESSION, ANNOUNCEMENT

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "venue", length = 200)
    private String venue;

    @Column(name = "online_meeting_url", length = 300)
    private String onlineMeetingUrl;

    @Builder.Default
    @Column(name = "target_audience", nullable = false, length = 50)
    private String targetAudience = "ALL"; // ALL, BTECH_CSE, FINAL_YEAR, FACULTY_ONLY, PLACEMENT_ELIGIBLE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private AcademicBatch batch;

    @Column(name = "registration_deadline")
    private Instant registrationDeadline;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "organizer_name", length = 150)
    private String organizerName;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, CANCELLED, CONCLUDED

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
