package edu.jecrc.portal.modules.academic.ops.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "crt_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrtSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crt_program_id", nullable = false)
    private CrtProgram crtProgram;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Builder.Default
    @Column(name = "category", nullable = false, length = 50)
    private String category = "APTITUDE"; // APTITUDE, CODING, COMMUNICATION, INTERVIEW_PREP, RESUME_BUILDING

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "venue", length = 150)
    private String venue;

    @Column(name = "trainer_name", length = 150)
    private String trainerName;

    @Column(name = "trainer_email", length = 150)
    private String trainerEmail;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "SCHEDULED"; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "max_seats")
    private Integer maxSeats;

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
