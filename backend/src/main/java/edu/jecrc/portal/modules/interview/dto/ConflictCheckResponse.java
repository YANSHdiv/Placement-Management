package edu.jecrc.portal.modules.interview.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictCheckResponse {
    private boolean hasConflict;
    private String message;

    @Builder.Default
    private List<InterviewAppointmentDto> studentConflicts = new ArrayList<>();

    @Builder.Default
    private List<InterviewAppointmentDto> evaluatorConflicts = new ArrayList<>();
}
