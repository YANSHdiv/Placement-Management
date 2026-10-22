package edu.jecrc.portal.modules.interview.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkCandidateResultUploadRequest {

    private List<ResultRow> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultRow {
        private Integer rowNumber;
        private UUID applicationId;
        private String universityStudentId;
        private String status; // ADVANCED, ELIMINATED, ON_HOLD, ABSENT
        private BigDecimal score;
        private String remarks;
    }
}
