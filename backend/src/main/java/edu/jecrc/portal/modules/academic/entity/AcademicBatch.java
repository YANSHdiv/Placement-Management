package edu.jecrc.portal.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "academic_batches", uniqueConstraints = {
        @UniqueConstraint(name = "uq_program_grad_year", columnNames = {"program_id", "graduation_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Integer id;

    @Column(name = "batch_name", nullable = false, length = 100)
    private String batchName;

    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private AcademicProgram program;

    @Builder.Default
    @Column(name = "is_placement_active", nullable = false)
    private Boolean isPlacementActive = true;

    @Builder.Default
    @Column(name = "current_semester", nullable = false)
    private Integer currentSemester = 1;

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
