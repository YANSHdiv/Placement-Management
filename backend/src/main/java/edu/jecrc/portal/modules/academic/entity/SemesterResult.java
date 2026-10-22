package edu.jecrc.portal.modules.academic.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "semester_results", uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_semester", columnNames = {"student_id", "semester_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    // SGPA can be NULL (never default to 0.0 for pending/unreleased results)
    @Column(name = "sgpa", precision = 4, scale = 2)
    private BigDecimal sgpa;

    @Column(name = "credits_registered", precision = 4, scale = 1)
    private BigDecimal creditsRegistered;

    @Column(name = "credits_earned", precision = 4, scale = 1)
    private BigDecimal creditsEarned;

    @Builder.Default
    @Column(name = "result_status", nullable = false, length = 30)
    private String resultStatus = "PASSED"; // PASSED, FAILED, DETAINED, WITHHELD

    @Builder.Default
    @Column(name = "has_active_backlog", nullable = false)
    private Boolean hasActiveBacklog = false;

    @Column(name = "marksheet_document_ref", length = 255)
    private String marksheetDocumentRef;

    @Builder.Default
    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "PENDING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_remarks", columnDefinition = "TEXT")
    private String verificationRemarks;

    @Builder.Default
    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectAttempt> subjectAttempts = new ArrayList<>();

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
