package edu.jecrc.portal.modules.academic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportCommitRequest {

    @NotNull(message = "Import ID is required")
    private UUID importId;

    @Builder.Default
    private Boolean updateExisting = true;
}
