package edu.jecrc.portal.modules.placement.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id")
    private UUID id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_code", nullable = false, unique = true, length = 50)
    private String companyCode;

    @Column(name = "website")
    private String website;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recruiter_name", length = 100)
    private String recruiterName;

    @Column(name = "recruiter_email", length = 100)
    private String recruiterEmail;

    @Column(name = "recruiter_phone", length = 25)
    private String recruiterPhone;

    @Builder.Default
    @Column(name = "company_status", nullable = false, length = 30)
    private String companyStatus = "ACTIVE"; // ACTIVE, INACTIVE, ARCHIVED

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

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
