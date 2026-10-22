package edu.jecrc.portal.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportPreviewResponse {
    private UUID importId;
    private String fileName;
    private String fileType;
    private Integer totalRows;
    private Integer validRows;
    private Integer errorRows;
    private List<ImportRowPreviewDto> sampleRows;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportRowPreviewDto {
        private Integer rowNumber;
        private Map<String, Object> data;
        private Boolean isValid;
        private List<String> errors;
        private List<String> warnings;
    }
}
