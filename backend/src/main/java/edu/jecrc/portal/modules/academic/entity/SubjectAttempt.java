package edu.jecrc.portal.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "subject_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private SemesterResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "subject_code", nullable = false, length = 30)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    @Builder.Default
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    @Builder.Default
    @Column(name = "credits", nullable = false, precision = 3, scale = 1)
    private BigDecimal credits = new BigDecimal("3.0");

    @Column(name = "grade", length = 10)
    private String grade;

    @Column(name = "marks_obtained", precision = 5, scale = 2)
    private BigDecimal marksObtained;

    @Column(name = "max_marks", precision = 5, scale = 2)
    private BigDecimal maxMarks;

    @Builder.Default
    @Column(name = "is_backlog", nullable = false)
    private Boolean isBacklog = false;

    @Builder.Default
    @Column(name = "is_cleared", nullable = false)
    private Boolean isCleared = false;

    @Column(name = "cleared_in_semester")
    private Integer clearedInSemester;

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
