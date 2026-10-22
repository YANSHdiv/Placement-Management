package edu.jecrc.portal.modules.interview.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkCandidateResultResponse {
    private int totalRows;
    private int successfulCount;
    private int failedCount;

    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RowError {
        private Integer rowNumber;
        private String identifier;
        private String errorMessage;
    }
}
