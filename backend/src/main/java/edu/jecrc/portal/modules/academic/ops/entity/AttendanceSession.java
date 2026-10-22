package edu.jecrc.portal.modules.academic.ops.entity;

import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private TimetableSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private AcademicSubject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private FacultyProfile faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private AcademicBatch batch;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name = "section", nullable = false, length = 20)
    private String section;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "topic_covered")
    private String topicCovered;

    @Builder.Default
    @Column(name = "session_status", nullable = false, length = 30)
    private String sessionStatus = "OPEN"; // OPEN, FINALIZED, CANCELLED

    @Builder.Default
    @Column(name = "is_geofence_enabled", nullable = false)
    private Boolean isGeofenceEnabled = false;

    @Column(name = "authorized_latitude", precision = 10, scale = 7)
    private BigDecimal authorizedLatitude;

    @Column(name = "authorized_longitude", precision = 10, scale = 7)
    private BigDecimal authorizedLongitude;

    @Builder.Default
    @Column(name = "geofence_radius_meters", nullable = false)
    private Integer geofenceRadiusMeters = 60;

    @Builder.Default
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt = Instant.now();

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finalized_by")
    private User finalizedBy;

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
