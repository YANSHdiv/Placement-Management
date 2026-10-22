package edu.jecrc.portal.modules.academic.ops.entity;

import edu.jecrc.portal.modules.academic.entity.AcademicBatch;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "crt_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrtProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "crt_program_id")
    private UUID id;

    @Column(name = "program_code", nullable = false, unique = true, length = 50)
    private String programCode;

    @Column(name = "program_name", nullable = false, length = 150)
    private String programName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear = "2025-2026";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_batch_id")
    private AcademicBatch targetBatch;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
