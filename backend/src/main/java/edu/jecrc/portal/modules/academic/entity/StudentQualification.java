package edu.jecrc.portal.modules.academic.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "student_qualifications", uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_qual_type", columnNames = {"student_id", "qualification_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qualification_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "qualification_type", nullable = false, length = 30)
    private String qualificationType; // CLASS_10, CLASS_12, DIPLOMA, GRADUATION

    @Column(name = "board_or_university", nullable = false, length = 150)
    private String boardOrUniversity;

    @Column(name = "institution_name", length = 200)
    private String institutionName;

    @Column(name = "passing_year", nullable = false)
    private Integer passingYear;

    @Builder.Default
    @Column(name = "marking_scheme", nullable = false, length = 20)
    private String markingScheme = "PERCENTAGE"; // PERCENTAGE, CGPA

    // Can be NULL (do not default to 0.0)
    @Column(name = "percentage_or_cgpa", precision = 5, scale = 2)
    private BigDecimal percentageOrCgpa;

    @Column(name = "maximum_marks", precision = 6, scale = 2)
    private BigDecimal maximumMarks;

    @Column(name = "obtained_marks", precision = 6, scale = 2)
    private BigDecimal obtainedMarks;

    @Column(name = "marksheet_document_ref", length = 255)
    private String marksheetDocumentRef;

    @Builder.Default
    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_remarks", columnDefinition = "TEXT")
    private String verificationRemarks;

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
