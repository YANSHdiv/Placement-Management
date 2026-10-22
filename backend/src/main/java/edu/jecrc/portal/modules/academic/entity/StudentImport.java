package edu.jecrc.portal.modules.academic.entity;

import edu.jecrc.portal.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "student_imports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentImport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "import_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by", nullable = false)
    private User importedBy;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType; // CSV, XLSX

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "PREVIEWED"; // PREVIEWED, COMMITTED, FAILED, CANCELLED

    @Builder.Default
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows = 0;

    @Builder.Default
    @Column(name = "valid_rows", nullable = false)
    private Integer validRows = 0;

    @Builder.Default
    @Column(name = "error_rows", nullable = false)
    private Integer errorRows = 0;

    @Builder.Default
    @Column(name = "inserted_count", nullable = false)
    private Integer insertedCount = 0;

    @Builder.Default
    @Column(name = "updated_count", nullable = false)
    private Integer updatedCount = 0;

    @Builder.Default
    @Column(name = "skipped_count", nullable = false)
    private Integer skippedCount = 0;

    @Builder.Default
    @OneToMany(mappedBy = "studentImport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentImportRow> rows = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "committed_at")
    private Instant committedAt;
}
