package edu.jecrc.portal.modules.academic.ops.entity;

import edu.jecrc.portal.modules.academic.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_registrations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_event_student", columnNames = {"event_id", "student_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "registration_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private PortalEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Builder.Default
    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt = Instant.now();

    @Builder.Default
    @Column(name = "attendance_status", nullable = false, length = 30)
    private String attendanceStatus = "REGISTERED"; // REGISTERED, ATTENDED, ABSENT
}
