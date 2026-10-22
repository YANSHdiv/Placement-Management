package edu.jecrc.portal.modules.academic.ops.entity;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
        @UniqueConstraint(name = "uq_session_student", columnNames = {"session_id", "student_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "record_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Builder.Default
    @Column(name = "attendance_status", nullable = false, length = 30)
    private String attendanceStatus = "PRESENT"; // PRESENT, ABSENT, LATE, EXCUSED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    @Builder.Default
    @Column(name = "marked_at", nullable = false)
    private Instant markedAt = Instant.now();

    @Builder.Default
    @Column(name = "verification_method", nullable = false, length = 30)
    private String verificationMethod = "MANUAL_ROSTER"; // MANUAL_ROSTER, GEOTAGGED_SELF, QR_CODE

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "accuracy_meters", precision = 8, scale = 2)
    private BigDecimal accuracyMeters;

    @Column(name = "calculated_distance_meters", precision = 8, scale = 2)
    private BigDecimal calculatedDistanceMeters;

    @Builder.Default
    @Column(name = "geofence_status", nullable = false, length = 30)
    private String geofenceStatus = "NOT_APPLICABLE"; // WITHIN_GEOFENCE, OUTSIDE_GEOFENCE, LOCATION_UNAVAILABLE, ACCURACY_EXCEEDED, EXEMPTION_APPROVED, NOT_APPLICABLE

    @Column(name = "evidence_photo_ref", length = 300)
    private String evidencePhotoRef;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    @Column(name = "is_corrected", nullable = false)
    private Boolean isCorrected = false;

    @Column(name = "original_status", length = 30)
    private String originalStatus;

    @Column(name = "correction_reason", columnDefinition = "TEXT")
    private String correctionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_by")
    private User correctedBy;

    @Column(name = "corrected_at")
    private Instant correctedAt;
}
