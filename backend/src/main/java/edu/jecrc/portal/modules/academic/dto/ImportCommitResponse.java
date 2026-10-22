package edu.jecrc.portal.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportCommitResponse {
    private UUID importId;
    private String status;
    private Integer totalRows;
    private Integer insertedCount;
    private Integer updatedCount;
    private Integer skippedCount;
    private Integer errorRows;
    private Instant committedAt;
    private String message;
}
