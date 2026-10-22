package edu.jecrc.portal.modules.placement.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "placement_drives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "drive_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "drive_title", nullable = false)
    private String driveTitle;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Builder.Default
    @Column(name = "drive_status", nullable = false, length = 30)
    private String driveStatus = "DRAFT"; // DRAFT, PUBLISHED, IN_PROGRESS, CLOSED, COMPLETED, ARCHIVED

    @Column(name = "registration_start_date")
    private Instant registrationStartDate;

    @Column(name = "registration_deadline")
    private Instant registrationDeadline;

    @Column(name = "drive_date")
    private LocalDate driveDate;

    @Column(name = "recruitment_schedule", columnDefinition = "TEXT")
    private String recruitmentSchedule;

    @Column(name = "venue")
    private String venue;

    @Column(name = "reporting_time", length = 50)
    private String reportingTime;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "contact_details", columnDefinition = "TEXT")
    private String contactDetails;

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobProfile> jobProfiles = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
