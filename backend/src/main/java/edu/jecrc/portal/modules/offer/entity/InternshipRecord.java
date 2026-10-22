package edu.jecrc.portal.modules.offer.entity;

import edu.jecrc.portal.modules.academic.entity.Student;
import edu.jecrc.portal.modules.placement.entity.Company;
import edu.jecrc.portal.modules.placement.entity.JobApplication;
import edu.jecrc.portal.modules.placement.entity.JobProfile;
import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "internship_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_profile_id")
    private JobProfile jobProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placement_offer_id")
    private PlacementOffer placementOffer;

    @Column(name = "internship_title", nullable = false, length = 150)
    private String internshipTitle;

    @Column(name = "internship_type", nullable = false, length = 50)
    @Builder.Default
    private String internshipType = "SUMMER"; // SUMMER, SEMESTER, WINTER, FULL_TIME_CONCURRENT

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expected_end_date", nullable = false)
    private LocalDate expectedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "monthly_stipend_inr", precision = 10, scale = 2)
    private BigDecimal monthlyStipendInr;

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "work_arrangement", nullable = false, length = 50)
    @Builder.Default
    private String workArrangement = "ON_SITE"; // ON_SITE, HYBRID, REMOTE

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "appointment_doc_ref", length = 300)
    private String appointmentDocRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_mentor_id")
    private User facultyMentor;

    @Column(name = "industry_mentor_name", length = 150)
    private String industryMentorName;

    @Column(name = "industry_mentor_email", length = 200)
    private String industryMentorEmail;

    @Column(name = "industry_mentor_phone", length = 50)
    private String industryMentorPhone;

    @Column(name = "internship_status", nullable = false, length = 50)
    @Builder.Default
    private String internshipStatus = "OFFERED"; // OFFERED, ACCEPTED, ACTIVE, COMPLETED, TERMINATED, WITHDRAWN, DECLINED

    @Column(name = "has_ppo_potential", nullable = false)
    @Builder.Default
    private Boolean hasPpoPotential = false;

    @Column(name = "completion_remarks", columnDefinition = "TEXT")
    private String completionRemarks;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
