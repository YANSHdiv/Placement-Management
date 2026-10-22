package edu.jecrc.portal.modules.academic.entity;

import edu.jecrc.portal.modules.user.entity.Department;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "student_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "university_student_id", nullable = false, unique = true, length = 50)
    private String universityStudentId;

    @Column(name = "roll_number", nullable = false, unique = true, length = 50)
    private String rollNumber;

    @Column(name = "registration_number", unique = true, length = 50)
    private String registrationNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "university_email", nullable = false, unique = true, length = 150)
    private String universityEmail;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private AcademicProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private AcademicBatch batch;

    @Builder.Default
    @Column(name = "current_semester", nullable = false)
    private Integer currentSemester = 1;

    @Column(name = "section", length = 20)
    private String section;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Builder.Default
    @Column(name = "academic_status", nullable = false, length = 30)
    private String academicStatus = "ENROLLED"; // ENROLLED, SUSPENDED, ALUMNI, DROPOUT

    @Builder.Default
    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED, CHANGES_REQUESTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_remarks", columnDefinition = "TEXT")
    private String verificationRemarks;

    // Derived Summary Metrics (can be null, never default to 0.0 for unknown CGPA)
    @Column(name = "cgpa", precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Builder.Default
    @Column(name = "active_backlogs", nullable = false)
    private Integer activeBacklogs = 0;

    @Builder.Default
    @Column(name = "historical_backlogs", nullable = false)
    private Integer historicalBacklogs = 0;

    @Builder.Default
    @Column(name = "total_credits_earned", precision = 5, scale = 2)
    private BigDecimal totalCreditsEarned = BigDecimal.ZERO;

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

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
