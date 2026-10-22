package edu.jecrc.portal.modules.academic.entity;

import edu.jecrc.portal.modules.user.entity.Department;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "academic_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Integer id;

    @Column(name = "program_code", nullable = false, unique = true, length = 30)
    private String programCode;

    @Column(name = "program_name", nullable = false, length = 150)
    private String programName;

    @Column(name = "degree_type", nullable = false, length = 50)
    private String degreeType; // UG, PG, DIPLOMA, DOCTORAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Builder.Default
    @Column(name = "duration_years", nullable = false)
    private Integer durationYears = 4;

    @Builder.Default
    @Column(name = "total_semesters", nullable = false)
    private Integer totalSemesters = 8;

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
